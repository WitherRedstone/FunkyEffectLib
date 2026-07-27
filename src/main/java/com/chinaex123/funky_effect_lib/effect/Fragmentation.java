package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * 碎裂：减少战斗属性和行动能力
 * <p>
 * 机制：
 * <ol>
 *   <li>每级减少15%护甲值</li>
 *   <li>每级减少15%攻击力</li>
 *   <li>每级减少10%攻击速度</li>
 *   <li>每级减少10%移动速度</li>
 *   <li>每级减少4点最大生命值</li>
 *   <li>每级减少2点幸运值</li>
 * </ol>
 */
public class Fragmentation extends MobEffect {

    private static final String FRAGMENTATION_MODIFIER_STRING = UUID.nameUUIDFromBytes("fragmentation_armor".getBytes()).toString();
    private static final String FRAGMENTATION_ATTACK_DAMAGE_MODIFIER_STRING = UUID.nameUUIDFromBytes("fragmentation_attack_damage".getBytes()).toString();
    private static final String FRAGMENTATION_ATTACK_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("fragmentation_attack_speed".getBytes()).toString();
    private static final String FRAGMENTATION_MOVEMENT_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("fragmentation_movement_speed".getBytes()).toString();
    private static final String FRAGMENTATION_MAX_HEALTH_MODIFIER_STRING = UUID.nameUUIDFromBytes("fragmentation_max_health".getBytes()).toString();
    private static final String FRAGMENTATION_LUCK_MODIFIER_STRING = UUID.nameUUIDFromBytes("fragmentation_luck".getBytes()).toString();

    /** 每级护甲减少 **/
    private static final double ARMOR_REDUCTION_PER_LEVEL = -0.15;
    /** 每级攻击力减少 **/
    private static final double ATTACK_DAMAGE_REDUCTION_PER_LEVEL = -0.15;
    /** 每级攻击速度减少 **/
    private static final double ATTACK_SPEED_REDUCTION_PER_LEVEL = -0.10;
    /** 每级移动速度减少 **/
    private static final double MOVEMENT_SPEED_REDUCTION_PER_LEVEL = -0.10;
    /** 每级最大生命值减少 **/
    private static final double MAX_HEALTH_REDUCTION_PER_LEVEL = -4.0;
    /** 每级幸运值减少 **/
    private static final double LUCK_REDUCTION_PER_LEVEL = -2.0;

    public Fragmentation(int color) {
        super(MobEffectCategory.HARMFUL, color);

        // 护甲值减少（乘法基础）
        this.addAttributeModifier(
                Attributes.ARMOR,
                FRAGMENTATION_MODIFIER_STRING,
                ARMOR_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        // 攻击力减少（乘法基础）
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                FRAGMENTATION_ATTACK_DAMAGE_MODIFIER_STRING,
                ATTACK_DAMAGE_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        // 攻击速度减少（乘法基础）
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                FRAGMENTATION_ATTACK_SPEED_MODIFIER_STRING,
                ATTACK_SPEED_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        // 移动速度减少（乘法基础）
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                FRAGMENTATION_MOVEMENT_SPEED_MODIFIER_STRING,
                MOVEMENT_SPEED_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        // 最大生命值减少（加法）
        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                FRAGMENTATION_MAX_HEALTH_MODIFIER_STRING,
                MAX_HEALTH_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADDITION
        );

        // 幸运值减少（加法）
        this.addAttributeModifier(
                Attributes.LUCK,
                FRAGMENTATION_LUCK_MODIFIER_STRING,
                LUCK_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADDITION
        );
    }
}