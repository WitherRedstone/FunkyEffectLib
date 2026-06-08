package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** 脆弱皮肤：减少生物的护甲值 **/
public class FragileSkin extends MobEffect {

    private static final ResourceLocation ATTACK_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "fragile_skin_armor");

    public static final double BASE_ATTACK_SPEED_MODIFIER = -2; // 减少的护甲值

    public FragileSkin(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        this.addAttributeModifier(
                Attributes.ARMOR,
                ATTACK_SPEED_MODIFIER,
                BASE_ATTACK_SPEED_MODIFIER,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}
