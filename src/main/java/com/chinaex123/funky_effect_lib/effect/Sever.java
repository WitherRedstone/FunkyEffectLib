package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 瓦解：大幅降低攻击力 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Sever extends MobEffect {

    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("F8A9B0C1-2D3E-4F4A-5B6C-7D8E9F0A1B2C");
    private static final String ATTACK_DAMAGE_MODIFIER_STRING = UUID.nameUUIDFromBytes("sever_damage".getBytes()).toString();

    private static final float BASE_ATTACK_DAMAGE_REDUCTION = -0.35f; // 基础攻击力降低
    private static final float ADDITIONAL_REDUCTION_PER_LEVEL = -0.10f; // 每级额外降低攻击力

    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>(); // 记录每个实体的上一个等级

    public Sever(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (entity.level().isClientSide()) {
            return;
        }

        UUID entityId = entity.getUUID();
        MobEffectInstance effect = entity.getEffect(FELEffects.SEVER.get());

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
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
            attackDamage.addTransientModifier(new AttributeModifier(
                    ATTACK_DAMAGE_MODIFIER_UUID,
                    ATTACK_DAMAGE_MODIFIER_STRING,
                    reduction,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    private static void clearAttackDamageReduction(LivingEntity entity) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
        }
    }
}