package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * 覆盖护盾状态效果类
 * <p>
 * 该效果为增益效果，为玩家提供额外的黄色心（吸收生命值）保护。
 * 黄色心会优先承受伤害，当黄色心耗尽时效果自动移除。
 * <p>
 * 机制说明：
 * <ul>
 *   <li>每级效果给予2个黄色心（4点生命值）</li>
 *   <li>黄色心优先受到伤害（原版吸收机制）</li>
 *   <li>当黄色心归零时，效果自动消失</li>
 *   <li>使用原版吸收生命值机制，自动显示黄色心</li>
 * </ul>
 */
public class Overshield extends MobEffect {

    /** 每级效果提供的吸收生命值（2个心 = 4点生命值） */
    private static final float ABSORPTION_PER_LEVEL = 4.0F;

    /**
     * 构造覆盖护盾效果
     *
     * @param color 效果颜色值（用于效果图标和粒子颜色）
     */
    public Overshield(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    /**
     * 效果开始应用时调用的方法
     * <p>
     * 在效果被添加时，为实体增加对应的吸收生命值
     *
     * @param entity 获得该效果的实体
     * @param amplifier 效果等级（从0开始）
     */
    @Override
    public void onEffectStarted(@NotNull LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);
        float absorptionToAdd = ABSORPTION_PER_LEVEL * (amplifier + 1);
        entity.setAbsorptionAmount(Math.max(entity.getAbsorptionAmount(), absorptionToAdd));
    }

    /**
     * 每帧应用效果时调用的方法
     * <p>
     * 检查实体的吸收生命值是否归零，如果归零则返回false移除该效果
     *
     * @param entity 拥有该效果的实体
     * @param amplifier 效果等级（从0开始）
     * @return 如果吸收生命值大于0返回true，否则返回false
     */
    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return entity.getAbsorptionAmount() > 0.0F || entity.level().isClientSide();
    }

    /**
     * 判断效果是否应在每刻执行更新
     * <p>
     * 始终返回true，使效果持续生效并检测吸收生命值是否耗尽
     *
     * @param duration 剩余持续时间（刻）
     * @param amplifier 效果等级
     * @return 始终返回true
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}