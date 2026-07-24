package com.chinaex123.funky_effect_lib.api;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.effect.Slow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/** 减速公共 API 类 **/
public class SlowAPI {

    /** 减速层数在持久化数据中的存储键 **/
    public static final ResourceLocation STACKS_KEY = FunkyEffectLib.id("slow_stacks");

    /** 最大减速层数 **/
    public static final int MAX_STACKS = 100;

    /** 获取实体的当前减速层数 **/
    public static int getStacks(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = STACKS_KEY.toString();
        return persistentData.getInt(key);
    }

    /** 设置实体的减速层数 **/
    public static void setStacksInternal(LivingEntity entity, int stacks) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = STACKS_KEY.toString();
        persistentData.putInt(key, Math.min(stacks, MAX_STACKS));
    }

    /** 为实体增加指定数量的减速层数 **/
    public static void addStacks(LivingEntity entity, int amount) {
        int current = getStacks(entity);
        int newStacks = Math.min(current + amount, MAX_STACKS);
        setStacksInternal(entity, newStacks);
        Slow.syncStacks(entity, newStacks);

        if (newStacks >= MAX_STACKS) {
            Slow.triggerFreeze(entity);
        }
    }

    /** 设置实体的减速层数为指定值 **/
    public static void setStacks(LivingEntity entity, int stacks) {
        int newStacks = Math.min(stacks, MAX_STACKS);
        setStacksInternal(entity, newStacks);
        Slow.syncStacks(entity, newStacks);

        if (newStacks >= MAX_STACKS) {
            Slow.triggerFreeze(entity);
        }
    }

    /** 清除实体的所有减速数据 **/
    public static void clearStacks(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        persistentData.remove(STACKS_KEY.toString());
    }
}