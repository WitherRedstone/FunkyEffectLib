package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * 寻宝者：显示周围的战利品宝箱（通过高亮渲染）
 * <p>
 * 机制：
 * <ol>
 *   <li>基础检测半径为8格</li>
 *   <li>每级增加4格检测半径</li>
 *   <li>最大检测半径为32格</li>
 *   <li>效果本身不执行逻辑，由客户端渲染类处理显示</li>
 * </ol>
 */
public class TreasureFinder extends MobEffect {

    /** 基础检测半径 **/
    private static final int BASE_DETECTION_RADIUS = 8;
    /** 每级增加的检测半径 **/
    private static final int ADDITIONAL_RADIUS_PER_LEVEL = 4;
    /** 最大检测半径 **/
    private static final int MAX_DETECTION_RADIUS = 32;

    public TreasureFinder(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 获取检测半径
     *
     * @param amplifier 效果等级
     * @return 检测半径（格）
     */
    public static int getDetectionRadius(int amplifier) {
        return Math.min(MAX_DETECTION_RADIUS, BASE_DETECTION_RADIUS + (amplifier * ADDITIONAL_RADIUS_PER_LEVEL));
    }
}