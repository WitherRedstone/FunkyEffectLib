package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELDamageTypes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 震颤：受到攻击时产生连锁闪光伤害周围的生物
 * <p>
 * 机制：
 * <ol>
 *   <li>受到伤害时触发连锁闪光</li>
 *   <li>冷却时间为50刻（2.5秒）</li>
 *   <li>连锁半径为8格，最多连锁5个目标</li>
 *   <li>基础伤害4点，每级增加2点</li>
 *   <li>产生闪光、烟花和粒子视觉效果</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Jolt extends MobEffect {

    /** 冷却时间 **/
    private static final int COOLDOWN_TICKS = 50;
    /** 连锁半径 **/
    private static final double CHAIN_RADIUS = 8.0;
    /** 最大连锁目标数 **/
    private static final int MAX_CHAIN_TARGETS = 5;
    /** 基础伤害 **/
    private static final float BASE_DAMAGE = 4.0f;
    /** 每级额外伤害 **/
    private static final float DAMAGE_PER_LEVEL = 2.0f;

    /** 缓存每个实体的冷却时间 **/
    private static final Map<UUID, Integer> cooldownMap = new HashMap<>();

    public Jolt(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 实体受伤事件处理
     * 受到伤害时触发连锁闪电
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();

        // 检查是否拥有震颤效果
        MobEffectInstance effect = target.getEffect(FELEffects.JOLT.get());
        if (effect == null || target.level().isClientSide()) {
            return;
        }

        UUID entityId = target.getUUID();
        int currentTick = target.tickCount;
        int lastTriggerTick = cooldownMap.getOrDefault(entityId, -COOLDOWN_TICKS);

        // 检查冷却时间
        if (currentTick - lastTriggerTick < COOLDOWN_TICKS) {
            return;
        }

        // 更新冷却时间
        cooldownMap.put(entityId, currentTick);

        int amplifier = effect.getAmplifier();
        // 计算伤害：基础 + 等级 × 每级加成
        float damage = BASE_DAMAGE + (DAMAGE_PER_LEVEL * amplifier);

        // 触发连锁闪电
        triggerChainLightning(target, damage);
    }

    /**
     * 触发连锁闪光
     *
     * @param source 触发源实体
     * @param damage 伤害值
     */
    private static void triggerChainLightning(LivingEntity source, float damage) {
        if (!(source.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 sourcePos = source.position();

        // 在源实体位置产生闪光效果
        spawnFlashEffect(level, sourcePos);

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

        // 创建震颤伤害源
        DamageSource joltDamage = new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(FELDamageTypes.JOIT)
        );

        // 对每个目标造成伤害并产生连锁视觉效果
        Vec3 previousPos = sourcePos;
        for (LivingEntity target : targets) {
            Vec3 targetPos = target.position();

            // 造成伤害
            target.hurt(joltDamage, damage);

            // 在目标位置产生闪光效果
            spawnFlashEffect(level, targetPos);

            // 产生连锁闪光视觉效果
            spawnChainFlashEffect(level, previousPos, targetPos);

            previousPos = targetPos;
        }
    }

    /**
     * 产生闪光粒子特效（单点）
     *
     * @param level 服务端世界
     * @param pos 粒子位置
     */
    private static void spawnFlashEffect(ServerLevel level, Vec3 pos) {
        // 闪光粒子
        level.sendParticles(ParticleTypes.FLASH,
                pos.x, pos.y + 1, pos.z,
                1, 0, 0, 0, 0);

        // 火花粒子（减少到8个）
        for (int i = 0; i < 8; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.5;
            double offsetY = level.random.nextDouble() * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.5;

            level.sendParticles(ParticleTypes.FIREWORK,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0, 0, 0, 0.1);
        }

        // 闪光星形粒子（减少到6个）
        for (int i = 0; i < 6; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 1.2;
            double offsetY = level.random.nextDouble() * 1.5;
            double offsetZ = (level.random.nextDouble() - 0.5) * 1.2;

            level.sendParticles(ParticleTypes.END_ROD,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0, 0, 0, 0.05);
        }

        // 白色烟雾粒子（减少到4个）
        for (int i = 0; i < 4; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = level.random.nextDouble() * 1.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;

            level.sendParticles(ParticleTypes.SMOKE,
                    pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                    1, 0, 0, 0, 0.02);
        }
    }

    /**
     * 产生连锁闪光视觉效果（两点之间）
     *
     * @param level 服务端世界
     * @param from 起始点
     * @param to 终点
     */
    private static void spawnChainFlashEffect(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 direction = to.subtract(from);
        double distance = direction.length();
        Vec3 step = direction.scale(1.0 / Math.max(distance, 1));

        // 沿着两点之间的线段产生闪光粒子
        for (double t = 0; t <= distance; t += 0.4) {
            Vec3 current = from.add(step.scale(t));

            // 添加随机偏移，让闪光看起来更自然
            double offsetX = (level.random.nextDouble() - 0.5) * 0.2;
            double offsetY = (level.random.nextDouble() - 0.5) * 0.2;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.2;

            level.sendParticles(ParticleTypes.END_ROD,
                    current.x + offsetX, current.y + 1 + offsetY, current.z + offsetZ,
                    1, 0, 0, 0, 0.01);
        }

        // 产生额外的闪光星形效果
        int particles = (int) (distance * 2);
        for (int i = 0; i < particles; i++) {
            double t = level.random.nextDouble();
            Vec3 current = from.add(step.scale(t * distance));

            level.sendParticles(ParticleTypes.FIREWORK,
                    current.x, current.y + 1, current.z,
                    1, 0, 0, 0, 0.02);
        }

        // 产生闪光连线效果
        for (int i = 0; i < (int) distance; i++) {
            double t = level.random.nextDouble();
            Vec3 current = from.add(step.scale(t * distance));

            level.sendParticles(ParticleTypes.FLASH,
                    current.x, current.y + 1, current.z,
                    1, 0, 0, 0, 0);
        }
    }

    /**
     * 玩家Tick事件处理
     * 效果结束时清理冷却记录
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        // 如果实体没有震颤效果，清除冷却记录
        if (!entity.hasEffect(FELEffects.JOLT.get())) {
            cooldownMap.remove(entity.getUUID());
        }
    }
}