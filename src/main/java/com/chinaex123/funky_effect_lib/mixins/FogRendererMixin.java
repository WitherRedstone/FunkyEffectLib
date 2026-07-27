package com.chinaex123.funky_effect_lib.mixins;

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

/**
 * 雾渲染器混入类
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {

    /**
     * 设置雾的渲染参数
     * 在雾渲染设置阶段注入，修改熔岩中的雾效
     *
     * @param camera 相机对象
     * @param fogMode 雾模式
     * @param farPlaneDistance 远平面距离
     * @param shouldCreateFog 是否应该创建雾
     * @param partialTick 部分Tick时间
     * @param ci 回调信息（用于取消原方法）
     */
    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void onSetupFog(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance, boolean shouldCreateFog, float partialTick, CallbackInfo ci) {
        Entity entity = camera.getEntity();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        // 检查是否拥有熔岩视野效果
        if (!living.hasEffect(FELEffects.LAVA_VISION.get())) {
            return;
        }

        // 检查相机是否在熔岩中
        if (camera.getFluidInCamera() == FogType.LAVA) {
            // 将雾的起始和结束距离设置为最大值，消除雾效
            RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
            RenderSystem.setShaderFogShape(FogShape.CYLINDER);

            // 取消原方法的执行，使用自定义雾设置
            ci.cancel();
        }
    }
}