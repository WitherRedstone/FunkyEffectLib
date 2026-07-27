package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELDamageTypes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.effect.SoulburnSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/**
 * 魂燃：潜行时消耗经验值，使下一次攻击时附带无视护甲和减伤的真实伤害
 * <p>
 * 机制：
 * <ol>
 *   <li>潜行时消耗50点经验值为攻击充能</li>
 *   <li>充能后下一次攻击造成额外真实伤害</li>
 *   <li>基础伤害4点，每级增加2点</li>
 *   <li>攻击后消耗充能，需要重新充能</li>
 *   <li>每次潜行只能充能一次（有冷却标记）</li>
 *   <li>真实伤害附加在原伤害之上，不被护甲减免</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Soulburn extends MobEffect {

    private static final ResourceLocation SOULBURN_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "soulburn");

    /** 充能状态存储键 **/
    private static final String SOULBURN_TAG = "soulburn_charged";
    /** 冷却状态存储键 **/
    private static final String SOULBURN_COOLDOWN_TAG = "soulburn_cooldown";

    /** 基础伤害 **/
    private static final float DAMAGE_BASE = 4.0f;
    /** 每级伤害增加 **/
    private static final float DAMAGE_PER_LEVEL = 2.0f;
    /** 消耗经验值 **/
    private static final int COST_EXP = 50;

    public Soulburn(int color) {
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
     * 玩家登录事件处理
     * 同步充能状态到客户端
     *
     * @param event 玩家登录事件
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.hasEffect(FELEffects.SOULBURN)) {
                boolean hasCharge = hasCharge(player);
                PacketDistributor.sendToPlayer(player, new SoulburnSyncPacket(player.getUUID(), hasCharge));
            }
        }
    }

    /**
     * 效果移除事件处理
     * 清理充能状态
     *
     * @param event 效果移除事件
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.SOULBURN && event.getEntity() instanceof Player player) {
            clearCharge(player);
        }
    }

    /**
     * 效果过期事件处理
     * 清理充能状态
     *
     * @param event 效果过期事件
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null &&
                event.getEffectInstance().getEffect() == FELEffects.SOULBURN &&
                event.getEntity() instanceof Player player) {
            clearCharge(player);
        }
    }

    /**
     * 玩家Tick事件处理
     * 管理充能状态
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        // 检查是否拥有魂燃效果
        var effect = player.getEffect(FELEffects.SOULBURN);
        if (effect == null) {
            if (hasCharge(player)) {
                clearCharge(player);
            }
            return;
        }

        var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute == null) return;

        // 潜行时充能
        if (player.isShiftKeyDown()) {
            // 检查是否在冷却中
            if (isCooldown(player)) {
                return;
            }

            // 如果没有充能，消耗经验进行充能
            if (!hasCharge(player)) {
                if (player.totalExperience >= COST_EXP) {
                    // 扣除经验值
                    player.giveExperiencePoints(-COST_EXP);
                    setCharge(player, true);

                    // 同步到客户端
                    if (player instanceof ServerPlayer serverPlayer) {
                        PacketDistributor.sendToPlayer(serverPlayer, new SoulburnSyncPacket(player.getUUID(), true));
                    }

                    // 添加攻击力加成（显示在属性面板）
                    int amplifier = effect.getAmplifier();
                    float damageBonus = DAMAGE_BASE + (amplifier * DAMAGE_PER_LEVEL);
                    attribute.addTransientModifier(new AttributeModifier(
                            SOULBURN_MODIFIER,
                            damageBonus,
                            AttributeModifier.Operation.ADD_VALUE
                    ));

                    // 设置冷却（防止本次潜行重复充能）
                    setCooldown(player, true);
                }
            }
        } else {
            // 取消潜行时重置冷却
            setCooldown(player, false);
        }
    }

    /**
     * 实体受伤事件处理
     * 应用真实伤害
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        var source = event.getSource();
        var attacker = source.getEntity();

        // 检查攻击者是否为玩家
        if (!(attacker instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // 检查是否拥有魂燃效果
        var effect = player.getEffect(FELEffects.SOULBURN);
        if (effect == null) {
            return;
        }

        // 检查是否有充能
        if (!hasCharge(player)) {
            return;
        }

        // 计算真实伤害
        int amplifier = effect.getAmplifier();
        float realDamage = DAMAGE_BASE + (amplifier * DAMAGE_PER_LEVEL);

        // 清空充能
        setCharge(player, false);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SoulburnSyncPacket(player.getUUID(), false));
        }

        // 移除攻击力加成
        var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute != null) {
            attribute.removeModifier(SOULBURN_MODIFIER);
        }

        // 在原伤害基础上增加真实伤害（真实伤害无视护甲和减伤）
        float originalDamage = event.getNewDamage();
        event.setNewDamage(originalDamage + realDamage);
    }

    /**
     * 清除充能状态
     *
     * @param player 玩家对象
     */
    private static void clearCharge(Player player) {
        setCharge(player, false);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SoulburnSyncPacket(player.getUUID(), false));
        }
        var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute != null) {
            attribute.removeModifier(SOULBURN_MODIFIER);
        }
        setCooldown(player, false);
    }

    /**
     * 检查是否有充能
     *
     * @param player 玩家对象
     * @return true表示有充能
     */
    private static boolean hasCharge(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(SOULBURN_TAG);
    }

    /**
     * 设置充能状态
     *
     * @param player 玩家对象
     * @param charged 是否充能
     */
    private static void setCharge(Player player, boolean charged) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(SOULBURN_TAG, charged);
    }

    /**
     * 检查是否在冷却中
     *
     * @param player 玩家对象
     * @return true表示在冷却中
     */
    private static boolean isCooldown(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(SOULBURN_COOLDOWN_TAG);
    }

    /**
     * 设置冷却状态
     *
     * @param player 玩家对象
     * @param cooldown 是否冷却
     */
    private static void setCooldown(Player player, boolean cooldown) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(SOULBURN_COOLDOWN_TAG, cooldown);
    }
}