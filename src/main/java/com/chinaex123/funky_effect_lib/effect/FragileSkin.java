package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/** 脆弱皮肤：减少生物的护甲值 **/
public class FragileSkin extends MobEffect {

    private static final String ARMOR_MODIFIER_STRING = UUID.nameUUIDFromBytes("fragile_skin_armor".getBytes()).toString();

    public static final double BASE_ARMOR_REDUCTION = -2; // 减少的护甲值

    public FragileSkin(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        this.addAttributeModifier(
                Attributes.ARMOR,
                ARMOR_MODIFIER_STRING,
                BASE_ARMOR_REDUCTION,
                AttributeModifier.Operation.ADDITION
        );
    }
}