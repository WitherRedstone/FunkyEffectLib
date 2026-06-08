package com.chinaex123.funky_effect_lib.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 镇静：生效期间不会获得疼痛效果 **/
public class Calm extends MobEffect {
    public Calm(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
