package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** 钢铁意志：免疫虚弱和挖掘疲劳，提升击退抗性 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class IronWill extends MobEffect {

    private static final String IRON_WILL_KNOCKBACK_RESISTANCE_MODIFIER_STRING = UUID.nameUUIDFromBytes("iron_will_knockback_resistance".getBytes()).toString();

    private static final float BASE_KNOCKBACK_RESISTANCE = 0.25f; // 基础击退抗性

    public IronWill(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        this.addAttributeModifier(
                Attributes.KNOCKBACK_RESISTANCE,
                IRON_WILL_KNOCKBACK_RESISTANCE_MODIFIER_STRING,
                BASE_KNOCKBACK_RESISTANCE,
                AttributeModifier.Operation.MULTIPLY_BASE
        );
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

        MobEffectInstance effect = entity.getEffect(FELEffects.IRON_WILL.get());
        if (effect == null) {
            return;
        }

        removeNegativeEffects(entity);
    }

    private static void removeNegativeEffects(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.WEAKNESS)) {
            entity.removeEffect(MobEffects.WEAKNESS);
        }
        if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            entity.removeEffect(MobEffects.DIG_SLOWDOWN);
        }
    }
}