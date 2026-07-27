package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

/**
 * 蜂刺：攻击有概率附加中毒效果，对已经中毒的目标伤害增加
 * <p>
 * 机制：
 * <ol>
 *   <li>攻击时有25%概率为目标附加中毒效果</li>
 *   <li>基础中毒持续100刻（5秒），每级增加50刻</li>
 *   <li>对已经中毒的目标造成1.5倍伤害</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Stinger extends MobEffect {

    /** 附加中毒的概率 **/
    private static final float POISON_CHANCE = 0.25f;
    /** 基础中毒持续时间 **/
    private static final int POISON_DURATION = 100;
    /** 对中毒目标的伤害倍率 **/
    private static final float POISONED_DAMAGE_MULTIPLIER = 1.5f;

    public Stinger(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 实体受伤事件处理
     * 攻击时施加中毒效果或增加对中毒目标的伤害
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();

        if (target.level().isClientSide()) {
            return;
        }

        var source = event.getSource();
        var attacker = source.getEntity();

        // 检查攻击者是否为LivingEntity
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        // 检查攻击者是否拥有蜂刺效果
        MobEffectInstance effect = livingAttacker.getEffect(FELEffects.STINGER.get());
        if (effect == null) {
            return;
        }

        int amplifier = effect.getAmplifier();
        boolean isTargetPoisoned = target.hasEffect(MobEffects.POISON);

        if (isTargetPoisoned) {
            // 目标已中毒：增加伤害
            float originalDamage = event.getAmount();
            float newDamage = originalDamage * POISONED_DAMAGE_MULTIPLIER;
            event.setAmount(newDamage);
        } else {
            // 目标未中毒：概率施加中毒效果
            if (livingAttacker.getRandom().nextFloat() < POISON_CHANCE) {
                // 持续时间：基础 + 等级 × 50刻
                int duration = POISON_DURATION + (amplifier * 50);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, amplifier));
            }
        }
    }
}