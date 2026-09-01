package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.api.StackableArmorAPI;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.effect.WovenMailSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 织造铠甲状态效果类
 * <p>
 * 该效果为增益效果，通过拾取经验球积攒缠结层数，每层提供较高的伤害减免。
 * 缠结具有独立的过期时间，过期后自动消失。
 * <p>
 * 核心机制：
 * <ul>
 *   <li>拾取经验球时有概率生成缠结（基础25%，每级效果+5%）</li>
 *   <li>最大缠结数量：10个</li>
 *   <li>每个缠结提供8%伤害减免</li>
 *   <li>最高伤害减免：80%（10层 × 8%）</li>
 *   <li>每个缠结持续100刻（5秒），过期后自动移除</li>
 *   <li>缠结过期时间存储在实体的持久化数据中</li>
 * </ul>
 * <p>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class WovenMail extends MobEffect {

    /** 记录每个玩家上一tick的经验值，用于检测经验变化 **/
    private static final Map<UUID, Integer> LAST_XP_MAP = new HashMap<>();

    /**
     * 织造铠甲配置
     * <p>
     * 定义了缠结管理所需的全部参数：
     * <ul>
     *   <li>存储键名：woven_mail_tangle_expiry_array</li>
     *   <li>属性修改器UUID：基于"woven_mail_modifier"生成</li>
     *   <li>每层减伤：8%</li>
     *   <li>最大减伤：80%</li>
     *   <li>最大层数：10层</li>
     *   <li>每层持续时间：100刻（5秒）</li>
     *   <li>关联效果：WOVEN_MAIL</li>
     * </ul>
     */
    public static final StackableArmorAPI.ArmorConfig CONFIG = new StackableArmorAPI.ArmorConfig(
            FunkyEffectLib.id("woven_mail_tangle_expiry_array"),
            FunkyEffectLib.id("woven_mail_damage_reduction"),
            0.08f,   // 每层伤害减免
            0.8f,    // 最高伤害减免
            10,      // 最大层数
            100,     // 每层持续时间
            () -> FELEffects.WOVEN_MAIL,
            WovenMail::syncToClient  // 同步函数
    );

    /**
     * 构造织造铠甲效果
     *
     * @param color 效果颜色值（用于效果图标和粒子颜色）
     */
    public WovenMail(int color) {
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
        if (event.getEffect() == FELEffects.WOVEN_MAIL) {
            StackableArmorAPI.onEffectLost(event.getEntity(), CONFIG);
        }
    }

    /**
     * 效果过期事件处理（自然过期时）
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.WOVEN_MAIL) {
            StackableArmorAPI.onEffectLost(event.getEntity(), CONFIG);
        }
    }

    /**
     * 每帧应用效果时调用的方法
     * <p>
     * 在效果生效期间，每刻检查并清理已过期的缠结层数
     *
     * @param entity 拥有该效果的实体
     * @param amplifier 效果等级（从0开始）
     */
    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        StackableArmorAPI.onEffectTick(entity, CONFIG);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 添加一层缠结
     * <p>
     * 向实体添加一个新的缠结层数，如果已达到上限则返回false
     *
     * @param entity 目标实体
     */
    public static void addTangle(LivingEntity entity) {
        StackableArmorAPI.addLayer(entity, CONFIG);
    }

    /**
     * 移除一层缠结（最早的一层）
     * <p>
     * 移除最早添加的缠结层数（FIFO顺序）
     *
     * @param entity 目标实体
     * @return true表示成功移除，false表示无层数可移除
     */
    public static boolean removeTangle(LivingEntity entity) {
        return StackableArmorAPI.removeLayer(entity, CONFIG);
    }

    /**
     * 清除所有缠结层数
     * <p>
     * 移除所有缠结层数并删除伤害减免属性
     *
     * @param entity 目标实体
     */
    public static void clearTangles(LivingEntity entity) {
        StackableArmorAPI.clearAllLayers(entity, CONFIG);
    }

    /**
     * 获取当前缠结数量
     *
     * @param entity 目标实体
     * @return 当前缠结层数
     */
    public static int getTangleCount(LivingEntity entity) {
        return StackableArmorAPI.getLayerCount(entity, CONFIG);
    }

    /**
     * 获取剩余时间（秒）
     * <p>
     * 返回最早过期的缠结距离当前时间的剩余秒数
     * 如果没有任何缠结，返回0
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
                    new WovenMailSyncPacket(entity.getUUID(), count, earliestExpiry, remainingSeconds));
        }
    }

    /**
     * 玩家拾取经验球事件处理
     * 监听经验球拾取，有概率生成缠结
     */
    @SubscribeEvent
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        MobEffectInstance effect = player.getEffect(FELEffects.WOVEN_MAIL);
        if (effect == null) return;

        // 概率：基础25%，每级+5%
        float chance = 0.25f + (effect.getAmplifier() * 0.05f);
        if (player.getRandom().nextFloat() < chance) {
            addTangle(player);
        }
    }
}