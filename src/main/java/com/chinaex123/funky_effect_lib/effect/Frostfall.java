package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** 霜降：攻击生物时，使生物获得霜寒效果 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Frostfall extends MobEffect {

    private static final int FROSTBITE_DURATION_TICKS = 100;  // 基础持续时间

    public Frostfall(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            var effect = attacker.getEffect(FELEffects.FROSTFALL);
            if (effect != null) {
                int amplifier = effect.getAmplifier();
                LivingEntity target = event.getEntity();

                target.addEffect(new MobEffectInstance(FELEffects.FROSTBITE, FROSTBITE_DURATION_TICKS, amplifier));
            }
        }
    }
}