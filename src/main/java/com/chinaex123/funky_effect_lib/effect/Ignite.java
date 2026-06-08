package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.init.FELSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 点燃：瞬间造成火焰内爆伤害 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Ignite extends MobEffect {

    private static final float BASE_DAMAGE = 10.0f; // 基础伤害
    private static final float DAMAGE_PER_LEVEL = 5.0f; // 每级额外伤害
    private static final int FIRE_DURATION = 100; // 点燃实体的时间

    private static final DustParticleOptions GOLDEN_DUST = new DustParticleOptions(
            new Vector3f(1.0f, 0.8f, 0.0f), 1.5f
    );

    private static final DustParticleOptions BRIGHT_GOLDEN_DUST = new DustParticleOptions(
            new Vector3f(1.0f, 0.9f, 0.2f), 2.0f
    );

    private static final Map<UUID, Boolean> damagedMap = new HashMap<>();

    public Ignite(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            UUID entityId = entity.getUUID();
            if (!damagedMap.containsKey(entityId)) {
                // 造成伤害
                dealDamage(entity, amplifier);
                damagedMap.put(entityId, true);

                // 立即移除效果
                entity.removeEffect(FELEffects.IGNITE);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 造成火焰内爆伤害
     */
    private static void dealDamage(LivingEntity entity, int amplifier) {
        float damage = BASE_DAMAGE + (DAMAGE_PER_LEVEL * amplifier);

        entity.hurt(entity.damageSources().onFire(), damage); // 造成火焰伤害
        entity.setRemainingFireTicks(FIRE_DURATION); // 点燃实体

        entity.level().playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                FELSounds.IGNITE_EXPLODE.get(),
                SoundSource.HOSTILE,
                1.5f,
                1.0f
        );

        // 金黄色粒子效果
        if (entity.level() instanceof ServerLevel serverLevel) {
            // 金黄色爆炸中心粒子
            serverLevel.sendParticles(GOLDEN_DUST,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    5, 0, 0, 0, 0);

            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    1, 0, 0, 0, 0.1f);

            // 金黄色爆炸冲击波
            for (int i = 0; i < 30; i++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = 0.8 + Math.random() * 0.5;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double offsetY = (Math.random() - 0.5);

                serverLevel.sendParticles(GOLDEN_DUST,
                        entity.getX() + offsetX,
                        entity.getY() + entity.getBbHeight() / 2 + offsetY,
                        entity.getZ() + offsetZ,
                        1, 0, 0, 0, 0);
            }

            // 火焰粒子
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    15, 0.5, 0.5, 0.5, 0.05);

            // 金黄色闪光粒子
            serverLevel.sendParticles(BRIGHT_GOLDEN_DUST,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    10, 0.3, 0.3, 0.3, 0.02);
        }
    }

    /**
     * 效果结束时清理记录
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide()) {
            if (!entity.hasEffect(FELEffects.IGNITE)) {
                damagedMap.remove(entity.getUUID());
            }
        }
    }
}