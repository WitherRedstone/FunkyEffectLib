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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 瓦解：大幅降低攻击力 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Sever extends MobEffect {

    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "sever_attack");

    private static final float BASE_ATTACK_DAMAGE_REDUCTION = -0.35f; // 基础攻击力降低
    private static final float ADDITIONAL_REDUCTION_PER_LEVEL = -0.10f; // 每级额外降低攻击力

    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>(); // 记录每个实体的上一个等级

    public Sever(int color) {
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
        MobEffectInstance effect = entity.getEffect(FELEffects.SEVER);

        if (effect == null) {
            clearAttackDamageReduction(entity);
            lastAmplifierMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        if (lastAmplifier == null || lastAmplifier != amplifier) {
            float reduction = BASE_ATTACK_DAMAGE_REDUCTION + (amplifier * ADDITIONAL_REDUCTION_PER_LEVEL);
            applyAttackDamageReduction(entity, reduction);
            lastAmplifierMap.put(entityId, amplifier);
        }
    }

    private static void applyAttackDamageReduction(LivingEntity entity, float reduction) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER);
            attackDamage.addTransientModifier(new AttributeModifier(
                    ATTACK_DAMAGE_MODIFIER,
                    reduction,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
    }

    private static void clearAttackDamageReduction(LivingEntity entity) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER);
        }
    }
}