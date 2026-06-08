package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

/** 透支治愈：治疗效果溢出部分会转化为临时生命值 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Overheal extends MobEffect {

    private static final float OVERHEAL_RATIO = 0.5f; // 溢出转化为吸收
    private static final float MAX_ABSORPTION = 40.0f; // 最大吸收量

    public Overheal(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        MobEffectInstance effect = entity.getEffect(FELEffects.OVERHEAL.get());
        if (effect == null) {
            return;
        }

        float healAmount = event.getAmount();
        float currentHealth = entity.getHealth();
        float maxHealth = entity.getMaxHealth();

        if (currentHealth >= maxHealth) {
            float overhealAmount = healAmount * OVERHEAL_RATIO;
            addAbsorption(entity, overhealAmount);
        } else if (currentHealth + healAmount > maxHealth) {
            float overhealAmount = (currentHealth + healAmount - maxHealth) * OVERHEAL_RATIO;
            addAbsorption(entity, overhealAmount);
        }
    }

    private static void addAbsorption(LivingEntity entity, float amount) {
        if (amount <= 0) {
            return;
        }

        float currentAbsorption = entity.getAbsorptionAmount();
        float newAbsorption = Math.min(currentAbsorption + amount, MAX_ABSORPTION);

        if (newAbsorption <= currentAbsorption) {
            return;
        }

        entity.setAbsorptionAmount(newAbsorption);
    }
}