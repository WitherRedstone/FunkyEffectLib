package com.chinaex123.funky_effect_lib.api.event.BoltCharge;

import com.chinaex123.funky_effect_lib.api.BoltChargeAPI;
import net.minecraft.world.entity.LivingEntity;

/** 电光充能接收事件：当实体获得电光充能层数时触发 **/
public class BoltChargeReceivedEvent extends BoltChargeEvent {
    private final int amountAdded;
    private final int previousCount;

    public BoltChargeReceivedEvent(LivingEntity entity, int amountAdded, int previousCount, int newCount) {
        super(entity, newCount);
        this.amountAdded = amountAdded;
        this.previousCount = previousCount;
    }

    public int getAmountAdded() {
        return amountAdded;
    }

    public int getPreviousCount() {
        return previousCount;
    }

    public boolean isFullyCharged() {
        return getChargeCount() >= BoltChargeAPI.MAX_CHARGES;
    }
}
