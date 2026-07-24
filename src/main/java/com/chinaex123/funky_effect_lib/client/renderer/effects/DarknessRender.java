package com.chinaex123.funky_effect_lib.client.renderer.effects;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.Objects;
import java.util.stream.Stream;

/** 黑暗效果的渲染类：拥有效果时，在玩家视野上渲染黑暗效果 **/
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class DarknessRender {

    // 黑暗效果的纹理路径
    private static final ResourceLocation DARKNESS_TEXTURE = FunkyEffectLib.id("textures/misc/darkness_overlay.png");

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Stream.of(FELEffects.CREEPING_DARKNESS, FELEffects.PERVADING_DARKNESS, FELEffects.DARKNESS)
                .map(mc.player::getEffect)
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(effect -> renderDarknessOverlay(event.getGuiGraphics(), mc, effect));
    }

    private static void renderDarknessOverlay(GuiGraphics guiGraphics, Minecraft mc, MobEffectInstance effect) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, DARKNESS_TEXTURE);
        RenderSystem.enableBlend();

        // 根据效果等级调整透明度 (等级越高越明显)
        int amplifier = effect.getAmplifier();
        float alpha = Math.min(0.3f + (amplifier * 0.07f), 1.0f);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        // 绘制纹理，覆盖整个屏幕
        guiGraphics.blit(
                DARKNESS_TEXTURE,
                0, 0,  // 位置
                0, 0,  // 纹理偏移
                mc.getWindow().getGuiScaledWidth(),   // 宽度
                mc.getWindow().getGuiScaledHeight(),  // 高度
                mc.getWindow().getGuiScaledWidth(),   // 纹理宽度
                mc.getWindow().getGuiScaledHeight()   // 纹理高度
        );

        RenderSystem.disableBlend();
    }
}