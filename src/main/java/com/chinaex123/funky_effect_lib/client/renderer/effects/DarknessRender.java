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

/**
 * 黑暗效果渲染类（NeoForge版）
 * <p>
 * 功能：当玩家拥有黑暗类效果时，在视野上叠加黑暗纹理
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class DarknessRender {

    /** 黑暗效果的纹理路径 **/
    private static final ResourceLocation DARKNESS_TEXTURE = FunkyEffectLib.id("textures/misc/darkness_overlay.png");

    /**
     * 渲染GUI事件处理
     * 如果玩家拥有黑暗类效果，渲染黑暗覆盖纹理
     *
     * @param event 渲染GUI事件
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 检查玩家是否拥有任意一种黑暗类效果
        Stream.of(FELEffects.CREEPING_DARKNESS, FELEffects.PERVADING_DARKNESS, FELEffects.DARKNESS)
                .map(mc.player::getEffect) // 获取效果实例
                .filter(Objects::nonNull) // 过滤空值
                .findFirst() // 取第一个有效效果
                .ifPresent(effect -> renderDarknessOverlay(event.getGuiGraphics(), mc, effect));
    }

    /**
     * 渲染黑暗覆盖纹理
     * 在屏幕上方绘制半透明的黑暗纹理
     *
     * @param guiGraphics GUI绘图上下文
     * @param mc Minecraft客户端实例
     * @param effect 黑暗效果实例
     */
    private static void renderDarknessOverlay(GuiGraphics guiGraphics, Minecraft mc, MobEffectInstance effect) {
        // 设置着色器
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, DARKNESS_TEXTURE);
        RenderSystem.enableBlend();

        // 根据效果等级计算透明度（等级越高越暗）
        int amplifier = effect.getAmplifier();
        // 基础透明度0.3，每级增加0.07，最大不超过1.0
        float alpha = Math.min(0.3f + (amplifier * 0.07f), 1.0f);
        // 设置颜色和透明度
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        // 绘制黑暗纹理，覆盖整个屏幕
        guiGraphics.blit(
                DARKNESS_TEXTURE,
                0, 0,  // 屏幕位置（左上角）
                0, 0,  // 纹理偏移
                mc.getWindow().getGuiScaledWidth(),   // 绘制宽度
                mc.getWindow().getGuiScaledHeight(),  // 绘制高度
                mc.getWindow().getGuiScaledWidth(),   // 纹理宽度
                mc.getWindow().getGuiScaledHeight()   // 纹理高度
        );

        // 关闭混合
        RenderSystem.disableBlend();
    }
}