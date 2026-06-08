package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** 狂怒：增加攻击速度和伤害 **/
public class Rage extends MobEffect {

    private static final String BASE_ATTACK_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("rage_speed".getBytes()).toString();
    private static final String BASE_ATTACK_DAMAGE_MODIFIER_STRING = UUID.nameUUIDFromBytes("rage_attack".getBytes()).toString();

    private static final float BASE_ATTACK_SPEED_MODIFIER = 0.1f; // 基础攻击速度增加
    private static final float BASE_ATTACK_DAMAGE_MODIFIER = 0.15f; // 基础攻击力增加

    public Rage(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                BASE_ATTACK_SPEED_MODIFIER_STRING,
                BASE_ATTACK_SPEED_MODIFIER,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                BASE_ATTACK_DAMAGE_MODIFIER_STRING,
                BASE_ATTACK_DAMAGE_MODIFIER,
                AttributeModifier.Operation.MULTIPLY_BASE
        );
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
