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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 不稳定：受到伤害时产生爆炸，爆炸会向周围实体传播效果 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Volatile extends MobEffect {

    private static final int COOLDOWN_TICKS = 50; // 爆炸冷却时间
    private static final float BASE_DAMAGE = 6.0f; // 基础伤害
    private static final float DAMAGE_PER_LEVEL = 3.0f; // 每级额外伤害
    private static final float EXPLOSION_RADIUS = 3.0f; // 爆炸半径

    private static final int SPREAD_COOLDOWN_TICKS = 100; // 传播效果的冷却时间
    private static final int SPREAD_DURATION = 200; // 传播效果的持续时间
    private static final int SPREAD_AMPLIFIER = 0; // 传播效果的等级
    private static final float SPREAD_RADIUS = 5.0f; // 传播范围半径
    private static final int MAX_SPREAD_COUNT = 3; // 最大传播次数

    private static final Map<UUID, Integer> cooldownMap = new HashMap<>();
    private static final Map<UUID, Integer> spreadCountMap = new HashMap<>(); // 记录每个实体的传播次数

    public Volatile(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 受到伤害时触发爆炸
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        if (!entity.hasEffect(FELEffects.VOLATILE.get())) {
            return;
        }

        UUID entityId = entity.getUUID();
        int lastExplosionTick = cooldownMap.getOrDefault(entityId, -COOLDOWN_TICKS);
        int currentTick = entity.tickCount;

        if (currentTick - lastExplosionTick >= COOLDOWN_TICKS) {
            cooldownMap.put(entityId, currentTick);
            explode(entity, 0); // 初始传播次数为0
        }
    }

    /**
     * 爆炸方法 - 造成伤害并向周围实体传播效果
     * @param entity 爆炸源实体
     * @param spreadCount 当前传播次数
     */
    public static void explode(LivingEntity entity, int spreadCount) {
        if (entity.level().isClientSide()) return;

        if (!(entity.level() instanceof ServerLevel level)) return;

        // 超过最大传播次数则停止传播
        if (spreadCount >= MAX_SPREAD_COUNT) {
            return;
        }

        MobEffectInstance effect = entity.getEffect(FELEffects.VOLATILE.get());
        int amplifier = effect != null ? effect.getAmplifier() : 0;

        float damage = BASE_DAMAGE + (DAMAGE_PER_LEVEL * amplifier);
        float radius = EXPLOSION_RADIUS + (amplifier * 0.5f);

        // 收集爆炸范围内的所有实体
        AABB explosionBounds = new AABB(
                entity.getX() - radius, entity.getY() - radius, entity.getZ() - radius,
                entity.getX() + radius, entity.getY() + radius, entity.getZ() + radius
        );

        List<Entity> nearbyEntities = level.getEntities(entity, explosionBounds,
                e -> e instanceof LivingEntity && e != entity);

        // 对爆炸范围内所有实体造成伤害
        for (Entity target : nearbyEntities) {
            LivingEntity livingTarget = (LivingEntity) target;
            double distance = target.distanceTo(entity);
            if (distance <= radius) {
                float damageReduction = (float) (1.0 - (distance / radius));
                float finalDamage = damage * damageReduction;
                livingTarget.hurt(livingTarget.damageSources().explosion(entity, entity), finalDamage);

                // 向周围实体传播效果
                spreadEffectToNearby(level, livingTarget, entity, spreadCount + 1);
            }
        }

        // 对爆炸源本身也造成伤害
        entity.hurt(entity.damageSources().explosion(entity, entity), damage * 0.5f);

        // 播放主爆炸音效
        level.playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                1.5f,
                0.8f
        );

        // 主爆炸粒子
        level.sendParticles(ParticleTypes.EXPLOSION,
                entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                1, 0, 0, 0, 0.1f);

        // 根据传播次数增加额外粒子效果
        if (spreadCount > 0) {
            level.sendParticles(ParticleTypes.SONIC_BOOM,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    2, 0.5f, 0.5f, 0.5f, 0.1f);
        }
    }

    /**
     * 向周围实体传播 Volatile 效果
     * @param level 世界
     * @param center 中心实体
     * @param source 原始爆炸源
     * @param newSpreadCount 新的传播次数
     */
    private static void spreadEffectToNearby(ServerLevel level, LivingEntity center, LivingEntity source, int newSpreadCount) {
        UUID centerId = center.getUUID();

        // 检查冷却
        int lastSpreadTick = spreadCountMap.getOrDefault(centerId, -SPREAD_COOLDOWN_TICKS);
        int currentTick = center.tickCount;

        if (currentTick - lastSpreadTick < SPREAD_COOLDOWN_TICKS) {
            return;
        }

        // 收集传播范围内的所有实体
        AABB spreadBounds = new AABB(
                center.getX() - SPREAD_RADIUS, center.getY() - SPREAD_RADIUS, center.getZ() - SPREAD_RADIUS,
                center.getX() + SPREAD_RADIUS, center.getY() + SPREAD_RADIUS, center.getZ() + SPREAD_RADIUS
        );

        List<Entity> nearbyEntities = level.getEntities(center, spreadBounds,
                e -> e instanceof LivingEntity && e != center && e != source);

        // 为范围内的每个实体添加 Volatile 效果
        for (Entity target : nearbyEntities) {
            LivingEntity livingTarget = (LivingEntity) target;
            double distance = target.distanceTo(center);

            if (distance <= SPREAD_RADIUS) {
                // 根据距离计算效果持续时间（越近时间越长）
                int durationModifier = (int) ((1.0 - distance / SPREAD_RADIUS) * SPREAD_DURATION);
                int finalDuration = Math.max(SPREAD_DURATION / 2, durationModifier);

                // 添加效果
                livingTarget.addEffect(new MobEffectInstance(
                        FELEffects.VOLATILE.get(),
                        finalDuration,
                        SPREAD_AMPLIFIER,
                        false,  // 是否显示粒子
                        true    // 是否显示图标
                ));

                spreadCountMap.put(livingTarget.getUUID(), currentTick);

                // 传播效果粒子提示
                level.sendParticles(ParticleTypes.PORTAL,
                        livingTarget.getX(), livingTarget.getY() + livingTarget.getBbHeight() / 2, livingTarget.getZ(),
                        5, 0.3f, 0.3f, 0.3f, 0.1f);

                // 可选：对传播到的实体造成额外的小伤害
                // livingTarget.hurt(livingTarget.damageSources().magic(), 2.0f);
            }
        }

        // 记录传播冷却
        spreadCountMap.put(centerId, currentTick);

        // 传播音效
        level.playSound(null,
                center.getX(), center.getY(), center.getZ(),
                SoundEvents.BEACON_POWER_SELECT,
                SoundSource.HOSTILE,
                0.6f,
                1.5f
        );
    }

    /**
     * 效果结束时清理冷却记录
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (!entity.hasEffect(FELEffects.VOLATILE.get())) {
            cooldownMap.remove(entity.getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.VOLATILE.get()) {
            cooldownMap.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.VOLATILE.get()) {
            cooldownMap.remove(event.getEntity().getUUID());
        }
    }
}