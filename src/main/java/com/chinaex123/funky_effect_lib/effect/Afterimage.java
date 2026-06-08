package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.entity.AfterimageClone;
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

/** 残影效果：受到伤害后有概率生成一个分身吸引敌人 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Afterimage extends MobEffect {

    private static final float BASE_CHANCE = 0.25f; // 基础生成概率
    private static final float CHANCE_PER_LEVEL = 0.05f; // 每级增加的生成概率
    private static final int BASE_DURATION = 200; // 基础持续时间
    private static final int DURATION_PER_LEVEL = 100; // 每级增加的持续时间

    public Afterimage(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public static int getCloneDuration(int amplifier) {
        return BASE_DURATION + (amplifier * DURATION_PER_LEVEL);
    }

    public static float getCloneChance(int amplifier) {
        return Math.min(1.0f, BASE_CHANCE + (amplifier * CHANCE_PER_LEVEL));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        MobEffectInstance effect = player.getEffect(FELEffects.AFTERIMAGE.get());
        if (effect == null) {
            return;
        }

        float chance = getCloneChance(effect.getAmplifier());

        if (player.getRandom().nextFloat() < chance) {
            int duration = getCloneDuration(effect.getAmplifier());
            AfterimageClone.createClone(player, duration);
            player.removeEffect(FELEffects.AFTERIMAGE.get());
        }
    }
}