package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

/**
 * 压制：生物将停止移动并降低护甲值
 * <p>
 * 机制：
 * <ol>
 *   <li>移动速度降低-1200%，完全定身</li>
 *   <li>每级降低25%护甲值</li>
 *   <li>通过属性修改器实现，效果持续期间生效</li>
 *   <li>效果移除后属性恢复正常</li>
 * </ol>
 */
public class Suppression extends MobEffect {

    private static final ResourceLocation SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "suppression_speed");
    private static final ResourceLocation ARMOR_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "suppression_armor");

    /** 速度降低量 **/
    private static final float SPEED_REDUCTION = -12.0f;
    /** 每级护甲降低 **/
    private static final float ARMOR_REDUCTION_PER_LEVEL = -0.25f;

    public Suppression(int color) {
        super(MobEffectCategory.HARMFUL, color);

        // 移动速度修改器：完全定身
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER,
                SPEED_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // 护甲修改器：降低护甲值
        this.addAttributeModifier(
                Attributes.ARMOR,
                ARMOR_MODIFIER,
                ARMOR_REDUCTION_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
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