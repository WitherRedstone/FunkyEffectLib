package com.chinaex123.funky_effect_lib.api.event.Slow;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * 减速事件基类
 * <p>
 * 所有减速相关事件的父类，提供实体和减速层数的通用访问方法
 */
public abstract class SlowEvent extends Event {

    /** 触发事件的实体（拥有减速层数的实体） */
    private final LivingEntity entity;
    /** 当前的减速层数 */
    private final int slowStacks;

    /**
     * 构造减速事件
     *
     * @param entity 触发事件的实体
     * @param slowStacks 当前减速层数
     */
    public SlowEvent(LivingEntity entity, int slowStacks) {
        this.entity = entity;
        this.slowStacks = slowStacks;
    }

    /**
     * 获取触发事件的实体
     *
     * @return 拥有减速层数的实体
     */
    public LivingEntity getEntity() {
        return entity;
    }

    /**
     * 获取当前减速层数
     *
     * @return 减速层数
     */
    public int getSlowStacks() {
        return slowStacks;
    }
}