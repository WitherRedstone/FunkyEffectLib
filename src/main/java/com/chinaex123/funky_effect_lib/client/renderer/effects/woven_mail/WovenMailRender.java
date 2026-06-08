package com.chinaex123.funky_effect_lib.client.renderer.effects.woven_mail;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 织造铠甲渲染类：渲染缠结粒子，每个缠结提供高额减伤 **/
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class WovenMailRender {

    // 深绿色粒子
    private static final DustParticleOptions DARK_GREEN_TANGLE = new DustParticleOptions(
            new Vector3f(0.0f, 0.6f, 0.0f), 0.6f
    );

    private static final float ROTATION_SPEED = 0.05f; // 织造粒子旋转速度
    private static final float TANGLE_RADIUS = 1.2f; // 织造粒子半径
    private static final float PARTICLE_OFFSET_RANGE = 0.1f; // 织造粒子偏移范围

    private static final Map<UUID, Float> rotationAngleMap = new HashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        if (!livingEntity.hasEffect(FELEffects.WOVEN_MAIL)) {
            UUID entityId = livingEntity.getUUID();
            rotationAngleMap.remove(entityId);
            return;
        }

        UUID entityId = livingEntity.getUUID();
        int tangleCount = WovenMailClientData.getTangleCount(entityId);

        if (tangleCount <= 0) {
            rotationAngleMap.remove(entityId);
            return;
        }

        float currentAngle = rotationAngleMap.getOrDefault(entityId, 0.0f);
        currentAngle += ROTATION_SPEED;
        rotationAngleMap.put(entityId, currentAngle);

        for (int i = 0; i < tangleCount; i++) {
            float angle = (float) (2 * Math.PI * i / tangleCount) + currentAngle;
            float x = (float) livingEntity.getX() + (float) Math.cos(angle) * TANGLE_RADIUS;
            float y = (float) livingEntity.getY() + (float) (livingEntity.getBbHeight() / 2) + (float) Math.sin(angle * 2) * 0.3f;
            float z = (float) livingEntity.getZ() + (float) Math.sin(angle) * TANGLE_RADIUS;

            float offsetX = (livingEntity.getRandom().nextFloat() - 0.5f) * PARTICLE_OFFSET_RANGE;
            float offsetY = (livingEntity.getRandom().nextFloat() - 0.5f) * PARTICLE_OFFSET_RANGE;
            float offsetZ = (livingEntity.getRandom().nextFloat() - 0.5f) * PARTICLE_OFFSET_RANGE;

            mc.level.addParticle(DARK_GREEN_TANGLE,
                    x + offsetX, y + offsetY, z + offsetZ,
                    0, 0, 0
            );
        }
    }
}