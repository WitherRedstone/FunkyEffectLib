package com.chinaex123.funky_effect_lib.client.renderer.effects.frost_armor;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 冰冻铠甲渲染类：渲染冰晶粒子，每个冰晶提供高额减伤 **/
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class FrostArmorRender {

    private static final DustParticleOptions ICE_CRYSTAL = new DustParticleOptions(
            new Vector3f(0.8f, 0.9f, 1.0f), 0.5f
    );

    private static final float ROTATION_SPEED = 0.05f; // 冰晶旋转速度
    private static final float CRYSTAL_RADIUS = 1.2f; // 冰晶半径
    private static final float PARTICLE_OFFSET_RANGE = 0.1f; // 冰晶偏移范围

    private static final Map<UUID, Float> rotationAngleMap = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        // 遍历所有实体（简化处理，只处理玩家自身）
        LivingEntity livingEntity = mc.player;

        if (!livingEntity.hasEffect(FELEffects.FROST_ARMOR.get())) {
            UUID entityId = livingEntity.getUUID();
            rotationAngleMap.remove(entityId);
            return;
        }

        UUID entityId = livingEntity.getUUID();
        int crystalCount = FrostArmorClientData.getCrystalCount(entityId);

        if (crystalCount <= 0) {
            rotationAngleMap.remove(entityId);
            return;
        }

        float currentAngle = rotationAngleMap.getOrDefault(entityId, 0.0f);
        currentAngle += ROTATION_SPEED;
        rotationAngleMap.put(entityId, currentAngle);

        for (int i = 0; i < crystalCount; i++) {
            float angle = (float) (2 * Math.PI * i / crystalCount) + currentAngle;
            float x = (float) livingEntity.getX() + (float) Math.cos(angle) * CRYSTAL_RADIUS;
            float y = (float) livingEntity.getY() + (float) (livingEntity.getBbHeight() / 2) + (float) Math.sin(angle * 2) * 0.3f;
            float z = (float) livingEntity.getZ() + (float) Math.sin(angle) * CRYSTAL_RADIUS;

            float offsetX = (livingEntity.getRandom().nextFloat() - 0.5f) * PARTICLE_OFFSET_RANGE;
            float offsetY = (livingEntity.getRandom().nextFloat() - 0.5f) * PARTICLE_OFFSET_RANGE;
            float offsetZ = (livingEntity.getRandom().nextFloat() - 0.5f) * PARTICLE_OFFSET_RANGE;

            mc.level.addParticle(ICE_CRYSTAL,
                    x + offsetX, y + offsetY, z + offsetZ,
                    0, 0, 0
            );
        }
    }
}