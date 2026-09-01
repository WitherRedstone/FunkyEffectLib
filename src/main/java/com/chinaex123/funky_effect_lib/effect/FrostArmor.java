package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.api.StackableArmorAPI;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.effect.FrostArmorSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;

/**
 * 冰霜护甲状态效果类
 * <p>
 * 该效果为增益效果，通过拾取经验球积攒冰晶层数，每层提供伤害减免。
 * 冰晶具有独立的过期时间，过期后自动消失。
 * <p>
 * 核心机制：
 * <ul>
 *   <li>拾取经验球时有概率生成冰晶（基础25%，每级效果+5%）</li>
 *   <li>最大冰晶数量：10个</li>
 *   <li>每个冰晶提供5%伤害减免</li>
 *   <li>最高伤害减免：50%（10层 × 5%）</li>
 *   <li>每个冰晶持续100刻（5秒），过期后自动移除</li>
 *   <li>冰晶过期时间存储在实体的持久化数据中</li>
 * </ul>
 * <p>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class FrostArmor extends MobEffect {

    /**
     * 冰霜护甲配置
     * <p>
     * 定义了冰晶管理所需的全部参数：
     * <ul>
     *   <li>存储键名：frost_armor_crystal_expiry_array</li>
     *   <li>属性修改器UUID：基于"frost_armor_modifier"生成</li>
     *   <li>每层减伤：5%</li>
     *   <li>最大减伤：50%</li>
     *   <li>最大层数：10层</li>
     *   <li>每层持续时间：100刻（5秒）</li>
     *   <li>关联效果：FROST_ARMOR</li>
     * </ul>
     */
    public static final StackableArmorAPI.ArmorConfig CONFIG = new StackableArmorAPI.ArmorConfig(
            FunkyEffectLib.id("frost_armor_crystal_expiry_array"),
            FunkyEffectLib.id("frost_armor_damage_reduction"),
            0.05f,   // 每层伤害减免
            0.5f,    // 最高伤害减免
            10,      // 最大层数
            100,     // 每层持续时间
            () -> FELEffects.FROST_ARMOR,
            FrostArmor::syncToClient  // 同步函数
    );

    /**
     * 构造冰霜护甲效果
     *
     * @param color 效果颜色值（用于效果图标和粒子颜色）
     */
    public FrostArmor(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    /**
     * 效果开始应用时调用
     */
    @Override
    public void onEffectStarted(@NotNull LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);
        StackableArmorAPI.onEffectGained(entity, CONFIG);
    }

    /**
     * 效果移除事件处理（手动移除时）
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.FROST_ARMOR) {
            StackableArmorAPI.onEffectLost(event.getEntity(), CONFIG);
        }
    }

    /**
     * 效果过期事件处理（自然过期时）
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.FROST_ARMOR) {
            StackableArmorAPI.onEffectLost(event.getEntity(), CONFIG);
        }
    }

    /**
     * 每帧应用效果时调用的方法
     * <p>
     * 在效果生效期间，每刻检查并清理已过期的冰晶层数
     *
     * @param entity 拥有该效果的实体
     * @param amplifier 效果等级（从0开始）
     */
    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        StackableArmorAPI.onEffectTick(entity, CONFIG);
        return true;
    }

    /**
     * 判断效果是否应在每刻执行更新
     *
     * @param duration 剩余持续时间（刻）
     * @param amplifier 效果等级
     * @return 始终返回true，使效果持续生效
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 添加一层冰晶
     * <p>
     * 向实体添加一个新的冰晶层数，如果已达到上限则返回false
     *
     * @param entity 目标实体
     */
    public static void addCrystal(LivingEntity entity) {
        StackableArmorAPI.addLayer(entity, CONFIG);
    }

    /**
     * 移除一层冰晶（最早的一层）
     * <p>
     * 移除最早添加的冰晶层数（FIFO顺序）
     *
     * @param entity 目标实体
     * @return true表示成功移除，false表示无层数可移除
     */
    public static boolean removeCrystal(LivingEntity entity) {
        return StackableArmorAPI.removeLayer(entity, CONFIG);
    }

    /**
     * 清除所有冰晶层数
     * <p>
     * 移除所有冰晶层数并删除伤害减免属性
     *
     * @param entity 目标实体
     */
    public static void clearCrystals(LivingEntity entity) {
        StackableArmorAPI.clearAllLayers(entity, CONFIG);
    }

    /**
     * 获取当前冰晶数量
     *
     * @param entity 目标实体
     * @return 当前冰晶层数
     */
    public static int getCrystalCount(LivingEntity entity) {
        return StackableArmorAPI.getLayerCount(entity, CONFIG);
    }

    /**
     * 获取剩余时间（秒）
     * <p>
     * 返回最早过期的冰晶距离当前时间的剩余秒数
     * 如果没有任何冰晶，返回0
     *
     * @param entity 目标实体
     * @return 剩余秒数（向上取整）
     */
    public static int getRemainingSeconds(LivingEntity entity) {
        return StackableArmorAPI.getRemainingSeconds(entity, CONFIG);
    }

    /**
     * 同步数据到客户端
     *
     * @param entity 目标实体
     * @param expiryArray 过期时间数组
     */
    public static void syncToClient(LivingEntity entity, long[] expiryArray) {
        if (entity instanceof ServerPlayer serverPlayer) {
            int count = expiryArray.length;
            long earliestExpiry = count > 0 ? expiryArray[0] : 0;
            int remainingSeconds = StackableArmorAPI.getRemainingSeconds(entity, CONFIG);
            PacketDistributor.sendToPlayer(serverPlayer,
                    new FrostArmorSyncPacket(entity.getUUID(), count, earliestExpiry, remainingSeconds));
        }
    }

    /**
     * 玩家拾取经验球事件处理
     * 监听经验球拾取，有概率生成冰晶
     */
    @SubscribeEvent
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        MobEffectInstance effect = player.getEffect(FELEffects.FROST_ARMOR);
        if (effect == null) return;

        // 概率：基础25%，每级+5%
        float chance = 0.25f + (effect.getAmplifier() * 0.05f);
        if (player.getRandom().nextFloat() < chance) {
            addCrystal(player);
        }
    }
}