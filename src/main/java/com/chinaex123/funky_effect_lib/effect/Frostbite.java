package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 霜寒：使生物冻结，根据等级产生不同效果
 * <p>
 * 机制：
 * <ol>
 *   <li>1级：减速15%</li>
 *   <li>2级及以上：冰冻效果（增加冰冻时间）</li>
 *   <li>持续生成雪花粒子效果</li>
 *   <li>效果移除或过期时自动清除减速修改器</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Frostbite extends MobEffect {

    private static final UUID FROSTBITE_MODIFIER_UUID = UUID.fromString("cece3398-f923-45eb-b959-b00ce61e32fd");
    private static final String FROSTBITE_MODIFIER_STRING = UUID.nameUUIDFromBytes("frostbite_slowdown".getBytes()).toString();

    /** 减速比例 **/
    private static final float SPEED_REDUCTION = -0.15f;
    /** 基础冰冻时间增量 **/
    private static final int BASE_FROZEN_TICKS = 40;
    /** 每级额外冰冻时间增量 **/
    private static final int EXTRA_FROZEN_PER_LEVEL = 40;

    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> entityTickMap = new HashMap<>();

    /** 减速属性修改器 **/
    private static final AttributeModifier FROSTBITE_MODIFIER = new AttributeModifier(
            FROSTBITE_MODIFIER_UUID,
            FROSTBITE_MODIFIER_STRING,
            SPEED_REDUCTION,
            AttributeModifier.Operation.MULTIPLY_TOTAL
    );

    public Frostbite(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 根据等级应用不同效果
        if (amplifier == 0) {
            // 1级：减速效果
            AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeed != null && !movementSpeed.hasModifier(FROSTBITE_MODIFIER)) {
                movementSpeed.addTransientModifier(FROSTBITE_MODIFIER);
            }
        } else if (entity.canFreeze()) {
            // 2级及以上：冰冻效果
            entity.setIsInPowderSnow(true);
            int frozenIncrease = BASE_FROZEN_TICKS + (amplifier * EXTRA_FROZEN_PER_LEVEL);
            entity.setTicksFrozen(entity.getTicksFrozen() + frozenIncrease);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 效果移除事件处理
     * 效果被手动移除时清除减速修改器
     *
     * @param event 效果移除事件
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.FROSTBITE.get()) {
            LivingEntity entity = event.getEntity();
            removeSpeedModifier(entity);
            entityTickMap.remove(entity.getUUID());
        }
    }

    /**
     * 效果过期事件处理
     * 效果自然过期时清除减速修改器
     *
     * @param event 效果过期事件
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.FROSTBITE.get()) {
            LivingEntity entity = event.getEntity();
            removeSpeedModifier(entity);
            entityTickMap.remove(entity.getUUID());
        }
    }

    /**
     * 移除速度减速修改器
     *
     * @param entity 目标实体
     */
    private static void removeSpeedModifier(LivingEntity entity) {
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(FROSTBITE_MODIFIER_UUID);
        }
    }

    /**
     * 玩家Tick事件处理
     * 生成雪花粒子效果并清理数据
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        MobEffectInstance effect = entity.getEffect(FELEffects.FROSTBITE.get());

        if (effect != null && !entity.level().isClientSide()) {
            // 生成雪花粒子
            UUID entityId = entity.getUUID();
            int ticks = entityTickMap.getOrDefault(entityId, 0) + 1;

            // 每20刻生成一次粒子（每秒1次）
            if (ticks >= 20) {
                entityTickMap.put(entityId, 0);
                if (entity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                            5, 0.3, 0.3, 0.3, 0.02);
                }
            } else {
                entityTickMap.put(entityId, ticks);
            }
        } else if (effect == null) {
            // 没有效果时，确保清理修改器和计时器
            if (entityTickMap.containsKey(entity.getUUID())) {
                removeSpeedModifier(entity);
                entityTickMap.remove(entity.getUUID());
            }
        }
    }
}