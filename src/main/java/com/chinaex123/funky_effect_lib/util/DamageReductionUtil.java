package com.chinaex123.funky_effect_lib.util;

import com.chinaex123.funky_effect_lib.init.FELAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

/**
 * 伤害减免工具类
 * <p>
 * 提供便捷的伤害减免属性操作方法
 */
public class DamageReductionUtil {

    /** 伤害减免修饰符UUID */
    private static final UUID DAMAGE_REDUCTION_UUID = UUID.nameUUIDFromBytes("damage_reduction".getBytes());

    /**
     * 为实体添加伤害减免
     *
     * @param entity 目标实体
     * @param reduction 伤害减免百分比 (0.0-1.0, 即0%-100%)
     * @param name 修饰符名称
     */
    public static void addDamageReduction(LivingEntity entity, double reduction, String name) {
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION.get());
        if (attribute != null) {
            // 确保减免在有效范围内
            reduction = Math.max(0.0, Math.min(1.0, reduction));

            // 移除同名的旧修饰符
            attribute.removeModifier(DAMAGE_REDUCTION_UUID);

            // 添加新的修饰符
            AttributeModifier modifier = new AttributeModifier(
                    DAMAGE_REDUCTION_UUID,
                    name,
                    reduction,
                    AttributeModifier.Operation.ADDITION
            );
            attribute.addPermanentModifier(modifier);
        }
    }

    /**
     * 为实体添加伤害减免（使用默认名称）
     *
     * @param entity 目标实体
     * @param reduction 伤害减免百分比 (0.0-1.0, 即0%-100%)
     */
    public static void addDamageReduction(LivingEntity entity, double reduction) {
        addDamageReduction(entity, reduction, "damage_reduction");
    }

    /**
     * 移除实体的伤害减免
     *
     * @param entity 目标实体
     */
    public static void removeDamageReduction(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION.get());
        if (attribute != null) {
            attribute.removeModifier(DAMAGE_REDUCTION_UUID);
        }
    }

    /**
     * 获取实体的当前伤害减免百分比
     *
     * @param entity 目标实体
     * @return 伤害减免百分比 (0.0-1.0, 即0%-100%)
     */
    public static double getDamageReduction(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION.get());
        if (attribute != null) {
            return Math.max(0.0, Math.min(1.0, attribute.getValue()));
        }
        return 0.0;
    }

    /**
     * 应用伤害减免到实际伤害值
     *
     * @param entity 目标实体
     * @param originalDamage 原始伤害
     * @return 减免后的伤害
     */
    public static float applyDamageReduction(LivingEntity entity, float originalDamage) {
        double reduction = getDamageReduction(entity);
        return (float) (originalDamage * (1.0 - reduction));
    }

    /**
     * 设置实体的伤害减免（覆盖现有值）
     *
     * @param entity 目标实体
     * @param reduction 伤害减免百分比 (0.0-1.0, 即0%-100%)
     */
    public static void setDamageReduction(LivingEntity entity, double reduction) {
        addDamageReduction(entity, reduction);
    }
}