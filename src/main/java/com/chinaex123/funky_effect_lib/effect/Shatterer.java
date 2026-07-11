package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 碎裂：当被冻结的目标碎裂时，造成大量伤害 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Shatterer extends InstantenousMobEffect {

    private static final float BASE_DAMAGE_MULTIPLIER = 3.0f; // 基础伤害倍率
    private static final float EXTRA_MULTIPLIER_PER_LEVEL = 0.5f; // 每级额外增加伤害倍率
    private static final float DEFAULT_DAMAGE = 5.0f; // 默认伤害

    private static final Map<UUID, Float> triggerDamageMap = new HashMap<>();
    private static final Map<UUID, Boolean> shatteredMap = new HashMap<>();

    public Shatterer(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            UUID entityId = entity.getUUID();
            if (!shatteredMap.containsKey(entityId)) {
                // 计算碎裂伤害
            float shatterDamage;
            if (triggerDamageMap.containsKey(entityId)) {
                // 有触发伤害（来自Freeze），使用倍率计算
                float triggerDamage = triggerDamageMap.get(entityId);
                float damageMultiplier = BASE_DAMAGE_MULTIPLIER + (EXTRA_MULTIPLIER_PER_LEVEL * amplifier);
                shatterDamage = triggerDamage * damageMultiplier;
            } else {
                // 没有触发伤害（直接应用），直接使用默认伤害
                shatterDamage = DEFAULT_DAMAGE;
            }

                // 造成伤害
                entity.hurt(entity.damageSources().generic(), shatterDamage);
                shatteredMap.put(entityId, true);

                // 播放粒子效果
                if (entity.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 20; i++) {
                        double angle = Math.random() * Math.PI * 2;
                        double radius = 0.5 + Math.random() * 0.5;
                        double offsetX = Math.cos(angle) * radius;
                        double offsetZ = Math.sin(angle) * radius;
                        double offsetY = (Math.random() - 0.5);

                        serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                                entity.getX() + offsetX,
                                entity.getY() + entity.getBbHeight() / 2 + offsetY,
                                entity.getZ() + offsetZ,
                                1, 0, 0, 0, 0);
                    }

                    for (int i = 0; i < 10; i++) {
                        double angle = Math.random() * Math.PI * 2;
                        double radius = 0.3 + Math.random() * 0.3;
                        double offsetX = Math.cos(angle) * radius;
                        double offsetZ = Math.sin(angle) * radius;
                        double offsetY = (Math.random() - 0.5) * 0.5;

                        serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                                entity.getX() + offsetX,
                                entity.getY() + entity.getBbHeight() / 2 + offsetY,
                                entity.getZ() + offsetZ,
                                1, 0, 0, 0, 0);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.SHATTERER.get()) {
            UUID entityId = event.getEntity().getUUID();
            shatteredMap.remove(entityId);
            triggerDamageMap.remove(entityId);
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEffectInstance().getEffect() == FELEffects.SHATTERER.get()) {
            UUID entityId = event.getEntity().getUUID();
            shatteredMap.remove(entityId);
            triggerDamageMap.remove(entityId);
        }
    }
    
    /** 设置触发时的伤害 **/
    public static void setTriggerDamage(UUID entityId, float damage) {
        triggerDamageMap.put(entityId, damage);
    }
}