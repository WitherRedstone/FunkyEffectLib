package com.chinaex123.funky_effect_lib.client.renderer.effects;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 黑暗效果渲染类
 * <p>
 * 功能：当玩家拥有黑暗类效果时，在视野上叠加黑暗纹理
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class DarknessRender {

    /** 黑暗效果的纹理路径 **/
    private static final ResourceLocation DARKNESS_TEXTURE = FunkyEffectLib.id("textures/misc/darkness_overlay.png");

    /** 纹理原始分辨率 **/
    private static final int TEXTURE_SIZE = 256;

    /** 基础透明度 */
    private static final float BASE_ALPHA = 0.62F;

    /** 每提升一级效果额外增加的浓度 **/
    private static final float ALPHA_PER_LEVEL = 0.13F;

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

        // F1 隐藏界面时不叠加，避免残留一层黑
        if (mc.options.hideGui) return;

        // 检查玩家是否拥有任意一种黑暗类效果
        Stream.of(FELEffects.CREEPING_DARKNESS, FELEffects.PERVADING_DARKNESS, FELEffects.DARKNESS)
                .map(mc.player::getEffect) // 获取效果实例
                .filter(Objects::nonNull) // 过滤空值
                .findFirst() // 取第一个有效效果
                .ifPresent(effect -> renderDarknessOverlay(event.getGuiGraphics(), effect));
    }

    /**
     * 渲染黑暗覆盖纹理
     * 在屏幕上方绘制半透明的黑暗纹理
     *
     * @param guiGraphics GUI绘图上下文
     * @param effect 黑暗效果实例
     */
    private static void renderDarknessOverlay(GuiGraphics guiGraphics, MobEffectInstance effect) {
        // 根据效果等级计算透明度，最大不超过 1.0
        float alpha = Math.min(BASE_ALPHA + effect.getAmplifier() * ALPHA_PER_LEVEL, 1.0F);

        // GUI 空间尺寸：不要用窗口像素尺寸，否则 GUI 缩放不是 1 时会画错
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        RenderSystem.enableBlend();
        // 必须显式设定混合函数，否则会继承上一段渲染残留的状态
        RenderSystem.defaultBlendFunc();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);

        // 纹理宽高填 256（贴图真实分辨率），U/V 就会从 0 映射到 1，等于整张拉满屏幕
        guiGraphics.blit(
                DARKNESS_TEXTURE,
                0, 0,               // 屏幕位置（左上角）
                width, height,      // 绘制宽度、高度
                0.0F, 0.0F,         // 纹理偏移
                TEXTURE_SIZE, TEXTURE_SIZE, // 取用的纹理区域
                TEXTURE_SIZE, TEXTURE_SIZE  // 纹理总分辨率
        );

        // 还原颜色，避免污染后面渲染的界面
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
}
