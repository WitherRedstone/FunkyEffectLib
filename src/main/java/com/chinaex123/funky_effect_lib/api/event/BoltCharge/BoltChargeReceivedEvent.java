package com.chinaex123.funky_effect_lib.api.event.BoltCharge;

import net.minecraft.world.entity.LivingEntity;

/**
 * 电光充能接收事件：当实体获得电光充能层数时触发
 * <p>
 * 包含增加数量、增加前的层数和增加后的层数信息
 */
public class BoltChargeReceivedEvent extends BoltChargeEvent {

    /** 本次增加的充能层数 */
    private final int amountAdded;
    /** 增加前的充能层数 */
    private final int previousCount;

    /**
     * 构造电光充能接收事件
     *
     * @param entity 获得充能的实体
     * @param amountAdded 增加的充能层数
     * @param previousCount 增加前的充能层数
     * @param newCount 增加后的充能层数
     */
    public BoltChargeReceivedEvent(LivingEntity entity, int amountAdded, int previousCount, int newCount) {
        super(entity, newCount);
        this.amountAdded = amountAdded;
        this.previousCount = previousCount;
    }

    /**
     * 获取本次增加的充能层数
     *
     * @return 增加的层数
     */
    public int getAmountAdded() {
        return amountAdded;
    }

    /**
     * 获取增加前的充能层数
     *
     * @return 增加前的层数
     */
    public int getPreviousCount() {
        return previousCount;
    }

    /**
     * 检查充能是否已达到满层状态
     *
     * @return true表示已达到满层，false表示未满
     */
    public boolean isFullyCharged() {
        return getChargeCount() >= BoltChargeAPI.MAX_CHARGES;
    }
}