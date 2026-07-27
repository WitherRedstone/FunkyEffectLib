package com.chinaex123.funky_effect_lib.mixins;

import com.chinaex123.funky_effect_lib.effect.Blind;
import com.chinaex123.funky_effect_lib.effect.Freeze;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 生物实体混入类
 */
@Mixin(Mob.class)
public abstract class MobMixin {

    /**
     * 阻止生物设置攻击目标
     * 在 setTarget 方法执行前注入，检查生物状态
     *
     * @param target 要设置的目标实体
     * @param ci 回调信息（用于取消原方法）
     */
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void onSetTarget(LivingEntity target, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;

        // 如果生物处于致盲状态，不允许设置目标
        if (Blind.isBlind(mob)) {
            ci.cancel();
            return;
        }

        // 如果生物处于冻结状态，不允许设置目标
        if (Freeze.isFrozen(mob)) {
            ci.cancel();
        }
    }
}