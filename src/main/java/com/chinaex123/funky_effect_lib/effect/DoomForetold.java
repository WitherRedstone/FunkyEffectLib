package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.effect.DoomMarkSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/**
 * 厄运预示：攻击时有概率为目标施加标记，标记期间内目标受到的下一次伤害大幅增加
 * <p>
 * 机制：
 * <ol>
 *   <li>攻击时有30%概率为被攻击者施加厄运标记</li>
 *   <li>标记期间内，目标受到的下一次伤害增加至2.5倍</li>
 *   <li>伤害触发后移除标记</li>
 *   <li>标记状态同步给附近64格内的所有玩家</li>
 *   <li>玩家加入游戏时同步所有现有标记</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class DoomForetold extends MobEffect {

    private static final String DOOM_FORETOLD_TAG = "doom_foretold";

    /** 标记施加概率 **/
    private static final float MARK_CHANCE = 0.3f;
    /** 伤害倍率 **/
    private static final float DAMAGE_MULTIPLIER = 2.5f;
    /** 标记同步范围 **/
    private static final int SYNC_RANGE = 64;

    public DoomForetold(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家加入游戏事件处理
     * 同步所有带有厄运标记的实体到新加入的玩家
     *
     * @param event 玩家登录事件
     */
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // 遍历世界所有实体，同步带有标记的实体
        for (var entity : serverLevel.getAllEntities()) {
            if (entity instanceof LivingEntity living && hasMark(living)) {
                PacketDistributor.sendToPlayer(player, new DoomMarkSyncPacket(living.getUUID(), true));
            }
        }
    }

    /**
     * 实体受伤事件处理
     * 施加标记或触发标记效果
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        var source = event.getSource();
        var attacker = source.getEntity();

        // 检查攻击者是否为LivingEntity
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        if (target.level().isClientSide()) {
            return;
        }

        // 检查攻击者是否拥有厄运预示效果
        var effect = livingAttacker.getEffect(FELEffects.DOOM_FORETOLD);
        if (effect == null) {
            return;
        }

        boolean hasMark = hasMark(target);

        if (hasMark) {
            // 目标有标记：伤害倍率提升，移除标记
            float originalDamage = event.getOriginalDamage();
            float newDamage = originalDamage * DAMAGE_MULTIPLIER;
            event.setNewDamage(newDamage);
            removeMark(target);
            broadcastMarkToNearbyPlayers(target, false);
        } else {
            // 目标无标记：概率施加标记
            if (livingAttacker.getRandom().nextFloat() < MARK_CHANCE) {
                applyMark(target);
                broadcastMarkToNearbyPlayers(target, true);
            }
        }
    }

    /**
     * 将标记状态广播给附近所有玩家
     *
     * @param target 目标实体
     * @param hasMark 是否拥有标记
     */
    private static void broadcastMarkToNearbyPlayers(LivingEntity target, boolean hasMark) {
        if (target.level() instanceof ServerLevel serverLevel) {
            var players = serverLevel.players();
            for (var player : players) {
                // 仅在范围内的玩家同步
                if (player.distanceTo(target) <= SYNC_RANGE) {
                    PacketDistributor.sendToPlayer(player, new DoomMarkSyncPacket(target.getUUID(), hasMark));
                }
            }
        }
    }

    /**
     * 为实体施加厄运标记
     *
     * @param entity 目标实体
     */
    private static void applyMark(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        data.putBoolean(DOOM_FORETOLD_TAG, true);
    }

    /**
     * 检查实体是否拥有厄运标记
     *
     * @param entity 目标实体
     * @return true表示拥有标记
     */
    private static boolean hasMark(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.getBoolean(DOOM_FORETOLD_TAG);
    }

    /**
     * 移除实体的厄运标记
     *
     * @param entity 目标实体
     */
    private static void removeMark(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        data.remove(DOOM_FORETOLD_TAG);
    }
}