package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
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
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Frostbite extends MobEffect {

    private static final ResourceLocation FROSTBITE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "frostbite_slowdown");

    /** 减速比例 **/
    private static final float SPEED_REDUCTION = -0.15f;
    /** 基础冰冻时间增量 **/
    private static final int BASE_FROZEN_TICKS = 40;
    /** 每级额外冰冻时间增量 **/
    private static final int EXTRA_FROZEN_PER_LEVEL = 40;

    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> entityTickMap = new HashMap<>();

    public Frostbite(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (amplifier == 0) {
            // 1级：减速效果
            AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeed != null) {
                // 移除旧的修改器
                movementSpeed.removeModifier(FROSTBITE_MODIFIER);
                // 添加永久修改器（效果持续期间生效）
                movementSpeed.addPermanentModifier(new AttributeModifier(
                        FROSTBITE_MODIFIER,
                        SPEED_REDUCTION,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        } else if (entity.canFreeze()) {
            // 2级及以上：冰冻效果
            entity.setIsInPowderSnow(true);
            int frozenIncrease = BASE_FROZEN_TICKS + (amplifier * EXTRA_FROZEN_PER_LEVEL);
            entity.setTicksFrozen(entity.getTicksFrozen() + frozenIncrease);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 实体Tick事件处理
     * 为拥有霜寒效果的实体生成雪花粒子
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            var effect = entity.getEffect(FELEffects.FROSTBITE);

            if (effect != null && !entity.level().isClientSide()) {
                UUID entityId = entity.getUUID();
                int ticks = entityTickMap.getOrDefault(entityId, 0) + 1;

                // 每20刻生成一次粒子
                if (ticks >= 20) {
                    entityTickMap.put(entityId, 0);
                    ((ServerLevel) entity.level()).sendParticles(ParticleTypes.SNOWFLAKE,
                            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                            5, 0.3, 0.3, 0.3, 0.02);
                } else {
                    entityTickMap.put(entityId, ticks);
                }
            }
        }
    }
}