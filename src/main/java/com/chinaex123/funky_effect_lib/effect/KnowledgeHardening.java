package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 知识固化：耐久损耗将转换为经验值消耗
 * <p>
 * 机制：
 * <ol>
 *   <li>每点耐久消耗基础经验值为10点</li>
 *   <li>每级减少2点经验消耗</li>
 *   <li>耐久损耗时消耗经验值替代物品耐久</li>
 *   <li>需要配合耐久修改器实现具体功能</li>
 * </ol>
 */
public class KnowledgeHardening extends MobEffect {

    /** 基础每点耐久消耗的经验值 **/
    public static final int BASE_XP_PER_DURABILITY = 10;

    /** 每级减少的经验消耗 **/
    public static final int XP_REDUCTION_PER_LEVEL = 2;

    public KnowledgeHardening(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }
}