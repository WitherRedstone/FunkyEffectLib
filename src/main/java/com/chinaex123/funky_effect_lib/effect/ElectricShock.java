package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELDamageTypes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
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

/** 触电：降低移动速度并持续造成伤害 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class ElectricShock extends MobEffect {

    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "electric_shock_speed");

    private static final float BASE_SPEED_REDUCTION = -0.15f; // 基础减少的移动速度
    private static final float ADDITIONAL_REDUCTION_PER_LEVEL = -0.1f; // 每级减少的移动速度
    private static final float BASE_DAMAGE = 2.0f; // 基础伤害
    private static final float ADDITIONAL_DAMAGE_PER_LEVEL = 2.0f; // 每级伤害量
    private static final int DAMAGE_INTERVAL = 20; // 伤害间隔

    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>();
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();

    public ElectricShock(int color) {
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
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide()) {
            return;
        }

        UUID entityId = entity.getUUID();
        MobEffectInstance effect = entity.getEffect(FELEffects.ELECTRIC_SHOCK);

        if (effect == null) {
            clearAttributes(entity);
            lastAmplifierMap.remove(entityId);
            tickCounterMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        if (lastAmplifier == null || lastAmplifier != amplifier) {
            float speedReduction = BASE_SPEED_REDUCTION + (amplifier * ADDITIONAL_REDUCTION_PER_LEVEL);
            applyAttributes(entity, speedReduction);
            lastAmplifierMap.put(entityId, amplifier);
        }

        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        if (tickCounter >= DAMAGE_INTERVAL) {
            float damage = BASE_DAMAGE + (amplifier * ADDITIONAL_DAMAGE_PER_LEVEL);
            entity.hurt(entity.damageSources().source(FELDamageTypes.ELECTRIC_SHOCK), damage);
            tickCounter = 0;
        }

        tickCounterMap.put(entityId, tickCounter);
    }

    private static void applyAttributes(LivingEntity entity, float speedReduction) {
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER);
            movementSpeed.addTransientModifier(new AttributeModifier(
                    MOVEMENT_SPEED_MODIFIER,
                    speedReduction,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
    }

    private static void clearAttributes(LivingEntity entity) {
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER);
        }
    }
}