package com.chinaex123.funky_effect_lib.api.event.BoltCharge;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * 电光充能事件基类
 * <p>
 * 所有电光充能相关事件的父类，提供实体和充能层数的通用访问方法
 */
public abstract class BoltChargeEvent extends Event {

    /** 触发事件的实体（拥有电光充能层数的实体） */
    private final LivingEntity entity;
    /** 当前的电光充能层数 */
    private final int chargeCount;

    /**
     * 构造电光充能事件
     *
     * @param entity 触发事件的实体
     * @param chargeCount 当前电光充能层数
     */
    public BoltChargeEvent(LivingEntity entity, int chargeCount) {
        this.entity = entity;
        this.chargeCount = chargeCount;
    }

    /**
     * 获取触发事件的实体
     *
     * @return 拥有电光充能层数的实体
     */
    public LivingEntity getEntity() {
        return entity;
    }

    /**
     * 获取当前电光充能层数
     *
     * @return 充能层数
     */
    public int getChargeCount() {
        return chargeCount;
    }
}