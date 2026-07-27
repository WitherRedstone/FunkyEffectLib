package com.chinaex123.funky_effect_lib.api.event.Scorch;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/**
 * 灼烧事件基类
 * <p>
 * 所有灼烧相关事件的父类，提供实体和灼烧层数的通用访问方法
 */
public abstract class ScorchEvent extends Event {

    /** 触发事件的实体（拥有灼烧层数的实体） */
    private final LivingEntity entity;
    /** 当前的灼烧层数 */
    private final int scorchStacks;

    /**
     * 构造灼烧事件
     *
     * @param entity 触发事件的实体
     * @param scorchStacks 当前灼烧层数
     */
    public ScorchEvent(LivingEntity entity, int scorchStacks) {
        this.entity = entity;
        this.scorchStacks = scorchStacks;
    }

    /**
     * 获取触发事件的实体
     *
     * @return 拥有灼烧层数的实体
     */
    public LivingEntity getEntity() {
        return entity;
    }

    /**
     * 获取当前灼烧层数
     *
     * @return 灼烧层数
     */
    public int getScorchStacks() {
        return scorchStacks;
    }
}