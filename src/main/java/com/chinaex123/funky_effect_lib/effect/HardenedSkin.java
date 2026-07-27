package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

/**
 * 硬化皮肤：增加生物的护甲值
 * <p>
 * 机制：
 * <ol>
 *   <li>固定增加2点护甲值</li>
 *   <li>护甲值增加会提升物理伤害减免能力</li>
 *   <li>通过属性修改器实现，效果持续期间生效</li>
 *   <li>效果移除后护甲值恢复正常</li>
 * </ol>
 */
public class HardenedSkin extends MobEffect {

    private static final String ARMOR_MODIFIER_STRING = UUID.nameUUIDFromBytes("hardened_skin".getBytes()).toString();

    /** 护甲增加量 **/
    public static final double ARMOR_BONUS = 2;

    public HardenedSkin(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        // 添加护甲属性修改器
        this.addAttributeModifier(
                Attributes.ARMOR,
                ARMOR_MODIFIER_STRING,
                ARMOR_BONUS,
                AttributeModifier.Operation.ADDITION
        );
    }
}