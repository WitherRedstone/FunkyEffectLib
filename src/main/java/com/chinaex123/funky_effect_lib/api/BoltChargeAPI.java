package com.chinaex123.funky_effect_lib.api;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.api.event.BoltCharge.BoltChargeDischargedEvent;
import com.chinaex123.funky_effect_lib.api.event.BoltCharge.BoltChargeReceivedEvent;
import com.chinaex123.funky_effect_lib.effect.BoltCharge;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

/** 电光充能公共 API 类 **/
public class BoltChargeAPI {

    /** 充能层数在持久化数据中的存储键 **/
    public static final ResourceLocation CHARGE_COUNT_KEY = ResourceLocation.fromNamespaceAndPath(
            FunkyEffectLib.MOD_ID, "bolt_charge_count"
    );

    /** 上次充能时间在持久化数据中的存储键 **/
    public static final ResourceLocation LAST_CHARGE_TIME_KEY = ResourceLocation.fromNamespaceAndPath(
            FunkyEffectLib.MOD_ID, "bolt_charge_last_time"
    );

    /** 最大充能层数 **/
    public static final int MAX_CHARGES = 10;

    /** 闪电造成的伤害值 **/
    public static final float LIGHTNING_DAMAGE = 5.0f;

    /** 获取实体的当前充能层数 **/
    public static int getChargeCount(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = CHARGE_COUNT_KEY.toString();
        return persistentData.getInt(key);
    }

    /** 设置实体的充能层数 **/
    public static void setChargeCountInternal(LivingEntity entity, int count) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = CHARGE_COUNT_KEY.toString();
        persistentData.putInt(key, Math.min(count, MAX_CHARGES));
    }

    /** 获取实体的上次充能时间 **/
    public static long getLastChargeTime(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = LAST_CHARGE_TIME_KEY.toString();
        return persistentData.getLong(key);
    }

    /** 设置实体的上次充能时间 **/
    public static void setLastChargeTime(LivingEntity entity, long time) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = LAST_CHARGE_TIME_KEY.toString();
        persistentData.putLong(key, time);
    }

    /** 为实体增加指定数量的充能层数 **/
    public static void addCharge(LivingEntity entity, int amount) {
        int current = getChargeCount(entity);
        int newCount = Math.min(current + amount, MAX_CHARGES);
        setChargeCountInternal(entity, newCount);
        BoltCharge.syncToClient(entity, newCount);

        if (amount > 0) {
            NeoForge.EVENT_BUS.post(new BoltChargeReceivedEvent(entity, amount, current, newCount));
        }
    }

    /** 设置实体的充能层数为指定值 **/
    public static void setChargeCount(LivingEntity entity, int count) {
        int current = getChargeCount(entity);
        int newCount = Math.min(count, MAX_CHARGES);
        setChargeCountInternal(entity, newCount);
        BoltCharge.syncToClient(entity, newCount);

        if (newCount != current) {
            int amountAdded = newCount - current;
            NeoForge.EVENT_BUS.post(new BoltChargeReceivedEvent(entity, amountAdded, current, newCount));
        }
    }

    /** 清除实体的所有充能数据 **/
    public static void clearCharges(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        persistentData.remove(CHARGE_COUNT_KEY.toString());
        persistentData.remove(LAST_CHARGE_TIME_KEY.toString());
    }

    /** 在目标实体位置召唤一道闪电，由攻击者触发 **/
    public static void triggerLightning(LivingEntity target, LivingEntity attacker) {
        if (target.level().isClientSide()) {
            return;
        }

        BlockPos pos = target.blockPosition();
        EntityType<LightningBolt> lightningType = EntityType.LIGHTNING_BOLT;
        LightningBolt lightning = lightningType.create(target.level());

        if (lightning != null) {
            lightning.moveTo(Vec3.atBottomCenterOf(pos));
            lightning.setDamage(LIGHTNING_DAMAGE);
            if (attacker instanceof ServerPlayer) {
                lightning.setCause((ServerPlayer) attacker);
            }
            target.level().addFreshEntity(lightning);

            target.level().playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.MASTER, 1.0f, 1.0f);
            target.level().playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT,
                    SoundSource.MASTER, 1.0f, 1.0f);
        }

        clearCharges(attacker);
        BoltCharge.syncToClient(attacker, 0);

        NeoForge.EVENT_BUS.post(new BoltChargeDischargedEvent(attacker, MAX_CHARGES, target, lightning));
    }
}