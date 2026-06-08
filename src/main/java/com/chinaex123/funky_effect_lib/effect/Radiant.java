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

/** 焕光：大幅提高攻击力 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Radiant extends MobEffect {

    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "radiant_attack");

    private static final float BASE_ATTACK_DAMAGE_INCREASE = 0.35f; // 基础攻击力增加
    private static final float ADDITIONAL_INCREASE_PER_LEVEL = 0.10f; // 每级额外增加攻击力

    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>(); // 记录每个实体的上一个等级

    public Radiant(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
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
        MobEffectInstance effect = entity.getEffect(FELEffects.RADIANT);

        if (effect == null) {
            clearAttackDamageIncrease(entity);
            lastAmplifierMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        if (lastAmplifier == null || lastAmplifier != amplifier) {
            float increase = BASE_ATTACK_DAMAGE_INCREASE + (amplifier * ADDITIONAL_INCREASE_PER_LEVEL);
            applyAttackDamageIncrease(entity, increase);
            lastAmplifierMap.put(entityId, amplifier);
        }
    }

    private static void applyAttackDamageIncrease(LivingEntity entity, float increase) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER);
            attackDamage.addTransientModifier(new AttributeModifier(
                    ATTACK_DAMAGE_MODIFIER,
                    increase,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
    }

    private static void clearAttackDamageIncrease(LivingEntity entity) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER);
        }
    }
}