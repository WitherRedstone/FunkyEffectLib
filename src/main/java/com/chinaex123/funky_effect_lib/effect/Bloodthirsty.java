package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

/** 嗜血：攻击时，恢复相当于造成伤害10%的生命值 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Bloodthirsty extends MobEffect {

    private static final float BASE_HEAL_RATIO = 0.15F; // 基础吸血比例
    private static final float EXTRA_HEAL_PER_LEVEL = 0.10F; // 每级额外吸血比例

    public Bloodthirsty(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        if (attacker.level().isClientSide()) {
            return;
        }

        MobEffectInstance effect = attacker.getEffect(FELEffects.BLOODTHIRSTY.get());
        if (effect == null) {
            return;
        }

        int amplifier = effect.getAmplifier();

        // 计算恢复量
        float healRatio = BASE_HEAL_RATIO + (EXTRA_HEAL_PER_LEVEL * amplifier);
        float healAmount = event.getAmount() * healRatio;

        // 治疗攻击者
        attacker.heal(healAmount);
    }
}