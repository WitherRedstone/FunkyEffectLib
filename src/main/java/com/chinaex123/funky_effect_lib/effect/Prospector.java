package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/** 勘探者：显示周围的矿石 **/
public class Prospector extends MobEffect {

    private static final int BASE_DETECTION_RADIUS = 8; // 基础检测半径
    private static final int ADDITIONAL_RADIUS_PER_LEVEL = 4; // 每个等级增加的检测半径

    public Prospector(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    public static int getDetectionRadius(int amplifier) {
        return Math.min(32, BASE_DETECTION_RADIUS + (amplifier * ADDITIONAL_RADIUS_PER_LEVEL));
    }
}