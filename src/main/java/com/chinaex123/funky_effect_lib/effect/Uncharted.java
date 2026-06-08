package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/** ???：增加战斗属性和行动能力 **/
public class Uncharted extends MobEffect {

    private static final String ARMOR_MODIFIER_STRING = UUID.nameUUIDFromBytes("uncharted_armor".getBytes()).toString();
    private static final String ATTACK_DAMAGE_MODIFIER_STRING = UUID.nameUUIDFromBytes("uncharted_attack_damage".getBytes()).toString();
    private static final String ATTACK_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("uncharted_attack_speed".getBytes()).toString();
    private static final String MOVEMENT_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("uncharted_movement_speed".getBytes()).toString();
    private static final String MAX_HEALTH_MODIFIER_STRING = UUID.nameUUIDFromBytes("uncharted_max_health".getBytes()).toString();
    private static final String LUCK_MODIFIER_STRING = UUID.nameUUIDFromBytes("uncharted_luck".getBytes()).toString();

    private static final double ARMOR_BOOST_PER_LEVEL = 0.15; // 护甲值
    private static final double ATTACK_DAMAGE_BOOST_PER_LEVEL = 0.15; // 攻击力
    private static final double ATTACK_SPEED_BOOST_PER_LEVEL = 0.10; // 攻击速度
    private static final double MOVEMENT_SPEED_BOOST_PER_LEVEL = 0.10; // 移动速度
    private static final double MAX_HEALTH_BOOST_PER_LEVEL = 4.0; // 点生命值
    private static final double LUCK_BOOST_PER_LEVEL = 2.0; // 幸运值

    public Uncharted(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        // 护甲值
        this.addAttributeModifier(
                Attributes.ARMOR,
                ARMOR_MODIFIER_STRING,
                ARMOR_BOOST_PER_LEVEL,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        // 攻击力
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER_STRING,
                ATTACK_DAMAGE_BOOST_PER_LEVEL,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        // 攻击速度
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_MODIFIER_STRING,
                ATTACK_SPEED_BOOST_PER_LEVEL,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        // 移动速度
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_MODIFIER_STRING,
                MOVEMENT_SPEED_BOOST_PER_LEVEL,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        // 最大生命值
        this.addAttributeModifier(
                Attributes.MAX_HEALTH,
                MAX_HEALTH_MODIFIER_STRING,
                MAX_HEALTH_BOOST_PER_LEVEL,
                AttributeModifier.Operation.ADDITION
        );

        // 幸运值
        this.addAttributeModifier(
                Attributes.LUCK,
                LUCK_MODIFIER_STRING,
                LUCK_BOOST_PER_LEVEL,
                AttributeModifier.Operation.ADDITION
        );
    }
}