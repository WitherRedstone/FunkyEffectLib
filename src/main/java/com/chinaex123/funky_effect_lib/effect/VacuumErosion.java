package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELDamageTypes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 真空侵蚀：每秒造成窒息伤害 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class VacuumErosion extends MobEffect {

    private static final float BASE_DAMAGE = 2.0f; // 基础伤害
    private static final float ADDITIONAL_DAMAGE_PER_LEVEL = 2.0f; // 每级额外伤害
    private static final int DAMAGE_INTERVAL = 20; // 伤害间隔

    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>(); // 伤害间隔计数器

    public VacuumErosion(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide()) {
            return;
        }

        UUID entityId = entity.getUUID();
        MobEffectInstance effect = entity.getEffect(FELEffects.VACUUM_EROSION);

        if (effect == null) {
            tickCounterMap.remove(entityId);
            return;
        }

        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        if (tickCounter >= DAMAGE_INTERVAL) {
            int amplifier = effect.getAmplifier();
            float damage = BASE_DAMAGE + (amplifier * ADDITIONAL_DAMAGE_PER_LEVEL);
            DamageSource vacuumErosionDamage = entity.level().damageSources().source(FELDamageTypes.VACUUM_EROSION);
            entity.hurt(vacuumErosionDamage, damage);
            tickCounter = 0;
        }

        tickCounterMap.put(entityId, tickCounter);
    }
}