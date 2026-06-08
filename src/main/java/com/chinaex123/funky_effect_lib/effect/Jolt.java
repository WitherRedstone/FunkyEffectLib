package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELDamageTypes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 震颤：受到攻击时产生连锁闪电伤害周围的生物 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Jolt extends MobEffect {

    private static final int COOLDOWN_TICKS = 50; // 冷却时间
    private static final double CHAIN_RADIUS = 8.0; // 连锁半径
    private static final int MAX_CHAIN_TARGETS = 5; // 最多连锁5个目标
    private static final float BASE_DAMAGE = 4.0f; // 基础伤害
    private static final float DAMAGE_PER_LEVEL = 2.0f; // 每级额外伤害

    private static final Map<UUID, Integer> cooldownMap = new HashMap<>(); // 记录每个实体的冷却时间

    public Jolt(int color) {
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

    /**
     * 受到伤害时触发连锁闪电
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();

        // 检查是否有 Jolt 效果
        MobEffectInstance effect = target.getEffect(FELEffects.JOLT);
        if (effect == null || target.level().isClientSide()) {
            return;
        }

        UUID entityId = target.getUUID();
        int currentTick = target.tickCount;
        int lastTriggerTick = cooldownMap.getOrDefault(entityId, -COOLDOWN_TICKS);

        // 检查冷却
        if (currentTick - lastTriggerTick < COOLDOWN_TICKS) {
            return;
        }

        // 更新冷却
        cooldownMap.put(entityId, currentTick);

        int amplifier = effect.getAmplifier();
        float damage = BASE_DAMAGE + (DAMAGE_PER_LEVEL * amplifier);

        // 触发连锁闪电
        triggerChainLightning(target, damage);
    }

    /**
     * 触发连锁闪电
     */
    private static void triggerChainLightning(LivingEntity source, float damage) {
        ServerLevel level = (ServerLevel) source.level();
        Vec3 sourcePos = source.position();

        // 生成视觉闪电（无伤害）在源实体位置
        spawnVisualLightning(level, sourcePos);

        // 获取范围内的所有生物
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(sourcePos.x - CHAIN_RADIUS, sourcePos.y - CHAIN_RADIUS, sourcePos.z - CHAIN_RADIUS,
                        sourcePos.x + CHAIN_RADIUS, sourcePos.y + CHAIN_RADIUS, sourcePos.z + CHAIN_RADIUS),
                entity -> entity != source && entity.isAlive());

        // 限制最多连锁目标数
        if (targets.size() > MAX_CHAIN_TARGETS) {
            targets = targets.subList(0, MAX_CHAIN_TARGETS);
        }

        if (targets.isEmpty()) {
            return;
        }

        // 在源实体位置产生闪电粒子
        spawnLightningEffect(level, sourcePos);

        // 对每个目标造成伤害并产生连锁视觉效果
        Vec3 previousPos = sourcePos;
        for (int i = 0; i < targets.size(); i++) {
            LivingEntity target = targets.get(i);
            Vec3 targetPos = target.position();

            // 在每个目标位置也生成视觉闪电
            spawnVisualLightning(level, targetPos);

            // 造成伤害
            target.hurt(source.damageSources().source(FELDamageTypes.JOIT), damage);

            // 产生闪电连锁视觉效果
            spawnChainLightningEffect(level, previousPos, targetPos);

            // 在目标位置产生闪电粒子
            spawnLightningEffect(level, targetPos);

            previousPos = targetPos;
        }
    }

    /**
     * 生成视觉闪电（无伤害，纯视觉效果）
     */
    private static void spawnVisualLightning(ServerLevel level, Vec3 pos) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(pos.x, pos.y, pos.z);
            lightning.setVisualOnly(true);  // 只显示视觉效果，不造成伤害
            level.addFreshEntity(lightning);
        }
    }

    /**
     * 产生闪电特效（单点）
     */
    private static void spawnLightningEffect(ServerLevel level, Vec3 pos) {
        // 闪电粒子
        for (int i = 0; i < 15; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = level.random.nextDouble() * 2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;

            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0, 0, 0, 0.1);
        }

        // 闪光粒子
        level.sendParticles(ParticleTypes.FLASH,
                pos.x, pos.y + 1, pos.z,
                1, 0, 0, 0, 0);

        // 电光效果
        for (int i = 0; i < 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.2;
            double offsetY = level.random.nextDouble() * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.2;

            level.sendParticles(ParticleTypes.WAX_OFF,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0, 0, 0, 0.05);
        }
    }

    /**
     * 产生连锁闪电视觉效果（两点之间）
     */
    private static void spawnChainLightningEffect(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 direction = to.subtract(from);
        double distance = direction.length();
        Vec3 step = direction.scale(1.0 / Math.max(distance, 1));

        // 沿着两点之间的线段产生粒子
        for (double t = 0; t <= distance; t += 0.3) {
            Vec3 current = from.add(step.scale(t));

            // 添加随机偏移，让闪电看起来更自然
            double offsetX = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.3;

            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    current.x + offsetX, current.y + 1 + offsetY, current.z + offsetZ,
                    1, 0, 0, 0, 0.02);
        }

        // 产生额外的电光效果
        int particles = (int) (distance * 3);
        for (int i = 0; i < particles; i++) {
            double t = level.random.nextDouble();
            Vec3 current = from.add(step.scale(t * distance));

            level.sendParticles(ParticleTypes.END_ROD,
                    current.x, current.y + 1, current.z,
                    1, 0, 0, 0, 0.01);
        }
    }

    /**
     * 效果结束时清理冷却记录
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide()) {
            if (!entity.hasEffect(FELEffects.JOLT)) {
                cooldownMap.remove(entity.getUUID());
            }
        }
    }
}