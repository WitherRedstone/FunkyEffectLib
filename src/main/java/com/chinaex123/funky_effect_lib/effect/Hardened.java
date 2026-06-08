package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 硬化：按百分比减免所有来源的伤害 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Hardened extends MobEffect {

    private static final float REDUCTION_PER_LEVEL = 0.10f; // 每级额外减免伤害
    private static final float MAX_REDUCTION = 0.90f; // 最大减免伤害

    public Hardened(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        MobEffectInstance effect = entity.getEffect(FELEffects.HARDENED.get());

        if (effect != null) {
            int amplifier = effect.getAmplifier();
            // 计算减伤百分比，最高 90%
            float damageReduction = Math.min((amplifier + 1) * REDUCTION_PER_LEVEL, MAX_REDUCTION);

            if (damageReduction > 0) {
                float originalDamage = event.getAmount();
                float reducedDamage = originalDamage * (1.0F - damageReduction);
                event.setAmount(Math.max(0, reducedDamage));
            }
        }
    }
}