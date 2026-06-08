package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 灼烧：每秒叠加一级并造成伤害，每满10级触发一次点燃 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Scorch extends MobEffect {

    private static final int TICKS_PER_STACK = 20; // 每秒叠加一级
    private static final int TRIGGER_INTERVAL = 10; // 每10级触发一次
    private static final int MAX_SCORCH_LEVEL = 100; // 灼烧最高100级
    private static final int MAX_IGNITE_LEVEL = 10; // 点燃最高10级
    private static final int POST_MAX_TICKS = 100; // 达到100级后每5秒触发一次
    private static final float BASE_DAMAGE = 2.0f; // 固定伤害2点
    private static final int FIRE_DURATION = 100; // 生物着火的时间
    private static final int FIRE_TICKS = 60; // 保持燃烧状态的时间

    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>(); // 每个实体的 tick 计数
    private static final Map<UUID, Integer> postMaxTickMap = new HashMap<>(); // 达到100级后的 tick 计数

    public Scorch(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            UUID entityId = entity.getUUID();

            // 让生物持续着火（每tick刷新燃烧时间）
            if (entity.getRemainingFireTicks() < 40) {
                entity.setRemainingFireTicks(FIRE_TICKS);
            }

            // 获取当前效果等级（amplifier）
            int currentLevel = amplifier;

            // 如果已经达到最高等级，进入特殊模式
            if (currentLevel >= MAX_SCORCH_LEVEL) {
                handlePostMaxMode(entity, entityId);
                return;
            }

            // 未达到最高等级，继续叠加逻辑
            int ticks = tickCounterMap.getOrDefault(entityId, 0) + 1;

            if (ticks >= TICKS_PER_STACK) {
                tickCounterMap.put(entityId, 0);

                // 新等级 = 当前等级 + 1
                int newLevel = currentLevel + 1;

                entity.hurt(entity.damageSources().onFire(), BASE_DAMAGE);

                // 获取当前效果的持续时间
                MobEffectInstance currentEffect = entity.getEffect(FELEffects.SCORCH.get());
                int duration = currentEffect != null ? currentEffect.getDuration() : 200;

                // 更新效果等级
                entity.addEffect(new MobEffectInstance(FELEffects.SCORCH.get(), duration, newLevel, false, true, true));

                // 检查是否达到触发条件（每10级触发一次点燃）
                if (newLevel % TRIGGER_INTERVAL == 0) {
                    int triggerTimes = newLevel / TRIGGER_INTERVAL;
                    int igniteLevel = Math.min(triggerTimes - 1, MAX_IGNITE_LEVEL - 1);

                    // 触发点燃效果
                    triggerIgnite(entity, igniteLevel);
                }

                // 检查是否刚刚达到100级
                if (newLevel == MAX_SCORCH_LEVEL) {
                    onReachMaxLevel(entity);
                }
            } else {
                tickCounterMap.put(entityId, ticks);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 处理100级后的模式
     */
    private static void handlePostMaxMode(LivingEntity entity, UUID entityId) {
        int postTicks = postMaxTickMap.getOrDefault(entityId, 0) + 1;

        if (postTicks >= POST_MAX_TICKS) {
            postMaxTickMap.put(entityId, 0);
            // 触发10级点燃
            triggerIgnite(entity, MAX_IGNITE_LEVEL - 1);
        } else {
            postMaxTickMap.put(entityId, postTicks);
        }
    }

    /**
     * 达到100级时的特效
     */
    private static void onReachMaxLevel(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    3, 0.5, 0.5, 0.5, 0.2);

            serverLevel.sendParticles(ParticleTypes.FLAME,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    50, 1.0, 1.0, 1.0, 0.2);
        }

        entity.level().playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.WITHER_SPAWN,
                SoundSource.HOSTILE,
                1.0f, 1.0f
        );
    }

    /**
     * 触发点燃效果
     */
    private static void triggerIgnite(LivingEntity entity, int level) {
        if (entity.level().isClientSide()) return;

        entity.addEffect(new MobEffectInstance(FELEffects.IGNITE.get(), 1, Math.max(level, 0)));

        // 让生物着火
        entity.setRemainingFireTicks(FIRE_DURATION);
    }

    /**
     * 效果结束时清理数据
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (!entity.hasEffect(FELEffects.SCORCH.get())) {
            tickCounterMap.remove(entity.getUUID());
            postMaxTickMap.remove(entity.getUUID());
        }
    }
}