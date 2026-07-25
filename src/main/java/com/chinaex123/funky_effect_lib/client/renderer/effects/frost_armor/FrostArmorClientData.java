package com.chinaex123.funky_effect_lib.client.renderer.effects.frost_armor;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 冰冻铠甲客户端数据类：存储水晶数量和过期时间 **/
public class FrostArmorClientData {

    private static final Map<UUID, Integer> crystalCountMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> earliestExpiryMap = new ConcurrentHashMap<>();

    public static int getCrystalCount(UUID entityId) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return 0;

        long currentTime = level.getGameTime();
        Long earliestExpiry = earliestExpiryMap.get(entityId);

        if (earliestExpiry != null && currentTime >= earliestExpiry) {
            return 0;
        }

        return crystalCountMap.getOrDefault(entityId, 0);
    }

    public static void setCrystalCount(UUID entityId, int count, long earliestExpiry) {
        if (count <= 0) {
            crystalCountMap.remove(entityId);
            earliestExpiryMap.remove(entityId);
        } else {
            crystalCountMap.put(entityId, count);
            earliestExpiryMap.put(entityId, earliestExpiry);
        }
    }

    public static void clearCrystals(UUID entityId) {
        crystalCountMap.remove(entityId);
        earliestExpiryMap.remove(entityId);
    }

    public static long getEarliestExpiry(UUID entityId) {
        return earliestExpiryMap.getOrDefault(entityId, 0L);
    }
}