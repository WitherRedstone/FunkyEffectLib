package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;

/** 蜂刺：攻击有概率附加中毒效果，对已经中毒的目标伤害增加 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Stinger extends MobEffect {

    private static final float POISON_CHANCE = 0.25f; // 附加中毒的概率
    private static final int POISON_DURATION = 100; // 中毒持续时间
    private static final float POISONED_DAMAGE_MULTIPLIER = 1.5f; // 对中毒目标的伤害倍率

    public Stinger(int color) {
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
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        var source = event.getSource();
        var attacker = source.getEntity();

        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        var effect = livingAttacker.getEffect(FELEffects.STINGER);
        if (effect == null) {
            return;
        }

        int amplifier = effect.getAmplifier();
        boolean isTargetPoisoned = target.hasEffect(MobEffects.POISON);

        if (isTargetPoisoned) {
            float originalDamage = event.getOriginalDamage();
            float newDamage = originalDamage * POISONED_DAMAGE_MULTIPLIER;
            event.setNewDamage(newDamage);
        } else {
            if (livingAttacker.getRandom().nextFloat() < POISON_CHANCE) {
                int duration = POISON_DURATION + (amplifier * 50);
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, duration, amplifier));
            }
        }
    }
}