package com.chinaex123.funky_effect_lib.api.event.BoltCharge;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/** 电光充能事件基类 **/
public abstract class BoltChargeEvent extends Event {
    private final LivingEntity entity;
    private final int chargeCount;

    public BoltChargeEvent(LivingEntity entity, int chargeCount) {
        this.entity = entity;
        this.chargeCount = chargeCount;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public int getChargeCount() {
        return chargeCount;
    }
}