package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** 硬化：按百分比减免所有来源的伤害 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Hardened extends MobEffect {

    private static final float REDUCTION_PER_LEVEL = 0.10f; // 每级额外减免伤害
    private static final float MAX_REDUCTION = 0.90f; // 最大减免伤害

    public Hardened(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        var effect = entity.getEffect(FELEffects.HARDENED);

        if (effect != null) {
            int amplifier = effect.getAmplifier();
            // 计算减伤百分比，最高60%
            float damageReduction = Math.min((amplifier + 1) * REDUCTION_PER_LEVEL, MAX_REDUCTION);

            if (damageReduction > 0) {
                float originalDamage = event.getOriginalDamage();
                float reducedDamage = originalDamage * (1.0F - damageReduction);
                event.setNewDamage(Math.max(0, reducedDamage));
            }
        }
    }
}