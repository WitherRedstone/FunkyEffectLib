package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.init.FELAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
public class Hardened extends MobEffect {

    private static final UUID MARDENED_MODIFIER_UUID = UUID.fromString("e53ec32f-223b-43b4-b6be-c1afef663f6b");
    private static final String HARDENED_MODIFIER_STRING = UUID.nameUUIDFromBytes("hardened_damage_reduction".getBytes()).toString();

    /** 每级伤害减免 **/
    private static final float REDUCTION_PER_LEVEL = 0.05f;
    /** 最大伤害减免 **/
    private static final float MAX_REDUCTION = 0.50f;

    public Hardened(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION.get());
        if (attribute != null) {
            // 计算减伤百分比：等级 + 1 × 每级减免，最高50%
            double damageReduction = Math.min((amplifier + 1) * REDUCTION_PER_LEVEL, MAX_REDUCTION);
            
            AttributeModifier modifier = new AttributeModifier(
                    MARDENED_MODIFIER_UUID,
                    HARDENED_MODIFIER_STRING,
                    damageReduction,
                    AttributeModifier.Operation.ADDITION
            );
            attribute.addPermanentModifier(modifier);
        }
        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION.get());
        if (attribute != null) {
            attribute.removeModifier(MARDENED_MODIFIER_UUID);
        }
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }
}