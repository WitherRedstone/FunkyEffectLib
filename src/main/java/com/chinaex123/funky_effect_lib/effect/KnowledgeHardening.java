package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.spongepowered.asm.mixin.Unique;

/** 知识固化：耐久损耗将转换为经验值消耗 **/
public class KnowledgeHardening extends MobEffect {

    public static final int BASE_XP_PER_DURABILITY = 10; // 基础每耐久消耗经验
    public static final int XP_REDUCTION_PER_LEVEL = 2; // 每级减少的经验

    public KnowledgeHardening(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }
}