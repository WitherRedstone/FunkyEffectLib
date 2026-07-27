package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 狂怒：增加攻击速度和伤害
 * <p>
 * 机制：
 * <ol>
 *   <li>攻击速度增加10%</li>
 *   <li>攻击力增加15%</li>
 *   <li>通过属性修改器实现，效果持续期间生效</li>
 *   <li>效果移除后属性恢复正常</li>
 * </ol>
 */
public class Rage extends MobEffect {

    private static final String ATTACK_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("rage_speed".getBytes()).toString();
    private static final String ATTACK_DAMAGE_MODIFIER_STRING = UUID.nameUUIDFromBytes("rage_attack".getBytes()).toString();

    /** 基础攻击速度增加 **/
    private static final float BASE_ATTACK_SPEED_MODIFIER = 0.1f;
    /** 基础攻击力增加 **/
    private static final float BASE_ATTACK_DAMAGE_MODIFIER = 0.15f;

    public Rage(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        // 增加攻击速度
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_MODIFIER_STRING,
                BASE_ATTACK_SPEED_MODIFIER,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        // 增加攻击力
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER_STRING,
                BASE_ATTACK_DAMAGE_MODIFIER,
                AttributeModifier.Operation.MULTIPLY_BASE
        );
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}