package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 透支治愈：治疗效果溢出部分会转化为临时生命值（吸收护盾）
 * <p>
 * 机制：
 * <ol>
 *   <li>溢出治疗量的50%转化为吸收护盾</li>
 *   <li>最大吸收量为40点</li>
 *   <li>仅当治疗效果导致生命值超过上限时触发</li>
 *   <li>吸收护盾会随时间自然衰减</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Overheal extends MobEffect {

    /** 溢出转化为吸收的比例 **/
    private static final float OVERHEAL_RATIO = 0.5f;
    /** 最大吸收量 **/
    private static final float MAX_ABSORPTION = 40.0f;

    public Overheal(int color) {
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

    /**
     * 生命恢复事件处理
     * 将溢出治疗量转化为吸收护盾
     *
     * @param event 生命恢复事件
     */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        // 检查是否拥有透支治愈效果
        var effect = entity.getEffect(FELEffects.OVERHEAL);
        if (effect == null) {
            return;
        }

        float healAmount = event.getAmount();
        float currentHealth = entity.getHealth();
        float maxHealth = entity.getMaxHealth();

        // 情况1：当前生命值已满，全部治疗量转化为吸收
        if (currentHealth >= maxHealth) {
            float overhealAmount = healAmount * OVERHEAL_RATIO;
            addAbsorption(entity, overhealAmount);
        }
        // 情况2：治疗会使生命值超过上限，仅溢出部分转化为吸收
        else if (currentHealth + healAmount > maxHealth) {
            float overhealAmount = (currentHealth + healAmount - maxHealth) * OVERHEAL_RATIO;
            addAbsorption(entity, overhealAmount);
        }
        // 情况3：治疗不会超过上限，不触发溢出效果
    }

    /**
     * 添加吸收护盾
     *
     * @param entity 目标实体
     * @param amount 吸收量
     */
    private static void addAbsorption(LivingEntity entity, float amount) {
        if (amount <= 0) {
            return;
        }

        float currentAbsorption = entity.getAbsorptionAmount();
        float newAbsorption = Math.min(currentAbsorption + amount, MAX_ABSORPTION);

        if (newAbsorption <= currentAbsorption) {
            return;
        }

        // 确保最大吸收属性值足够容纳新的吸收量
        AttributeInstance absorptionAttr = entity.getAttribute(Attributes.MAX_ABSORPTION);
        if (absorptionAttr != null) {
            absorptionAttr.setBaseValue(MAX_ABSORPTION);
        }

        entity.setAbsorptionAmount(newAbsorption);
    }
}