package com.chinaex123.funky_effect_lib.api.event.BoltCharge;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;

/**
 * 电光充能释放事件：当实体释放电光充能时触发
 * <p>
 * 在实体消耗电光充能召唤闪电时触发，包含释放目标、生成的闪电和之前的充能层数信息
 * 可用于监听电光充能消耗、统计效果触发次数等场景
 */
public class BoltChargeDischargedEvent extends BoltChargeEvent {

    /** 闪电击中的目标实体 */
    private final LivingEntity target;
    /** 生成的闪电实体 */
    private final LightningBolt lightning;
    /** 释放前的充能层数 */
    private final int previousCount;

    /**
     * 构造电光充能释放事件
     *
     * @param entity 触发释放事件的实体
     * @param chargeCount 释放后的剩余充能层数
     * @param target 闪电击中的目标实体
     * @param lightning 生成的闪电实体
     * @param previousCount 释放前的充能层数
     */
    public BoltChargeDischargedEvent(LivingEntity entity, int chargeCount, LivingEntity target, LightningBolt lightning, int previousCount) {
        super(entity, chargeCount);
        this.target = target;
        this.lightning = lightning;
        this.previousCount = previousCount;
    }

    /**
     * 获取闪电击中的目标实体
     *
     * @return 目标实体，如果无目标则返回null
     */
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * 获取生成的闪电实体
     *
     * @return 闪电实体，如果未生成则返回null
     */
    public LightningBolt getLightning() {
        return lightning;
    }

    /**
     * 检查是否有被击中的目标实体
     *
     * @return true表示有目标，false表示无目标
     */
    public boolean hasTarget() {
        return target != null;
    }

    /**
     * 获取释放前的充能层数
     *
     * @return 释放前的电光充能层数
     */
    public int getPreviousCount() {
        return previousCount;
    }
}