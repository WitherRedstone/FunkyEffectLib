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

/** 冻结：冻结的目标无法移动和攻击。对该目标造成足够伤害会触发碎裂效果 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Freeze extends MobEffect {

    private static final String SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("freeze_speed".getBytes()).toString();

    private static final float SPEED_REDUCTION = -12.0f;
    private static final float BASE_DAMAGE_THRESHOLD = 30.0f; // 基础伤害阈值
    private static final float EXTRA_THRESHOLD_PER_LEVEL = 5.0f; // 每级额外伤害阈值

    private static final Map<UUID, Float> damageAccumulatorMap = new HashMap<>();
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
        if (entity.hasEffect(this)) {
            if (entity.canFreeze()) {
                entity.setIsInPowderSnow(true);
                entity.setTicksFrozen(entity.getTicksFrozen() + 20);
            }
            entity.setDeltaMovement(0, entity.getDeltaMovement().y(), 0);
            entity.setJumping(false);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        MobEffectInstance freezeEffect = entity.getEffect(FELEffects.FREEZE.get());
        if (freezeEffect == null) {
            return;
        }

        UUID entityId = entity.getUUID();
        float damage = event.getAmount();

        // 排除冻结效果造成的伤害
        if (event.getSource().is(DamageTypes.FREEZE)) {
            //event.setCanceled(true);
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

            // 添加碎裂效果
            entity.addEffect(new MobEffectInstance(FELEffects.SHATTERER.get(), 1, amplifier));

            // 清理累积伤害
            damageAccumulatorMap.remove(entityId);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(new BlockPos(0, 0, 0).getCenter(),
                100000, 100000, 100000))) {
            MobEffectInstance effect = entity.getEffect(FELEffects.FREEZE.get());

            UUID entityId = entity.getUUID();
            if (effect != null) {
                int ticks = entityTickMap.getOrDefault(entityId, 0) + 1;

                if (ticks >= 20) {
                    entityTickMap.put(entityId, 0);
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                            8, 0.4, 0.4, 0.4, 0.02);
                } else {
                    entityTickMap.put(entityId, ticks);
                }
            } else {
                entityTickMap.remove(entityId);
                damageAccumulatorMap.remove(entityId);
            }
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.FREEZE.get()) {
            LivingEntity entity = event.getEntity();
            entity.setIsInPowderSnow(false);
            entityTickMap.remove(entity.getUUID());
            damageAccumulatorMap.remove(entity.getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.FREEZE.get()) {
            LivingEntity entity = event.getEntity();
            entity.setIsInPowderSnow(false);
            entityTickMap.remove(entity.getUUID());
            damageAccumulatorMap.remove(entity.getUUID());
        }
    }

    /** 检查实体是否处于冻结状态 **/
    public static boolean isFrozen(LivingEntity entity) {
        return entity.hasEffect(FELEffects.FREEZE.get());
    }
}