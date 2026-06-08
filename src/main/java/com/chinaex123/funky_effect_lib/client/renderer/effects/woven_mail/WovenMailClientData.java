package com.chinaex123.funky_effect_lib.client.renderer.effects.woven_mail;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 织造铠甲客户端数据类：存储缠结数量和过期时间 **/
public class WovenMailClientData {

    private static final Map<UUID, Integer> tangleCountMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> earliestExpiryMap = new ConcurrentHashMap<>();

    public static int getTangleCount(UUID entityId) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return 0;

        long currentTime = level.getGameTime();
        Long earliestExpiry = earliestExpiryMap.get(entityId);

        if (earliestExpiry != null && currentTime >= earliestExpiry) {
            return 0;
        }

        return tangleCountMap.getOrDefault(entityId, 0);
    }

    public static void setTangleCount(UUID entityId, int count, long earliestExpiry) {
        if (count <= 0) {
            tangleCountMap.remove(entityId);
            earliestExpiryMap.remove(entityId);
        } else {
            tangleCountMap.put(entityId, count);
            earliestExpiryMap.put(entityId, earliestExpiry);
        }
    }

    public static void clearTangles(UUID entityId) {
        tangleCountMap.remove(entityId);
        earliestExpiryMap.remove(entityId);
    }
}