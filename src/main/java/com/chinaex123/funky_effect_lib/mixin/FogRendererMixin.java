package com.chinaex123.funky_effect_lib.mixin;

import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 混合类，用于修改雾渲染 **/
@Mixin(FogRenderer.class)
public class FogRendererMixin {

    /** 注入方法，用于修改雾渲染 **/
    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void onSetupFog(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance, boolean shouldCreateFog, float partialTick, CallbackInfo ci) {
        Entity entity = camera.getEntity();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        // 检查是否有 LavaVision 效果
        if (!living.hasEffect(FELEffects.LAVA_VISION.get())) {
            return;
        }

        // 检查是否在岩浆中
        if (camera.getFluidInCamera() == FogType.LAVA) {
            RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
            RenderSystem.setShaderFogShape(FogShape.CYLINDER);

            ci.cancel();
        }
    }
}