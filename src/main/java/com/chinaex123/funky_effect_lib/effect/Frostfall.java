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

/** 霜降：攻击生物时，使生物获得霜寒效果 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Frostfall extends MobEffect {

    private static final int FROSTBITE_DURATION_TICKS = 100;  // 基础持续时间

    public Frostfall(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        if (attacker.level().isClientSide()) {
            return;
        }

        MobEffectInstance effect = attacker.getEffect(FELEffects.FROSTFALL.get());
        if (effect == null) {
            return;
        }

        int amplifier = effect.getAmplifier();
        LivingEntity target = event.getEntity();

        target.addEffect(new MobEffectInstance(FELEffects.FROSTBITE.get(), FROSTBITE_DURATION_TICKS, amplifier));
    }
}