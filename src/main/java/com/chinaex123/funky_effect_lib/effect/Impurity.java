package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/** 污秽：减少攻击力和护甲值 **/
public class Impurity extends MobEffect {

    private static final String IMPURITY_ARMOR_MODIFIER_STRING = UUID.nameUUIDFromBytes("impurity_armor".getBytes()).toString();
    private static final String IMPURITY_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("impurity_speed".getBytes()).toString();

    private static final float ARMOR_REDUCTION = -0.15f; // 减少护甲值
    private static final float SPEED_REDUCTION = -0.15f; // 减少移动速度

    public Impurity(int color) {
        super(MobEffectCategory.HARMFUL, color);

        this.addAttributeModifier(
                Attributes.ARMOR,
                IMPURITY_ARMOR_MODIFIER_STRING,
                ARMOR_REDUCTION,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                IMPURITY_SPEED_MODIFIER_STRING,
                SPEED_REDUCTION,
                AttributeModifier.Operation.MULTIPLY_BASE
        );
    }
}
