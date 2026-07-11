package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

/** 致盲：生物将无法攻击和移动 **/
public class Blind extends MobEffect {

    private static final ResourceLocation SPEED_MODIFIER_STRING = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "blind_speed");

    private static final float SPEED_REDUCTION = -12.0f; // 完全阻止移动

    public Blind(int color) {
        super(MobEffectCategory.HARMFUL, color);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_STRING,
                SPEED_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 强制停止移动
        entity.setDeltaMovement(0, entity.getDeltaMovement().y(), 0);
        entity.setJumping(false);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /** 检查实体是否处于致盲状态 **/
    public static boolean isBlind(LivingEntity entity) {
        return entity.hasEffect(FELEffects.BLIND);
    }
}