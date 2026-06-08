package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

/** 狂怒：增加攻击速度和伤害 **/
public class Rage extends MobEffect {

    private static final ResourceLocation ATTACK_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "amplified_speed");
    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "amplified_damage");

    private static final float BASE_ATTACK_SPEED_MODIFIER = 0.1f; // 基础攻击速度增加
    private static final float BASE_ATTACK_DAMAGE_MODIFIER = 0.15f; // 基础攻击力增加

    public Rage(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_MODIFIER,
                BASE_ATTACK_SPEED_MODIFIER,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER,
                BASE_ATTACK_DAMAGE_MODIFIER,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
