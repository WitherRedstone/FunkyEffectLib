package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 洞察：攻击同类型生物时伤害递增，目标死亡时重置 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Discern extends MobEffect {

    private static final float BASE_DAMAGE_MULTIPLIER = 1.0f; // 基础伤害倍数
    private static final float DAMAGE_INCREASE_PER_HIT = 0.1f; // 每次攻击增加的伤害倍数
    private static final float MAX_MULTIPLIER = 3.0f; // 最大伤害倍数

    private static final Map<UUID, Map<EntityType<?>, Integer>> attackCountMap = new HashMap<>();

    public Discern(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
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
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            MobEffectInstance effect = attacker.getEffect(FELEffects.DISCERN);
            if (effect == null) {
                return;
            }

            LivingEntity target = event.getEntity();

            UUID attackerId = attacker.getUUID();
            EntityType<?> targetType = target.getType();

            Map<EntityType<?>, Integer> typeCountMap = attackCountMap.computeIfAbsent(attackerId, k -> new HashMap<>());
            int hitCount = typeCountMap.getOrDefault(targetType, 0);

            float multiplier = BASE_DAMAGE_MULTIPLIER + (hitCount * DAMAGE_INCREASE_PER_HIT);
            multiplier = Math.min(multiplier, MAX_MULTIPLIER);

            float originalDamage = event.getOriginalDamage();
            float newDamage = originalDamage * multiplier;
            event.setNewDamage(newDamage);

            hitCount++;
            typeCountMap.put(targetType, hitCount);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();
        EntityType<?> targetType = target.getType();

        for (Map.Entry<UUID, Map<EntityType<?>, Integer>> entry : attackCountMap.entrySet()) {
            Map<EntityType<?>, Integer> typeCountMap = entry.getValue();
            typeCountMap.remove(targetType);
            if (typeCountMap.isEmpty()) {
                attackCountMap.remove(entry.getKey());
            }
        }
    }

    public static int getHitCount(UUID attackerId, EntityType<?> targetType) {
        Map<EntityType<?>, Integer> typeCountMap = attackCountMap.get(attackerId);
        if (typeCountMap == null) {
            return 0;
        }
        return typeCountMap.getOrDefault(targetType, 0);
    }

    public static void clearAttackerData(UUID attackerId) {
        attackCountMap.remove(attackerId);
    }
}