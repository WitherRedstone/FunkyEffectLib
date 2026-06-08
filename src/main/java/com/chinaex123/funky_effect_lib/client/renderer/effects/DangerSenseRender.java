package com.chinaex123.funky_effect_lib.client.renderer.effects;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.effect.DangerSense;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** 危险感知渲染类：显示周围的敌对生物 **/
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class DangerSenseRender {

    private static final Set<LivingEntity> glowingEntities = new HashSet<>(); // 存储当前需要显示的敌对生物
    private static boolean effectActive = false; // 危险感知效果是否激活

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        boolean hasDangerSense = mc.player.hasEffect(FELEffects.DANGER_SENSE);

        if (hasDangerSense) {
            int amplifier = Objects.requireNonNull(mc.player.getEffect(FELEffects.DANGER_SENSE)).getAmplifier();
            int detectionRadius = DangerSense.getDetectionRadius(amplifier);
            updateGlowingEntities(mc.level, mc.player, detectionRadius);
            effectActive = true;
        } else {
            if (effectActive) {
                glowingEntities.clear();
                effectActive = false;
            }
        }
    }

    private static void updateGlowingEntities(Level level, LivingEntity player, int radius) {
        Set<LivingEntity> newGlowingEntities = new HashSet<>();

        AABB searchArea = player.getBoundingBox().inflate(radius);

        for (Mob entity : level.getEntitiesOfClass(Mob.class, searchArea, entity -> {
            if (entity == player) return false;
            if (entity.isRemoved()) return false;
            return entity.getType().getCategory() == MobCategory.MONSTER;
        })) {
            newGlowingEntities.add(entity);
        }

        glowingEntities.clear();
        glowingEntities.addAll(newGlowingEntities);
    }

    public static boolean isEffectActive() {
        return effectActive;
    }

    public static boolean shouldEntityGlow(LivingEntity entity) {
        return glowingEntities.contains(entity);
    }
}