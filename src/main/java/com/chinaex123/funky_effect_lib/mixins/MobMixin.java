package com.chinaex123.funky_effect_lib.mixins;

import com.chinaex123.funky_effect_lib.effect.Blind;
import com.chinaex123.funky_effect_lib.effect.Freeze;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 混合类，用于修改生物的目标设置 **/
@Mixin(Mob.class)
public abstract class MobMixin {

    /** 阻止生物设置攻击目标 **/
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void onSetTarget(LivingEntity target, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;

        // 如果生物处于致盲状态，不允许设置目标
        if (Blind.isBlind(mob)) {
            ci.cancel();
        }

        // 如果生物处于冻结状态，不允许设置目标
        if (Freeze.isFrozen(mob)) {
            ci.cancel();
        }
    }
}