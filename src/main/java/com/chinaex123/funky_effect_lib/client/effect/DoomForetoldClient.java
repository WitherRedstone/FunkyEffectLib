package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 厄运预示客户端处理类 **/
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class DoomForetoldClient {

    private static final Map<UUID, Boolean> CLIENT_MARK_CACHE = new ConcurrentHashMap<>();

    public static void setClientMark(UUID uuid, boolean hasMark) {
        if (hasMark) {
            CLIENT_MARK_CACHE.put(uuid, true);
        } else {
            CLIENT_MARK_CACHE.remove(uuid);
        }
    }

    public static boolean hasClientMark(UUID uuid) {
        return CLIENT_MARK_CACHE.getOrDefault(uuid, false);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        for (var entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living && CLIENT_MARK_CACHE.containsKey(living.getUUID())) {
                for (int i = 0; i < 2; i++) {
                    double x = living.getX() + (living.getRandom().nextDouble() - 0.5) * living.getBbWidth();
                    double y = living.getY() + living.getRandom().nextDouble() * living.getBbHeight();
                    double z = living.getZ() + (living.getRandom().nextDouble() - 0.5) * living.getBbWidth();
                    minecraft.level.addParticle(new DustParticleOptions(
                            new Vector3f(1.0F, 0.0F, 0.0F), 1.0F
                    ), x, y, z, 0, 0.05, 0);
                }
            }
        }
    }
}