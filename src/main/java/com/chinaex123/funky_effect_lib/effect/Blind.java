package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 致盲：生物将无法攻击和移动
 * <p>
 * 机制：
 * <ol>
 *   <li>将移动速度降低-1200%，使生物无法移动</li>
 *   <li>每Tick强制停止移动并禁用跳跃</li>
 *   <li>效果持续期间生物被完全定身</li>
 *   <li>可与其他效果共存（通过属性修改器实现）</li>
 * </ol>
 */
public class Blind extends MobEffect {

    /** 移动速度修改器的名称 **/
    private static final String SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("blind_speed".getBytes()).toString();

    /** 速度降低量 **/
    private static final float SPEED_REDUCTION = -12.0f;

    public Blind(int color) {
        super(MobEffectCategory.HARMFUL, color);

        // 添加移动速度修改器
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_STRING,
                SPEED_REDUCTION,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 强制停止水平移动
        entity.setDeltaMovement(0, entity.getDeltaMovement().y(), 0);
        // 禁用跳跃
        entity.setJumping(false);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 检查实体是否处于致盲状态
     *
     * @param entity 要检查的实体
     * @return true表示处于致盲状态，false表示未致盲
     */
    public static boolean isBlind(LivingEntity entity) {
        return entity.hasEffect(FELEffects.BLIND.get());
    }
}