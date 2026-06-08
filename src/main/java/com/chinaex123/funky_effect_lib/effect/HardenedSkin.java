package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

/** 硬化皮肤：增加生物的护甲值 **/
public class HardenedSkin extends MobEffect {

    private static final String BASE_ATTACK_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("hardened_skin".getBytes()).toString();

    public static final double BASE_ATTACK_SPEED_MODIFIER = 2; // 增加的护甲值

    public HardenedSkin(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        this.addAttributeModifier(
                Attributes.ARMOR,
                BASE_ATTACK_SPEED_MODIFIER_STRING,
                BASE_ATTACK_SPEED_MODIFIER,
                AttributeModifier.Operation.ADDITION
        );
    }
}
