package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 冻结：冻结的目标无法移动和攻击。对该目标造成足够伤害会触发碎裂效果
 * <p>
 * 机制：
 * <ol>
 *   <li>移动速度降低-1200%，完全定身</li>
 *   <li>每Tick强制停止移动并禁用跳跃</li>
 *   <li>持续生成雪花粒子效果</li>
 *   <li>累积受到的伤害，达到阈值时触发碎裂效果</li>
 *   <li>基础伤害阈值30点，每级增加5点</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Freeze extends MobEffect {

    /** 移动速度修改器的名称 **/
    private static final String SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("freeze_speed".getBytes()).toString();

    /** 速度降低量 **/
    private static final float SPEED_REDUCTION = -12.0f;
    /** 基础伤害阈值 **/
    private static final float BASE_DAMAGE_THRESHOLD = 30.0f;
    /** 每级额外伤害阈值 **/
    private static final float EXTRA_THRESHOLD_PER_LEVEL = 5.0f;

    /** 缓存每个实体累积的伤害 **/
    private static final Map<UUID, Float> damageAccumulatorMap = new HashMap<>();
    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> entityTickMap = new HashMap<>();

    public Freeze(int color) {
        super(MobEffectCategory.HARMFUL, color);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_STRING,
                SPEED_REDUCTION,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 如果实体拥有冻结效果，应用冰冻效果
        if (entity.hasEffect(this)) {
            // 将实体标记为在细雪中（产生冰冻效果）
            if (entity.canFreeze()) {
                entity.setIsInPowderSnow(true);
                // 增加冰冻时间
                entity.setTicksFrozen(entity.getTicksFrozen() + 20);
            }
            // 强制停止移动
            entity.setDeltaMovement(0, entity.getDeltaMovement().y(), 0);
            entity.setJumping(false);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 实体受伤事件处理
     * 累积伤害，达到阈值时触发碎裂效果
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        // 检查实体是否拥有冻结效果
        MobEffectInstance freezeEffect = entity.getEffect(FELEffects.FREEZE.get());
        if (freezeEffect == null) {
            return;
        }

        UUID entityId = entity.getUUID();
        float damage = event.getAmount();

        // 排除冻结效果造成的伤害（防止无限循环）
        if (event.getSource().is(DamageTypes.FREEZE)) {
            return;
        }

        // 累积伤害
        float accumulatedDamage = damageAccumulatorMap.getOrDefault(entityId, 0f) + damage;
        damageAccumulatorMap.put(entityId, accumulatedDamage);

        // 计算伤害阈值
        int amplifier = freezeEffect.getAmplifier();
        float damageThreshold = BASE_DAMAGE_THRESHOLD + (EXTRA_THRESHOLD_PER_LEVEL * amplifier);

        // 如果累积伤害达到阈值，触发碎裂效果
        if (accumulatedDamage >= damageThreshold) {
            // 设置碎裂效果的触发伤害
            Shatterer.setTriggerDamage(entityId, damage);

            // 移除冻结效果
            entity.removeEffect(FELEffects.FREEZE.get());

            // 添加碎裂效果（持续1刻，用于触发一次效果）
            entity.addEffect(new MobEffectInstance(FELEffects.SHATTERER.get(), 1, amplifier));

            // 清理累积伤害
            damageAccumulatorMap.remove(entityId);
        }
    }

    /**
     * 世界Tick事件处理
     * 为冻结的实体生成雪花粒子
     *
     * @param event 世界Tick事件
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        // 遍历所有实体，为冻结的实体生成粒子
        for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(new BlockPos(0, 0, 0).getCenter(),
                100000, 100000, 100000))) {
            MobEffectInstance effect = entity.getEffect(FELEffects.FREEZE.get());

            UUID entityId = entity.getUUID();
            if (effect != null) {
                int ticks = entityTickMap.getOrDefault(entityId, 0) + 1;

                // 每20刻生成一次粒子
                if (ticks >= 20) {
                    entityTickMap.put(entityId, 0);
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                            8, 0.4, 0.4, 0.4, 0.02);
                } else {
                    entityTickMap.put(entityId, ticks);
                }
            } else {
                // 效果消失，清理数据
                entityTickMap.remove(entityId);
                damageAccumulatorMap.remove(entityId);
            }
        }
    }

    /**
     * 效果移除事件处理
     * 效果被手动移除时清理数据和状态
     *
     * @param event 效果移除事件
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.FREEZE.get()) {
            LivingEntity entity = event.getEntity();
            // 取消细雪状态
            entity.setIsInPowderSnow(false);
            entityTickMap.remove(entity.getUUID());
            damageAccumulatorMap.remove(entity.getUUID());
        }
    }

    /**
     * 效果过期事件处理
     * 效果自然过期时清理数据和状态
     *
     * @param event 效果过期事件
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.FREEZE.get()) {
            LivingEntity entity = event.getEntity();
            // 取消细雪状态
            entity.setIsInPowderSnow(false);
            entityTickMap.remove(entity.getUUID());
            damageAccumulatorMap.remove(entity.getUUID());
        }
    }

    /**
     * 检查实体是否处于冻结状态
     *
     * @param entity 目标实体
     * @return true表示处于冻结状态
     */
    public static boolean isFrozen(LivingEntity entity) {
        return entity.hasEffect(FELEffects.FREEZE.get());
    }
}