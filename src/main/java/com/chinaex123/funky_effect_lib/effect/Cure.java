package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * 治愈：立刻恢复一定的生命值（瞬间效果）
 * <p>
 * 机制：
 * <ol>
 *   <li>基础恢复量为4点生命值（2颗心）</li>
 *   <li>每级额外恢复2点生命值（1颗心）</li>
 *   <li>效果为瞬间生效，不持续</li>
 *   <li>仅在服务端执行</li>
 * </ol>
 */
public class Cure extends InstantenousMobEffect {

    /** 基础恢复量 **/
    private static final float BASE_HEAL_AMOUNT = 4.0f;
    /** 每级额外恢复量 **/
    private static final float HEAL_PER_LEVEL = 2.0f;

    public Cure(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 仅在服务端执行
        if (!entity.level().isClientSide()) {
            // 计算恢复量：基础 + 等级 × 每级加成
            float healAmount = BASE_HEAL_AMOUNT + (HEAL_PER_LEVEL * amplifier);
            // 恢复生命值
            entity.heal(healAmount);
        }
        return true;
    }
}