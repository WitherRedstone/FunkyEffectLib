package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 借贷：受到致命伤害时扣除持续时间而非死亡，效果结束后立即死亡 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class BorrowedTime extends MobEffect {

    private static final int DURATION_PENALTY_PER_DEATH = 200;

    private static final Map<UUID, Boolean> pendingDeathMap = new ConcurrentHashMap<>();

    public BorrowedTime(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        UUID entityId = entity.getUUID();

        MobEffectInstance effect = entity.getEffect(FELEffects.BORROWED_TIME.get());
        if (effect == null) {
            return;
        }

        float originalDamage = event.getAmount();
        float currentHealth = entity.getHealth();

        if (originalDamage >= currentHealth) {
            event.setAmount(0.0f);

            int currentDuration = effect.getDuration();
            int newDuration = Math.max(1, currentDuration - DURATION_PENALTY_PER_DEATH);

            pendingDeathMap.put(entityId, true);

            entity.removeEffect(FELEffects.BORROWED_TIME.get());
            MobEffectInstance newEffect = new MobEffectInstance(FELEffects.BORROWED_TIME.get(), newDuration, effect.getAmplifier());
            entity.addEffect(newEffect);
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.BORROWED_TIME.get()) {
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