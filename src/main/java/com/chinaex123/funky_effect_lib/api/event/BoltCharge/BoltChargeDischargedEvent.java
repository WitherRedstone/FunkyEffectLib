package com.chinaex123.funky_effect_lib.api.event.BoltCharge;

import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;

/** 电光充能释放事件：当实体释放电光充能时触发 **/
public class BoltChargeDischargedEvent extends BoltChargeEvent {
    private final LivingEntity target;
    private final LightningBolt lightning;

    public BoltChargeDischargedEvent(LivingEntity entity, int chargeCount, LivingEntity target, LightningBolt lightning) {
        super(entity, chargeCount);
        this.target = target;
        this.lightning = lightning;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public LightningBolt getLightning() {
        return lightning;
    }

    public boolean hasTarget() {
        return target != null;
    }
}