package com.chinaex123.funky_effect_lib.api.event.Scorch;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

/**
 * 灼烧公共 API 类
 * <p>
 * 提供灼烧层数的管理、存储和同步功能
 * 灼烧层数存储在实体的持久化数据中，支持服务端与客户端同步
 */
public class ScorchAPI {

    public static final ResourceLocation SCORCH_STACKS_KEY = FunkyEffectLib.id("scorch_stacks");

    /** 最大灼烧层数 **/
    public static final int MAX_SCORCH_STACKS = 100;
    /** 默认点燃触发间隔（每多少层触发一次） **/
    public static final int DEFAULT_TRIGGER_INTERVAL = 10;
    /** 最大点燃等级 **/
    public static final int MAX_IGNITE_LEVEL = 10;
    /** 默认基础伤害 **/
    public static final float DEFAULT_BASE_DAMAGE = 2.0f;
    /** 默认伤害增长率（每增加一个点燃等级增加的伤害） **/
    public static final float DEFAULT_DAMAGE_GROWTH = 1.0f;

    /**
     * 点燃配置记录类
     * 用于自定义点燃触发间隔和伤害计算
     */
    public record IgniteConfig(int triggerInterval, float baseDamage, float damageGrowth) {
        /** 创建默认配置 **/
        public static IgniteConfig createDefault() {
            return new IgniteConfig(DEFAULT_TRIGGER_INTERVAL, DEFAULT_BASE_DAMAGE, DEFAULT_DAMAGE_GROWTH);
        }
    }

    /**
     * 获取实体的当前灼烧层数
     *
     * @param entity 目标实体
     * @return 当前灼烧层数
     */
    public static int getScorchStacks(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = SCORCH_STACKS_KEY.toString();
        return persistentData.getInt(key);
    }

    /**
     * 设置实体的灼烧层数（内部方法）
     *
     * @param entity 目标实体
     * @param stacks 要设置的层数
     */
    public static void setScorchStacksInternal(LivingEntity entity, int stacks) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = SCORCH_STACKS_KEY.toString();
        persistentData.putInt(key, Math.min(stacks, MAX_SCORCH_STACKS));
    }

    /**
     * 为实体增加指定数量的灼烧层数（使用默认配置）
     *
     * @param entity 目标实体
     * @param amount 增加的层数（正数）
     */
    public static void addScorchStacks(LivingEntity entity, int amount) {
        addScorchStacks(entity, amount, IgniteConfig.createDefault());
    }

    /**
     * 为实体增加指定数量的灼烧层数（使用自定义配置）
     *
     * @param entity 目标实体
     * @param amount 增加的层数（正数）
     * @param config 点燃配置
     */
    public static void addScorchStacks(LivingEntity entity, int amount, IgniteConfig config) {
        int current = getScorchStacks(entity);
        int newStacks = Math.min(current + amount, MAX_SCORCH_STACKS);
        setScorchStacksInternal(entity, newStacks);

        if (amount > 0) {
            // 发布灼烧接收事件
            MinecraftForge.EVENT_BUS.post(new ScorchReceivedEvent(entity, amount, current, newStacks));
            // 触发点燃
            triggerIgniteForStacks(entity, current, newStacks, config);
        }
    }

    /**
     * 设置实体的灼烧层数为指定值（使用默认配置）
     *
     * @param entity 目标实体
     * @param stacks 要设置的层数
     */
    public static void setScorchStacks(LivingEntity entity, int stacks) {
        setScorchStacks(entity, stacks, IgniteConfig.createDefault());
    }

    /**
     * 设置实体的灼烧层数为指定值（使用自定义配置）
     *
     * @param entity 目标实体
     * @param stacks 要设置的层数
     * @param config 点燃配置
     */
    public static void setScorchStacks(LivingEntity entity, int stacks, IgniteConfig config) {
        int current = getScorchStacks(entity);
        int newStacks = Math.min(stacks, MAX_SCORCH_STACKS);
        setScorchStacksInternal(entity, newStacks);

        if (newStacks != current) {
            int amountAdded = newStacks - current;
            // 发布灼烧接收事件
            MinecraftForge.EVENT_BUS.post(new ScorchReceivedEvent(entity, amountAdded, current, newStacks));
            // 触发点燃
            triggerIgniteForStacks(entity, current, newStacks, config);
        }
    }

    /**
     * 清除实体的所有灼烧层数
     *
     * @param entity 目标实体
     */
    public static void clearScorchStacks(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        persistentData.remove(SCORCH_STACKS_KEY.toString());
    }

    /**
     * 根据层数变化触发点燃
     *
     * @param entity 目标实体
     * @param oldStacks 旧层数
     * @param newStacks 新层数
     * @param config 点燃配置
     */
    public static void triggerIgniteForStacks(LivingEntity entity, int oldStacks, int newStacks, IgniteConfig config) {
        if (entity.level().isClientSide()) return;

        // 计算跨越了多少个触发间隔
        int oldTrigger = oldStacks / config.triggerInterval;
        int newTrigger = newStacks / config.triggerInterval;

        for (int i = oldTrigger + 1; i <= newTrigger; i++) {
            int igniteLevel = Math.min(i - 1, MAX_IGNITE_LEVEL - 1);
            triggerIgnite(entity, igniteLevel, config);
        }
    }

    /**
     * 触发点燃效果
     *
     * @param entity 目标实体
     * @param level 点燃等级
     * @param config 点燃配置
     */
    public static void triggerIgnite(LivingEntity entity, int level, IgniteConfig config) {
        if (entity.level().isClientSide()) return;

        // 计算伤害：基础伤害 + 等级 * 增长率
        float damage = config.baseDamage + level * config.damageGrowth;
        entity.hurt(entity.damageSources().onFire(), damage);

        // 添加点燃效果
        entity.addEffect(new MobEffectInstance(FELEffects.IGNITE.get(), 1, Math.max(level, 0)));

        // 让生物着火
        entity.setRemainingFireTicks(100);
    }
}