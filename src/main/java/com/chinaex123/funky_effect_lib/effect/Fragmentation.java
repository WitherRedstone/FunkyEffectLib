package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

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

    private static final ResourceLocation ARMOR_MODIFIER = ResourceLocation.withDefaultNamespace("fragmentation_armor");
    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER = ResourceLocation.withDefaultNamespace("fragmentation_attack_damage");
    private static final ResourceLocation ATTACK_SPEED_MODIFIER = ResourceLocation.withDefaultNamespace("fragmentation_attack_speed");
    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER = ResourceLocation.withDefaultNamespace("fragmentation_movement_speed");
    private static final ResourceLocation MAX_HEALTH_MODIFIER = ResourceLocation.withDefaultNamespace("fragmentation_max_health");
    private static final ResourceLocation LUCK_MODIFIER = ResourceLocation.withDefaultNamespace("fragmentation_luck");

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
                ARMOR_MODIFIER,
                ARMOR_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        // 攻击力减少（乘法基础）
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER,
                ATTACK_DAMAGE_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        // 攻击速度减少（乘法基础）
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_MODIFIER,
                ATTACK_SPEED_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        // 移动速度减少（乘法基础）
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_MODIFIER,
                MOVEMENT_SPEED_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        // 最大生命值减少（加法）
        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                MAX_HEALTH_MODIFIER,
                MAX_HEALTH_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_VALUE
        );

        // 幸运值减少（加法）
        this.addAttributeModifier(
                Attributes.LUCK,
                LUCK_MODIFIER,
                LUCK_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}