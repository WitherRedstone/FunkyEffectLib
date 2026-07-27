package com.chinaex123.funky_effect_lib.api.event.Scorch;

import net.minecraft.world.entity.LivingEntity;

/**
 * 灼烧接收事件：当实体获得灼烧层数时触发
 * <p>
 * 包含增加数量、增加前的层数和增加后的层数信息
 */
public class ScorchReceivedEvent extends ScorchEvent {

    /** 本次增加的灼烧层数 */
    private final int amountAdded;
    /** 增加前的灼烧层数 */
    private final int previousStacks;

    /**
     * 构造灼烧接收事件
     *
     * @param entity 获得灼烧的实体
     * @param amountAdded 增加的灼烧层数
     * @param previousStacks 增加前的灼烧层数
     * @param newStacks 增加后的灼烧层数
     */
    public ScorchReceivedEvent(LivingEntity entity, int amountAdded, int previousStacks, int newStacks) {
        super(entity, newStacks);
        this.amountAdded = amountAdded;
        this.previousStacks = previousStacks;
    }

    /**
     * 获取本次增加的灼烧层数
     *
     * @return 增加的层数
     */
    public int getAmountAdded() {
        return amountAdded;
    }

    /**
     * 获取增加前的灼烧层数
     *
     * @return 增加前的层数
     */
    public int getPreviousStacks() {
        return previousStacks;
    }

    /**
     * 检查灼烧是否已达到满层状态
     *
     * @return true表示已达到满层，false表示未满
     */
    public boolean isFullyStacked() {
        return getScorchStacks() >= ScorchAPI.MAX_SCORCH_STACKS;
    }
}