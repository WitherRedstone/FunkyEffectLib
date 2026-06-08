package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/** 治愈：立刻恢复一定的生命值 **/
public class Cure extends InstantenousMobEffect {

    private static final float BASE_HEAL_AMOUNT = 4.0f; // 基础恢复量
    private static final float HEAL_PER_LEVEL = 2.0f; // 每级额外恢复量

    public Cure(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    /**
     * 瞬间治疗的核心方法 - 效果应用时立即执行
     */
    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            float healAmount = BASE_HEAL_AMOUNT + (HEAL_PER_LEVEL * amplifier);
            entity.heal(healAmount);
        }
        return true;
    }
}