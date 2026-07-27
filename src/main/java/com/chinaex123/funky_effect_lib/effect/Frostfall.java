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

/**
 * 霜降：攻击生物时，使生物获得霜寒效果
 * <p>
 * 机制：
 * <ol>
 *   <li>攻击时为目标施加霜寒效果</li>
 *   <li>霜寒效果持续100刻（5秒）</li>
 *   <li>霜寒效果的等级与霜降效果的等级相同</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Frostfall extends MobEffect {

    /** 霜寒效果持续时间 **/
    private static final int FROSTBITE_DURATION_TICKS = 100;

    public Frostfall(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    /**
     * 实体受伤事件处理
     * 攻击时为目标施加霜寒效果
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        // 检查攻击者是否为LivingEntity
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            // 检查攻击者是否拥有霜降效果
            var effect = attacker.getEffect(FELEffects.FROSTFALL);
            if (effect != null) {
                int amplifier = effect.getAmplifier();
                LivingEntity target = event.getEntity();

                // 为目标添加霜寒效果
                target.addEffect(new MobEffectInstance(FELEffects.FROSTBITE, FROSTBITE_DURATION_TICKS, amplifier));
            }
        }
    }
}