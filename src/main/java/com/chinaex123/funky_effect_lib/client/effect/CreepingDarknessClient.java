package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 蔓延黑暗客户端处理类
 * <p>
 * 功能：在客户端显示蔓延黑暗的层数HUD
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class CreepingDarknessClient {

    /** 缓存每个玩家的蔓延黑暗层数 **/
    private static final Map<UUID, Integer> STACK_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的蔓延黑暗层数
     * 仅在客户端玩家自身匹配时更新缓存
     *
     * @param playerUuid 玩家UUID
     * @param stack 层数
     */
    public static void setStack(UUID playerUuid, int stack) {
        Minecraft minecraft = Minecraft.getInstance();
        // 只缓存本地玩家的数据
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            if (stack <= 0) {
                // 层数为0时移除缓存
                STACK_CACHE.remove(playerUuid);
            } else {
                STACK_CACHE.put(playerUuid, stack);
            }
        }
    }

    /**
     * 在游戏界面上渲染蔓延黑暗层数显示
     * 在游戏画面渲染结束后绘制HUD
     *
     * @param event 渲染GUI事件
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Integer stack = STACK_CACHE.get(playerUuid);

        // 如果没有层数或层数为0，不显示
        if (stack == null || stack <= 0) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = minecraft.font;

        // 从配置读取显示参数
        int colorText = ClientConfig.parseColor(ClientConfig.CREEPING_DARKNESS_COLOR_TEXT.get());
        int colorBackground = ClientConfig.parseColor(ClientConfig.CREEPING_DARKNESS_COLOR_BACKGROUND.get());
        int displayX = ClientConfig.CREEPING_DARKNESS_DISPLAY_X.get();
        int displayY = ClientConfig.CREEPING_DARKNESS_DISPLAY_Y.get();
        int padding = ClientConfig.CREEPING_DARKNESS_PADDING.get();
        double scale = ClientConfig.GLOBAL_SCALE.get();

        // 准备显示文本
        String text = Component.translatable("gui.funky_effect_lib.creeping_darkness", stack).getString();
        int textWidth = font.width(text);
        int lineHeight = font.lineHeight;

        // 应用缩放后的尺寸
        int scaledTextWidth = (int) (textWidth * scale);
        int scaledLineHeight = (int) (lineHeight * scale);
        int scaledPadding = (int) (padding * scale);

        // 计算背景位置和大小（在原始坐标系中）
        int bgX = displayX - scaledPadding;
        int bgY = displayY - scaledPadding / 2;
        int bgWidth = scaledTextWidth + scaledPadding * 2;
        int bgHeight = scaledLineHeight + scaledPadding;

        // 绘制半透明背景
        guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, colorBackground);

        // 绘制文字（使用缩放变换）
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(displayX, displayY, 0);
        guiGraphics.pose().scale((float) scale, (float) scale, 1.0f);
        // 在缩放后的坐标系中从 (0,0) 开始绘制文字
        guiGraphics.drawString(font, text, 0, 0, colorText);
        guiGraphics.pose().popPose();
    }
}