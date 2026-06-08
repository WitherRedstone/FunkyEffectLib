package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;

/** 晶化护盾：受到伤害时，有概率将部分伤害转化为经验值消耗 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class CrystalShield extends MobEffect {

    private static final float CONVERSION_CHANCE = 0.5f; // 基础转换概率
    private static final float CONVERSION_RATIO = 0.5f; // 基础转换比例
    private static final int XP_PER_DAMAGE = 10; // 每点伤害消耗的经验值
    private static final float CHANCE_PER_LEVEL = 0.05f; // 每级增加的转换概率
    private static final float RATIO_PER_LEVEL = 0.05f; // 每级增加的转换比例

    public CrystalShield(int color) {
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
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        var effect = player.getEffect(FELEffects.CRYSTAL_SHIELD);
        if (effect == null) {
            return;
        }

        int amplifier = effect.getAmplifier();
        float conversionChance = CONVERSION_CHANCE + (amplifier * CHANCE_PER_LEVEL);
        float conversionRatio = CONVERSION_RATIO + (amplifier * RATIO_PER_LEVEL);
        conversionRatio = Math.min(conversionRatio, 0.9f);

        if (player.getRandom().nextFloat() < conversionChance) {
            float originalDamage = event.getOriginalDamage();
            float convertedDamage = originalDamage * conversionRatio;
            float remainingDamage = originalDamage - convertedDamage;

            int xpCost = (int) Math.ceil(convertedDamage * XP_PER_DAMAGE);
            int currentXp = getPlayerTotalExperience(player);

            if (currentXp >= xpCost) {
                giveExperience(player, -xpCost);
                event.setNewDamage(remainingDamage);
            }
        }
    }

    private static int getPlayerTotalExperience(Player player) {
        return player.totalExperience;
    }

    private static void giveExperience(Player player, int amount) {
        player.giveExperiencePoints(amount);
    }
}