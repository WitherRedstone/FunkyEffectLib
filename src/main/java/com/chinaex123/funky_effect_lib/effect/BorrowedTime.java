package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 借贷：受到致命伤害时扣除持续时间而非死亡，效果结束后立即死亡 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class BorrowedTime extends MobEffect {

    private static final int DURATION_PENALTY_PER_DEATH = 200; // 每次致命伤害扣除的持续时间

    private static final Map<UUID, Boolean> pendingDeathMap = new ConcurrentHashMap<>(); // 待死亡实体标记

    public BorrowedTime(int color) {
        super(MobEffectCategory.NEUTRAL, color);
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
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        UUID entityId = entity.getUUID();

        MobEffectInstance effect = entity.getEffect(FELEffects.BORROWED_TIME);
        if (effect == null) {
            return;
        }

        float originalDamage = event.getOriginalDamage();
        float currentHealth = entity.getHealth();

        if (originalDamage >= currentHealth) {
            event.setNewDamage(0.0f);

            int currentDuration = effect.getDuration();
            int newDuration = Math.max(1, currentDuration - DURATION_PENALTY_PER_DEATH);

            MobEffectInstance newEffect = new MobEffectInstance(FELEffects.BORROWED_TIME, newDuration, effect.getAmplifier());
            entity.forceAddEffect(newEffect, entity);

            pendingDeathMap.put(entityId, true);
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (Objects.requireNonNull(event.getEffectInstance()).getEffect() == FELEffects.BORROWED_TIME) {
            LivingEntity entity = event.getEntity();
            UUID entityId = entity.getUUID();
            if (pendingDeathMap.containsKey(entityId)) {
                pendingDeathMap.remove(entityId);
                killEntity(entity);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.BORROWED_TIME) {
            LivingEntity entity = event.getEntity();
            UUID entityId = entity.getUUID();
            if (pendingDeathMap.containsKey(entityId)) {
                pendingDeathMap.remove(entityId);
                killEntity(entity);
            }
        }
    }

    private static void killEntity(LivingEntity entity) {
        entity.setHealth(0.0f);
    }

    public static void clearPendingDeath(UUID entityId) {
        pendingDeathMap.remove(entityId);
    }
}