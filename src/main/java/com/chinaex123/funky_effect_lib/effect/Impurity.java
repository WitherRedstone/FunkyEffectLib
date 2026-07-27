package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * 污秽：减少攻击力和护甲值
 * <p>
 * 机制：
 * <ol>
 *   <li>减少15%护甲值</li>
 *   <li>减少15%移动速度</li>
 *   <li>通过属性修改器实现，效果持续期间生效</li>
 *   <li>效果移除后属性恢复正常</li>
 * </ol>
 */
public class Impurity extends MobEffect {

    private static final String IMPURITY_ARMOR_MODIFIER_STRING = UUID.nameUUIDFromBytes("impurity_armor".getBytes()).toString();
    private static final String IMPURITY_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("impurity_speed".getBytes()).toString();

    /** 护甲减少比例 **/
    private static final float ARMOR_REDUCTION = -0.15f;
    /** 移动速度减少比例 **/
    private static final float SPEED_REDUCTION = -0.15f;

    public Impurity(int color) {
        super(MobEffectCategory.HARMFUL, color);

        // 护甲值减少
        this.addAttributeModifier(
                Attributes.ARMOR,
                IMPURITY_ARMOR_MODIFIER_STRING,
                ARMOR_REDUCTION,
                AttributeModifier.Operation.MULTIPLY_BASE
        );

        // 移动速度减少
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                IMPURITY_SPEED_MODIFIER_STRING,
                SPEED_REDUCTION,
                AttributeModifier.Operation.MULTIPLY_BASE
        );
    }
}