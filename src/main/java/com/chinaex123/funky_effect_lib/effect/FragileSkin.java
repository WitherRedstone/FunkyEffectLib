package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 脆弱皮肤：减少生物的护甲值
 * <p>
 * 机制：
 * <ol>
 *   <li>固定减少2点护甲值</li>
 *   <li>护甲值减少会降低物理伤害减免能力</li>
 *   <li>通过属性修改器实现，效果持续期间生效</li>
 *   <li>效果移除后护甲值恢复正常</li>
 * </ol>
 */
public class FragileSkin extends MobEffect {

    private static final ResourceLocation ARMOR_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "fragile_skin_armor");

    /** 护甲减少量 **/
    public static final double BASE_ARMOR_REDUCTION = -2;

    public FragileSkin(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        this.addAttributeModifier(
                Attributes.ARMOR,
                ARMOR_MODIFIER,
                BASE_ARMOR_REDUCTION,
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}