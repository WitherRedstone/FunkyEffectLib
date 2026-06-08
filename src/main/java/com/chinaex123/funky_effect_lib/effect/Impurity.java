package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** 污秽：减少攻击力和护甲值 **/
public class Impurity extends MobEffect {

    private static final ResourceLocation ARMOR_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "impurity_armor");
    private static final ResourceLocation SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "impurity_speed");

    private static final float ARMOR_REDUCTION = -0.15f; // 减少护甲值
    private static final float SPEED_REDUCTION = -0.15f; // 减少移动速度

    public Impurity(int color) {
        super(MobEffectCategory.HARMFUL, color);

        this.addAttributeModifier(
                Attributes.ARMOR,
                ARMOR_MODIFIER,
                ARMOR_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER,
                SPEED_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }
}
