package com.chinaex123.funky_effect_lib.api.event.Slow;

import net.minecraft.world.entity.LivingEntity;

/**
 * 减速移除事件：当实体的减速层数被清除时触发
 * <p>
 * 包含被清除前的层数信息
 */
public class SlowRemovedEvent extends SlowEvent {

    /** 被清除前的减速层数 */
    private final int previousStacks;

    /**
     * 构造减速移除事件
     *
     * @param entity 减速被清除的实体
     * @param previousStacks 被清除前的减速层数
     */
    public SlowRemovedEvent(LivingEntity entity, int previousStacks) {
        super(entity, 0);
        this.previousStacks = previousStacks;
    }

    /**
     * 获取被清除前的减速层数
     *
     * @return 被清除前的层数
     */
    public int getPreviousStacks() {
        return previousStacks;
    }
}
