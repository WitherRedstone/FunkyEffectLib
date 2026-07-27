package com.chinaex123.funky_effect_lib.client.renderer.effects.woven_mail;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 织造铠甲客户端数据类
 * <p>
 * 功能：存储织造铠甲的缠结数量和最早过期时间
 */
public class WovenMailClientData {

    /** 缓存每个实体的缠结数量 **/
    private static final Map<UUID, Integer> tangleCountMap = new ConcurrentHashMap<>();
    /** 缓存每个实体的最早过期时间 **/
    private static final Map<UUID, Long> earliestExpiryMap = new ConcurrentHashMap<>();

    /**
     * 获取指定实体的缠结数量
     * 如果已过期则返回0并自动清除数据
     *
     * @param entityId 实体UUID
     * @return 缠结数量，如果已过期或不存在则返回0
     */
    public static int getTangleCount(UUID entityId) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return 0;

        long currentTime = level.getGameTime();
        Long earliestExpiry = earliestExpiryMap.get(entityId);

        // 如果已过期，返回0
        if (earliestExpiry != null && currentTime >= earliestExpiry) {
            return 0;
        }

        return tangleCountMap.getOrDefault(entityId, 0);
    }

    /**
     * 设置指定实体的缠结数量和最早过期时间
     * 如果数量小于等于0，则清除缓存
     *
     * @param entityId 实体UUID
     * @param count 缠结数量
     * @param earliestExpiry 最早过期时间（游戏刻）
     */
    public static void setTangleCount(UUID entityId, int count, long earliestExpiry) {
        if (count <= 0) {
            // 数量为0时清除缓存
            tangleCountMap.remove(entityId);
            earliestExpiryMap.remove(entityId);
        } else {
            tangleCountMap.put(entityId, count);
            earliestExpiryMap.put(entityId, earliestExpiry);
        }
    }

    /**
     * 清除指定实体的所有织造铠甲数据
     *
     * @param entityId 实体UUID
     */
    public static void clearTangles(UUID entityId) {
        tangleCountMap.remove(entityId);
        earliestExpiryMap.remove(entityId);
    }

    /**
     * 获取指定实体的最早过期时间
     *
     * @param entityId 实体UUID
     * @return 最早过期时间（游戏刻），如果不存在则返回0
     */
    public static long getEarliestExpiry(UUID entityId) {
        return earliestExpiryMap.getOrDefault(entityId, 0L);
    }
}