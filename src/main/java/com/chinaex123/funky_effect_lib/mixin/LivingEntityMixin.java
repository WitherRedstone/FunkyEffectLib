package com.chinaex123.funky_effect_lib.mixin;

import com.chinaex123.funky_effect_lib.client.renderer.effects.DangerSenseRender;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 混合类，用于修改实体是否发光 **/
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    /** 棷入方法，用于修改实体是否发光 **/
    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void onIsCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;

        boolean shouldGlow = false;

        // 如果 DangerSense 效果激活且实体在列表中，返回 true
        if (DangerSenseRender.isEffectActive() && DangerSenseRender.shouldEntityGlow(entity)) {
            shouldGlow = true;
        }

        cir.setReturnValue(shouldGlow);
    }
}