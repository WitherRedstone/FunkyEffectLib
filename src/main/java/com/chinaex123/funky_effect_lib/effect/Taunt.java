package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

/** 嘲讽：吸引附近的敌对生物 **/
public class Taunt extends MobEffect {

    private static final int BASE_TAUNT_RADIUS = 8; // 基础范围
    private static final int ADDITIONAL_RADIUS_PER_LEVEL = 4; // 每级额外范围
    private static final int MAX_TAUNT_RADIUS = 32; // 最大范围

    public Taunt(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            return true;
        }

        int tauntRadius = getTauntRadius(amplifier);
        AABB searchArea = new AABB(entity.blockPosition()).inflate(tauntRadius);

        entity.level().getEntitiesOfClass(Mob.class, searchArea, mob -> {
            if (mob == entity) return false;
            if (mob.isRemoved()) return false;
            if (mob.getType().getCategory() != MobCategory.MONSTER) return false;
            return mob.canAttack(entity);
        }).forEach(mob -> {
            mob.setTarget(entity);
        });

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    public static int getTauntRadius(int amplifier) {
        return Math.min(MAX_TAUNT_RADIUS, BASE_TAUNT_RADIUS + (amplifier * ADDITIONAL_RADIUS_PER_LEVEL));
    }
}