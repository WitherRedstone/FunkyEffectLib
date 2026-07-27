package com.chinaex123.funky_effect_lib.mixin;

import com.chinaex123.funky_effect_lib.client.renderer.effects.DangerSenseRender;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 生物实体混入类
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /**
     * 修改实体发光判定
     * 在 isCurrentlyGlowing 方法返回时注入，添加危险感知的发光逻辑
     *
     * @param cir 回调信息返回对象
     */
    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void onIsCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;

        // 如果原版已返回true（如拥有发光效果），或危险感知效果激活且实体在列表中，则返回true
        boolean shouldGlow = cir.getReturnValue() || (DangerSenseRender.isEffectActive() && DangerSenseRender.shouldEntityGlow(entity));

        cir.setReturnValue(shouldGlow);
    }
}