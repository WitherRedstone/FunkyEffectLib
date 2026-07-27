package com.chinaex123.funky_effect_lib.api.event.Slow;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.effect.Slow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

/**
 * 减速公共 API 类
 * <p>
 * 提供减速层数的管理、存储、同步和事件发布功能
 * 当减速层数达到最大值时，触发冻结效果
 * 减速层数存储在实体的持久化数据中，支持服务端与客户端同步
 */
public class SlowAPI {

    /** 减速层数在持久化数据中的存储键 **/
    public static final ResourceLocation STACKS_KEY = FunkyEffectLib.id("slow_stacks");

    /** 最大减速层数 **/
    public static final int MAX_STACKS = 100;

    /**
     * 获取实体的当前减速层数
     *
     * @param entity 目标实体
     * @return 当前减速层数
     */
    public static int getStacks(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = STACKS_KEY.toString();
        return persistentData.getInt(key);
    }

    /**
     * 设置实体的减速层数（内部方法，不触发冻结检测和事件）
     *
     * @param entity 目标实体
     * @param stacks 要设置的层数
     */
    public static void setStacksInternal(LivingEntity entity, int stacks) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = STACKS_KEY.toString();
        persistentData.putInt(key, Math.min(stacks, MAX_STACKS));
    }

    /**
     * 为实体增加指定数量的减速层数
     * 当层数达到最大值时触发冻结效果
     *
     * @param entity 目标实体
     * @param amount 增加的层数
     */
    public static void addStacks(LivingEntity entity, int amount) {
        int current = getStacks(entity);
        int newStacks = Math.min(current + amount, MAX_STACKS);
        setStacksInternal(entity, newStacks);
        Slow.syncStacks(entity, newStacks);

        if (amount > 0) {
            // 发布减速接收事件
            MinecraftForge.EVENT_BUS.post(new SlowReceivedEvent(entity, amount, current, newStacks));
        }

        // 达到最大层数时触发冻结
        if (newStacks >= MAX_STACKS) {
            Slow.triggerFreeze(entity);
        }
    }

    /**
     * 设置实体的减速层数为指定值
     * 当层数达到最大值时触发冻结效果
     *
     * @param entity 目标实体
     * @param stacks 要设置的层数
     */
    public static void setStacks(LivingEntity entity, int stacks) {
        int current = getStacks(entity);
        int newStacks = Math.min(stacks, MAX_STACKS);
        setStacksInternal(entity, newStacks);
        Slow.syncStacks(entity, newStacks);

        if (newStacks != current) {
            int amountAdded = newStacks - current;
            // 发布减速接收事件
            MinecraftForge.EVENT_BUS.post(new SlowReceivedEvent(entity, amountAdded, current, newStacks));
        }

        // 达到最大层数时触发冻结
        if (newStacks >= MAX_STACKS) {
            Slow.triggerFreeze(entity);
        }
    }

    /**
     * 清除实体的所有减速层数数据
     *
     * @param entity 目标实体
     */
    public static void clearStacks(LivingEntity entity) {
        int current = getStacks(entity);
        CompoundTag persistentData = entity.getPersistentData();
        persistentData.remove(STACKS_KEY.toString());
        
        if (current > 0) {
            // 发布减速移除事件
            MinecraftForge.EVENT_BUS.post(new SlowRemovedEvent(entity, current));
        }
    }
}