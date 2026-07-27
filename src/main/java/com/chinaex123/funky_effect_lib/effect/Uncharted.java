package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * ???：增加战斗属性和行动能力
 * <p>
 * 机制：
 * <ol>
 *   <li>每级增加15%护甲值</li>
 *   <li>每级增加15%攻击力</li>
 *   <li>每级增加10%攻击速度</li>
 *   <li>每级增加10%移动速度</li>
 *   <li>每级增加4点最大生命值</li>
 *   <li>每级增加2点幸运值</li>
 * </ol>
 */
public class Uncharted extends MobEffect {

    private static final ResourceLocation ARMOR_MODIFIER = ResourceLocation.withDefaultNamespace("uncharted_armor");
    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER = ResourceLocation.withDefaultNamespace("uncharted_attack_damage");
    private static final ResourceLocation ATTACK_SPEED_MODIFIER = ResourceLocation.withDefaultNamespace("uncharted_attack_speed");
    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER = ResourceLocation.withDefaultNamespace("uncharted_movement_speed");
    private static final ResourceLocation MAX_HEALTH_MODIFIER = ResourceLocation.withDefaultNamespace("uncharted_max_health");
    private static final ResourceLocation LUCK_MODIFIER = ResourceLocation.withDefaultNamespace("uncharted_luck");

    /** 每级护甲增加 **/
    private static final double ARMOR_BOOST_PER_LEVEL = 0.15;
    /** 每级攻击力增加 **/
    private static final double ATTACK_DAMAGE_BOOST_PER_LEVEL = 0.15;
    /** 每级攻击速度增加 **/
    private static final double ATTACK_SPEED_BOOST_PER_LEVEL = 0.10;
    /** 每级移动速度增加 **/
    private static final double MOVEMENT_SPEED_BOOST_PER_LEVEL = 0.10;
    /** 每级最大生命值增加 **/
    private static final double MAX_HEALTH_BOOST_PER_LEVEL = 4.0;
    /** 每级幸运值增加 **/
    private static final double LUCK_BOOST_PER_LEVEL = 2.0;

    public Uncharted(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        // 护甲值增加
        this.addAttributeModifier(
                Attributes.ARMOR,
                ARMOR_MODIFIER,
                ARMOR_BOOST_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        // 攻击力增加
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER,
                ATTACK_DAMAGE_BOOST_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        // 攻击速度增加
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_MODIFIER,
                ATTACK_SPEED_BOOST_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        // 移动速度增加
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_MODIFIER,
                MOVEMENT_SPEED_BOOST_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        // 最大生命值增加
        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                MAX_HEALTH_MODIFIER,
                MAX_HEALTH_BOOST_PER_LEVEL,
                AttributeModifier.Operation.ADD_VALUE
        );

        // 幸运值增加
        this.addAttributeModifier(
                Attributes.LUCK,
                LUCK_MODIFIER,
                LUCK_BOOST_PER_LEVEL,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}