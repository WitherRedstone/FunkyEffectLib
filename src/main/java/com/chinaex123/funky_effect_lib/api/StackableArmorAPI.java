package com.chinaex123.funky_effect_lib.api;

import com.chinaex123.funky_effect_lib.init.FELAttributes;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 可堆叠护甲API
 * <p>
 * 该API提供了一套完整的层数管理系统，用于实现可堆叠的护甲效果。
 * 每个层数都有独立的过期时间，层数越多提供的伤害减免越高。
 * <p>
 * 主要功能：
 * <ul>
 *   <li>层数管理：添加、移除、清除层数</li>
 *   <li>过期机制：每层独立计时，到期后自动移除</li>
 *   <li>伤害减免：根据层数动态计算并应用伤害减免属性</li>
 *   <li>数据持久化：层数数据保存在实体的持久数据中</li>
 *   <li>效果联动：与MobEffect状态效果协同工作</li>
 * </ul>
 * <p>
 * 使用流程：
 * <ol>
 *   <li>通过 {@link #addLayer(LivingEntity, ArmorConfig)} 添加层数</li>
 *   <li>在实体的tick中调用 {@link #cleanExpiredLayers(LivingEntity, ArmorConfig)} 清理过期层数</li>
 *   <li>通过 {@link #getLayerCount(LivingEntity, ArmorConfig)} 获取当前层数</li>
 *   <li>通过 {@link #getRemainingSeconds(LivingEntity, ArmorConfig)} 获取剩余时间</li>
 * </ol>
 */
public class StackableArmorAPI {

    /**
     * 护甲配置记录
     * <p>
     * 包含所有必要的配置参数，用于管理特定类型的可堆叠护甲
     *
     * @param expiryKey 过期时间数组在持久数据中的键名
     * @param damageReductionPerLayer 每层提供的伤害减免值
     * @param maxDamageReduction 最大伤害减免值（上限）
     * @param maxLayers 最大层数限制
     * @param layerDuration 每层的持续时间（刻）
     * @param effectType 关联的状态效果类型（Supplier形式延迟获取，避免静态初始化顺序问题）
     * @param syncFunction 同步函数（实体+过期时间数组 → 发送同步包）
     */
    public record ArmorConfig(
            ResourceLocation expiryKey,
            ResourceLocation modifierId,
            float damageReductionPerLayer,
            float maxDamageReduction,
            int maxLayers,
            int layerDuration,
            Supplier<Holder<MobEffect>> effectType,
            BiConsumer<LivingEntity, long[]> syncFunction
    ) {}

    /** 网络通道名称（用于同步数据包） */
    private static final String NETWORK_CHANNEL = "funky_effect_lib";

    /**
     * 获取实体的过期时间数组
     *
     * @param entity 目标实体
     * @param config 护甲配置
     * @return 过期时间数组（按添加顺序排列，最早的在前）
     */
    public static long[] getExpiryArray(LivingEntity entity, ArmorConfig config) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = config.expiryKey.toString();

        if (!persistentData.contains(key)) {
            return new long[0];
        }
        return persistentData.getLongArray(key);
    }

    /**
     * 保存过期时间数组到实体的持久数据
     *
     * @param entity 目标实体
     * @param config 护甲配置
     * @param expiryArray 要保存的过期时间数组
     */
    public static void saveExpiryArray(LivingEntity entity, ArmorConfig config, long[] expiryArray) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = config.expiryKey.toString();

        if (expiryArray.length == 0) {
            persistentData.remove(key);
        } else {
            persistentData.putLongArray(key, expiryArray);
        }
    }

    /**
     * 获取当前层数
     *
     * @param entity 目标实体
     * @param config 护甲配置
     * @return 当前层数
     */
    public static int getLayerCount(LivingEntity entity, ArmorConfig config) {
        return getExpiryArray(entity, config).length;
    }

    /**
     * 添加一层护甲
     * <p>
     * 如果层数已达到上限，则返回false
     * 新层数的过期时间 = 当前时间 + (层数索引 + 1) * 每层持续时间
     * 这样确保了最早添加的层数最先过期（FIFO）
     *
     * @param entity 目标实体
     * @param config 护甲配置
     * @return true表示成功添加，false表示已达到上限
     */
    public static boolean addLayer(LivingEntity entity, ArmorConfig config) {
        int currentCount = getLayerCount(entity, config);
        if (currentCount >= config.maxLayers) {
            return false;
        }

        long[] current = getExpiryArray(entity, config);
        long currentTime = entity.level().getGameTime();

        // 创建新数组，并计算每个层数的过期时间
        long[] newArray = new long[current.length + 1];
        for (int i = 0; i < current.length; i++) {
            newArray[i] = currentTime + ((long) (i + 1) * config.layerDuration);
        }
        newArray[current.length] = currentTime + ((long) (current.length + 1) * config.layerDuration);

        saveExpiryArray(entity, config, newArray);
        updateDamageReductionAttribute(entity, config);
        syncToClient(entity, config);
        return true;
    }

    /**
     * 移除最早的一层护甲（FIFO）
     * <p>
     * 移除最早的层数（索引0），其他层数向前移位
     *
     * @param entity 目标实体
     * @param config 护甲配置
     * @return true表示成功移除，false表示无层数可移除
     */
    public static boolean removeLayer(LivingEntity entity, ArmorConfig config) {
        long[] current = getExpiryArray(entity, config);
        if (current.length == 0) {
            return false;
        }

        // 移除第一个元素（最早的层数）
        long[] newArray = new long[current.length - 1];
        System.arraycopy(current, 1, newArray, 0, current.length - 1);

        saveExpiryArray(entity, config, newArray);
        updateDamageReductionAttribute(entity, config);
        syncToClient(entity, config);
        return true;
    }

    /**
     * 清除所有层数
     *
     * @param entity 目标实体
     * @param config 护甲配置
     */
    public static void clearAllLayers(LivingEntity entity, ArmorConfig config) {
        saveExpiryArray(entity, config, new long[0]);
        removeDamageReductionAttribute(entity, config);
        syncToClient(entity, config);
    }

    /**
     * 清理已过期的层数
     * <p>
     * 检查过期时间数组，移除所有已到期的层数
     * 由于数组按添加顺序排序（最早的在前），只需从头部开始检查
     *
     * @param entity 目标实体
     * @param config 护甲配置
     */
    public static void cleanExpiredLayers(LivingEntity entity, ArmorConfig config) {
        long currentTime = entity.level().getGameTime();
        long[] current = getExpiryArray(entity, config);

        if (current.length == 0) {
            return;
        }

        // 统计已过期的层数（从头部开始）
        int expiredLayers = 0;
        for (long expiry : current) {
            if (currentTime >= expiry) {
                expiredLayers++;
            } else {
                break;
            }
        }

        if (expiredLayers > 0) {
            if (expiredLayers >= current.length) {
                // 所有层数都已过期，完全清除
                clearAllLayers(entity, config);
            } else {
                // 移除已过期的层数，保留未过期的
                long[] newArray = new long[current.length - expiredLayers];
                System.arraycopy(current, expiredLayers, newArray, 0, current.length - expiredLayers);
                saveExpiryArray(entity, config, newArray);
                updateDamageReductionAttribute(entity, config);
                syncToClient(entity, config);
            }
        }
    }

    /**
     * 获取剩余时间（秒）
     * <p>
     * 返回最早过期的层数距离当前时间的剩余秒数
     * 如果没有任何层数，返回0
     *
     * @param entity 目标实体
     * @param config 护甲配置
     * @return 剩余秒数（向上取整）
     */
    public static int getRemainingSeconds(LivingEntity entity, ArmorConfig config) {
        long[] current = getExpiryArray(entity, config);
        if (current.length == 0) {
            return 0;
        }

        long currentTime = entity.level().getGameTime();
        long earliestExpiry = current[0];  // 最早的过期时间
        long remainingTicks = earliestExpiry - currentTime;
        return (int) Math.max(0, Math.ceil(remainingTicks / 20.0));
    }

    /**
     * 更新伤害减免属性
     * <p>
     * 根据当前层数计算伤害减免值，并应用到实体的属性上
     *
     * @param entity 目标实体
     * @param config 护甲配置
     */
    public static void updateDamageReductionAttribute(LivingEntity entity, ArmorConfig config) {
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION);
        if (attribute == null) {
            return;
        }

        int currentLayers = getLayerCount(entity, config);
        double damageReduction = 0.0;

        // 计算伤害减免值（应用上限）
        if (currentLayers > 0) {
            damageReduction = Math.min(currentLayers * config.damageReductionPerLayer, config.maxDamageReduction);
        }

        // 移除旧的属性修改器
        attribute.removeModifier(config.modifierId());

        // 如果有伤害减免值，添加新的属性修改器
        if (damageReduction > 0.0) {
            AttributeModifier modifier = new AttributeModifier(
                    config.modifierId(),
                    damageReduction,
                    AttributeModifier.Operation.ADD_VALUE
            );
            attribute.addPermanentModifier(modifier);
        }
    }

    /**
     * 移除伤害减免属性修改器
     *
     * @param entity 目标实体
     * @param config 护甲配置
     */
    public static void removeDamageReductionAttribute(LivingEntity entity, ArmorConfig config) {
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION);
        if (attribute != null) {
            attribute.removeModifier(config.modifierId());
        }
    }

    /**
     * 同步数据到客户端
     * <p>
     * 调用配置中的同步函数发送数据
     *
     * @param entity 目标实体
     * @param config 护甲配置
     */
    public static void syncToClient(LivingEntity entity, ArmorConfig config) {
        if (config.syncFunction != null) {
            config.syncFunction.accept(entity, getExpiryArray(entity, config));
        }
    }

    /**
     * 检查实体是否拥有关联的状态效果
     *
     * @param entity 目标实体
     * @param config 护甲配置
     * @return true表示拥有该效果，false表示没有
     */
    public static boolean hasEffect(LivingEntity entity, ArmorConfig config) {
        return config.effectType != null && entity.hasEffect(config.effectType.get());
    }

    /**
     * 状态效果获得时的回调
     * <p>
     * 当前实现为空，可根据需要在效果获得时执行额外的初始化逻辑
     *
     * @param entity 目标实体
     * @param config 护甲配置
     */
    public static void onEffectGained(LivingEntity entity, ArmorConfig config) {
        // 可以在此添加效果获得时的额外逻辑
    }

    /**
     * 状态效果失去时的回调
     * <p>
     * 当关联的效果失去时，清除所有护甲层数
     *
     * @param entity 目标实体
     * @param config 护甲配置
     */
    public static void onEffectLost(LivingEntity entity, ArmorConfig config) {
        clearAllLayers(entity, config);
    }

    /**
     * 状态效果每刻更新时的回调
     * <p>
     * 在效果生效期间，每刻检查并清理过期的层数
     *
     * @param entity 目标实体
     * @param config 护甲配置
     */
    public static void onEffectTick(LivingEntity entity, ArmorConfig config) {
        cleanExpiredLayers(entity, config);
    }
}