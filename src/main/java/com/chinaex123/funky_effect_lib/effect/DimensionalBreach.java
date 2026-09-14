package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELDamageTypes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 维度撕裂：这个维度正在逐渐撕碎你...
 * <p>
 * 机制：
 * <ol>
 *   <li>初始伤害为2点，每1.2秒（24刻）造成一次伤害</li>
 *   <li>每次伤害后伤害值增加2点，直到玩家死亡</li>
 *   <li>伤害类型为自定义的BREACH（撕裂）</li>
 *   <li>效果移除时重置伤害值</li>
 *   <li>适用于任何LivingEntity，不限于玩家</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class DimensionalBreach extends MobEffect {

    /** 初始伤害 **/
    private static final float INITIAL_DAMAGE = 2.0f;
    /** 每次伤害后的增量 **/
    private static final float DAMAGE_INCREMENT = 2.0f;
    /** 伤害间隔：1.2秒 = 24刻 **/
    private static final int DAMAGE_INTERVAL = 24;

    /** 当前伤害值 **/
    private static final String CURRENT_DAMAGE_KEY = "dimensional_breach_current_damage";

    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();

    public DimensionalBreach(int color) {
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

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.DIMENSIONAL_BREACH) {
            LivingEntity entity = event.getEntity();
            resetDamage(entity);
            tickCounterMap.remove(entity.getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.DIMENSIONAL_BREACH) {
            LivingEntity entity = event.getEntity();
            resetDamage(entity);
            tickCounterMap.remove(entity.getUUID());
        }
    }

    /**
     * 获取实体的当前伤害值
     *
     * @param entity 目标实体
     * @return 当前伤害值
     */
    private static float getCurrentDamage(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        if (persistentData.contains(CURRENT_DAMAGE_KEY)) {
            return persistentData.getFloat(CURRENT_DAMAGE_KEY);
        }
        return INITIAL_DAMAGE;
    }

    /**
     * 设置实体的当前伤害值
     *
     * @param entity 目标实体
     * @param damage 伤害值
     */
    private static void setCurrentDamage(LivingEntity entity, float damage) {
        entity.getPersistentData().putFloat(CURRENT_DAMAGE_KEY, damage);
    }

    /**
     * 重置实体的伤害值
     *
     * @param entity 目标实体
     */
    private static void resetDamage(LivingEntity entity) {
        entity.getPersistentData().remove(CURRENT_DAMAGE_KEY);
    }

    /**
     * 实体Tick事件处理
     * 管理维度撕裂效果的计时和伤害
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide()) {
            return;
        }

        UUID entityId = entity.getUUID();
        MobEffectInstance effect = entity.getEffect(FELEffects.DIMENSIONAL_BREACH);

        if (effect == null) {
            resetDamage(entity);
            tickCounterMap.remove(entityId);
            return;
        }

        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        if (tickCounter >= DAMAGE_INTERVAL) {
            float damage = getCurrentDamage(entity);

            DamageSource breachDamage = new DamageSource(
                    entity.level().registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(FELDamageTypes.BREACH)
            );

            entity.hurt(breachDamage, damage);

            float newDamage = damage + DAMAGE_INCREMENT;
            setCurrentDamage(entity, newDamage);
            tickCounter = 0;
        }

        tickCounterMap.put(entityId, tickCounter);
    }
}