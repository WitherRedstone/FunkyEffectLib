package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;

/** 枯斩：攻击敌人可以虚弱敌人并对其造成凋零 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class WitheredSlash extends MobEffect {

    private static final int WITHER_BASE_DURATION_TICKS = 60; // 基础3秒
    private static final int WITHER_EXTRA_DURATION_PER_LEVEL = 20; // 每级增加1秒
    private static final int WEAKNESS_BASE_DURATION_TICKS = 100; // 基础5秒

    public WitheredSlash(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent.Pre event) {
        // 检查攻击者是否是玩家
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        // 检查玩家是否拥有 枯斩 效果
        var effectInstance = attacker.getEffect(FELEffects.WITHERED_SLASH);
        if (effectInstance != null) {
            LivingEntity target = event.getEntity();
            int amplifier = effectInstance.getAmplifier(); // 效果等级

            // 应用效果
            int witherDuration = WITHER_BASE_DURATION_TICKS + (amplifier * WITHER_EXTRA_DURATION_PER_LEVEL);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, witherDuration, amplifier, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_BASE_DURATION_TICKS, amplifier, false, false));
        }
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}