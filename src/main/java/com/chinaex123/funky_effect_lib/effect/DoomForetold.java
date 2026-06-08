package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.NetworkHandler;
import com.chinaex123.funky_effect_lib.network.effect.DoomMarkSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/** 厄运预示：攻击时有概率为目标施加标记，标记期间内目标受到的下一次伤害大幅增加 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class DoomForetold extends MobEffect {

    private static final String DOOM_FORETOLD_TAG = "doom_foretold";
    private static final float MARK_CHANCE = 0.3f; // 标记概率
    private static final float DAMAGE_MULTIPLIER = 2.5f; // 伤害倍增

    public DoomForetold(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        for (var entity : serverLevel.getAllEntities()) {
            if (entity instanceof LivingEntity living && hasMark(living)) {
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new DoomMarkSyncPacket(living.getUUID(), true));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        var source = event.getSource();
        var attacker = source.getEntity();

        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        if (target.level().isClientSide()) {
            return;
        }

        MobEffectInstance effect = livingAttacker.getEffect(FELEffects.DOOM_FORETOLD.get());
        if (effect == null) {
            return;
        }

        boolean hasMark = hasMark(target);

        if (hasMark) {
            float originalDamage = event.getAmount();
            float newDamage = originalDamage * DAMAGE_MULTIPLIER;
            event.setAmount(newDamage);
            removeMark(target);
            broadcastMarkToNearbyPlayers(target, false);
        } else {
            if (livingAttacker.getRandom().nextFloat() < MARK_CHANCE) {
                applyMark(target);
                broadcastMarkToNearbyPlayers(target, true);
            }
        }
    }

    private static void broadcastMarkToNearbyPlayers(LivingEntity target, boolean hasMark) {
        if (target.level() instanceof ServerLevel serverLevel) {
            var players = serverLevel.players();
            for (var player : players) {
                if (player.distanceTo(target) <= 64) {
                    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new DoomMarkSyncPacket(target.getUUID(), hasMark));
                }
            }
        }
    }

    private static void applyMark(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        data.putBoolean(DOOM_FORETOLD_TAG, true);
    }

    private static boolean hasMark(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.getBoolean(DOOM_FORETOLD_TAG);
    }

    private static void removeMark(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        data.remove(DOOM_FORETOLD_TAG);
    }
}