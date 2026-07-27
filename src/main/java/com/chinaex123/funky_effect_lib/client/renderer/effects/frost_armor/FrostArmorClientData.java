package com.chinaex123.funky_effect_lib.client.renderer.effects.frost_armor;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 冰霜护甲客户端数据类
 * <p>
 * 功能：存储冰霜护甲的冰晶数量和最早过期时间
 */
public class FrostArmorClientData {

    /** 缓存每个实体的冰晶数量 **/
    private static final Map<UUID, Integer> crystalCountMap = new ConcurrentHashMap<>();
    /** 缓存每个实体的最早过期时间 **/
    private static final Map<UUID, Long> earliestExpiryMap = new ConcurrentHashMap<>();

    /**
     * 获取指定实体的冰晶数量
     * 如果已过期则返回0并自动清除数据
     *
     * @param entityId 实体UUID
     * @return 冰晶数量，如果已过期或不存在则返回0
     */
    public static int getCrystalCount(UUID entityId) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return 0;

        long currentTime = level.getGameTime();
        Long earliestExpiry = earliestExpiryMap.get(entityId);

        // 如果已过期，返回0
        if (earliestExpiry != null && currentTime >= earliestExpiry) {
            return 0;
        }

        return crystalCountMap.getOrDefault(entityId, 0);
    }

    /**
     * 设置指定实体的冰晶数量和最早过期时间
     * 如果数量小于等于0，则清除缓存
     *
     * @param entityId 实体UUID
     * @param count 冰晶数量
     * @param earliestExpiry 最早过期时间（游戏刻）
     */
    public static void setCrystalCount(UUID entityId, int count, long earliestExpiry) {
        if (count <= 0) {
            // 数量为0时清除缓存
            crystalCountMap.remove(entityId);
            earliestExpiryMap.remove(entityId);
        } else {
            crystalCountMap.put(entityId, count);
            earliestExpiryMap.put(entityId, earliestExpiry);
        }
    }

    /**
     * 清除指定实体的所有冰霜护甲数据
     *
     * @param entityId 实体UUID
     */
    public static void clearCrystals(UUID entityId) {
        crystalCountMap.remove(entityId);
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