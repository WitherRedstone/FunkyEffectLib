//package com.chinaex123.funky_effect_lib.effect;
//
//import com.chinaex123.funky_effect_lib.FunkyEffectLib;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.effect.MobEffect;
//import net.minecraft.world.effect.MobEffectCategory;
//import net.minecraft.world.entity.ai.attributes.AttributeModifier;
//import net.minecraft.world.entity.ai.attributes.Attributes;
//
///** 重力倒转：使自身重力反转，视野颠倒，向上坠落 **/
//public class FlipGravity extends MobEffect {
//
//    private static final ResourceLocation GRAVITY_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "flip_gravity");
//    private static final double GRAVITY_MULTIPLIER = -0.16;
//
//    public FlipGravity(int color) {
//        super(MobEffectCategory.HARMFUL, color);
//        this.addAttributeModifier(
//            Attributes.GRAVITY,
//            GRAVITY_MODIFIER,
//            GRAVITY_MULTIPLIER,
//            AttributeModifier.Operation.ADD_VALUE
//        );
//    }
//}