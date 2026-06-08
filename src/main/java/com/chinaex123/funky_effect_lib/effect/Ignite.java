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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 点燃：瞬间造成火焰内爆伤害 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Ignite extends MobEffect {

    private static final float BASE_DAMAGE = 10.0f; // 基础伤害
    private static final float DAMAGE_PER_LEVEL = 5.0f; // 每级额外伤害
    private static final int FIRE_DURATION = 100; // 火焰持续时间

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
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            UUID entityId = entity.getUUID();
            if (!damagedMap.containsKey(entityId)) {
                dealDamage(entity, amplifier);
                damagedMap.put(entityId, true);
                entity.removeEffect(FELEffects.IGNITE.get());
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    private static void dealDamage(LivingEntity entity, int amplifier) {
        float damage = BASE_DAMAGE + (DAMAGE_PER_LEVEL * amplifier);

        entity.hurt(entity.damageSources().onFire(), damage);
        entity.setRemainingFireTicks(FIRE_DURATION);

        entity.level().playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                FELSounds.IGNITE_EXPLODE.get(),
                SoundSource.HOSTILE,
                1.5f,
                1.0f
        );

        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GOLDEN_DUST,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    5, 0, 0, 0, 0);

            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    1, 0, 0, 0, 0.1f);

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

            serverLevel.sendParticles(ParticleTypes.FLAME,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    15, 0.5, 0.5, 0.5, 0.05);

            serverLevel.sendParticles(BRIGHT_GOLDEN_DUST,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    10, 0.3, 0.3, 0.3, 0.02);
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.IGNITE.get()) {
            damagedMap.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.IGNITE.get()) {
            damagedMap.remove(event.getEntity().getUUID());
        }
    }
}