package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 疼痛：大幅降低移动速度并每秒减少饥饿值 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Ache extends MobEffect {

    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "ache_speed");

    private static final float BASE_SPEED_REDUCTION = -0.35f; // 基础移动速度减少
    private static final float ADDITIONAL_REDUCTION_PER_LEVEL = -0.05f; // 每级额外移动速度减少
    private static final int HUNGER_COST_INTERVAL = 50; // 减少饥饿值间隔

    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>();
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();

    public Ache(int color) {
        super(MobEffectCategory.NEUTRAL, color);
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
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // 如果玩家有 镇静 效果，跳过 疼痛 的所有逻辑并清除已有的 疼痛
        if (player.hasEffect(FELEffects.CALM) && player.hasEffect(FELEffects.ACHE)) {
            player.removeEffect(FELEffects.ACHE);
            clearAttributes(player);
            lastAmplifierMap.remove(player.getUUID());
            tickCounterMap.remove(player.getUUID());
            return;
        }

        MobEffectInstance effect = player.getEffect(FELEffects.ACHE);

        if (effect == null) {
            clearAttributes(player);
            lastAmplifierMap.remove(player.getUUID());
            tickCounterMap.remove(player.getUUID());
            return;
        }

        int amplifier = effect.getAmplifier();
        UUID entityId = player.getUUID();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        if (lastAmplifier == null || lastAmplifier != amplifier) {
            float speedReduction = BASE_SPEED_REDUCTION + (amplifier * ADDITIONAL_REDUCTION_PER_LEVEL);
            applyAttributes(player, speedReduction);
            lastAmplifierMap.put(entityId, amplifier);
        }

        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        if (tickCounter >= HUNGER_COST_INTERVAL) {
            player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - 1));
            tickCounter = 0;
        }

        tickCounterMap.put(entityId, tickCounter);
    }

    private static void applyAttributes(Player player, float speedReduction) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER);
            movementSpeed.addTransientModifier(new AttributeModifier(
                    MOVEMENT_SPEED_MODIFIER,
                    speedReduction,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    private static void clearAttributes(Player player) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER);
        }
    }
}