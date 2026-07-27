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

/**
 * 硬化：按百分比减免所有来源的伤害
 * <p>
 * 机制：
 * <ol>
 *   <li>每级减免5%伤害</li>
 *   <li>最大减免50%伤害</li>
 *   <li>减免所有来源的伤害（近战、远程、魔法等）</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Hardened extends MobEffect {

    /** 每级伤害减免 **/
    private static final float REDUCTION_PER_LEVEL = 0.05f;
    /** 最大伤害减免 **/
    private static final float MAX_REDUCTION = 0.50f;

    public Hardened(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    /**
     * 实体受伤事件处理
     * 根据硬化等级减免伤害
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        // 检查实体是否拥有硬化效果
        MobEffectInstance effect = entity.getEffect(FELEffects.HARDENED.get());

        if (effect != null) {
            int amplifier = effect.getAmplifier();
            // 计算减伤百分比：等级 + 1 × 每级减免，最高90%
            float damageReduction = Math.min((amplifier + 1) * REDUCTION_PER_LEVEL, MAX_REDUCTION);

            if (damageReduction > 0) {
                float originalDamage = event.getAmount();
                float reducedDamage = originalDamage * (1.0F - damageReduction);
                // 设置减免后的伤害，最低为0
                event.setAmount(Math.max(0, reducedDamage));
            }
        }
    }
}