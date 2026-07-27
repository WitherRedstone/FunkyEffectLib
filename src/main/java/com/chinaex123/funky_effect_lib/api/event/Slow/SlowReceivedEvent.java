package com.chinaex123.funky_effect_lib.api.event.Slow;

import net.minecraft.world.entity.LivingEntity;

/**
 * 减速接收事件：当实体获得减速层数时触发
 * <p>
 * 包含增加数量、增加前的层数和增加后的层数信息
 */
public class SlowReceivedEvent extends SlowEvent {

    /** 本次增加的减速层数 */
    private final int amountAdded;
    /** 增加前的减速层数 */
    private final int previousStacks;

    /**
     * 构造减速接收事件
     *
     * @param entity 获得减速的实体
     * @param amountAdded 增加的减速层数
     * @param previousStacks 增加前的减速层数
     * @param newStacks 增加后的减速层数
     */
    public SlowReceivedEvent(LivingEntity entity, int amountAdded, int previousStacks, int newStacks) {
        super(entity, newStacks);
        this.amountAdded = amountAdded;
        this.previousStacks = previousStacks;
    }

    /**
     * 获取本次增加的减速层数
     *
     * @return 增加的层数
     */
    public int getAmountAdded() {
        return amountAdded;
    }

    /**
     * 获取增加前的减速层数
     *
     * @return 增加前的层数
     */
    public int getPreviousStacks() {
        return previousStacks;
    }

    /**
     * 检查减速是否已达到满层状态
     *
     * @return true表示已达到满层，false表示未满
     */
    public boolean isFullyStacked() {
        return getSlowStacks() >= SlowAPI.MAX_STACKS;
    }
}