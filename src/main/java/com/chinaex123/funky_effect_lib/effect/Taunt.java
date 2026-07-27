package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

/**
 * 嘲讽：吸引附近的敌对生物
 * <p>
 * 机制：
 * <ol>
 *   <li>每Tick将范围内敌对生物的目标设置为施法者</li>
 *   <li>基础嘲讽半径为8格</li>
 *   <li>每级增加4格半径</li>
 *   <li>最大半径为32格</li>
 * </ol>
 */
public class Taunt extends MobEffect {

    /** 基础嘲讽半径 **/
    private static final int BASE_TAUNT_RADIUS = 8;
    /** 每级额外嘲讽半径 **/
    private static final int ADDITIONAL_RADIUS_PER_LEVEL = 4;
    /** 最大嘲讽半径 **/
    private static final int MAX_TAUNT_RADIUS = 32;

    public Taunt(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 仅在服务端执行
        if (entity.level().isClientSide()) {
            return;
        }

        // 计算嘲讽半径
        int tauntRadius = getTauntRadius(amplifier);
        // 创建搜索区域
        AABB searchArea = entity.getBoundingBox().inflate(tauntRadius);

        // 搜索范围内的所有怪物
        entity.level().getEntitiesOfClass(Mob.class, searchArea, mob -> {
            if (mob == entity) return false;
            if (mob.isRemoved()) return false;
            // 仅对敌对生物有效
            if (mob.getType().getCategory() != MobCategory.MONSTER) return false;
            // 检查是否可以攻击该实体
            return mob.canAttack(entity);
        }).forEach(mob -> {
            // 将目标设置为施法者
            mob.setTarget(entity);
        });
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 获取嘲讽半径
     *
     * @param amplifier 效果等级
     * @return 嘲讽半径（格）
     */
    public static int getTauntRadius(int amplifier) {
        return Math.min(MAX_TAUNT_RADIUS, BASE_TAUNT_RADIUS + (amplifier * ADDITIONAL_RADIUS_PER_LEVEL));
    }
}