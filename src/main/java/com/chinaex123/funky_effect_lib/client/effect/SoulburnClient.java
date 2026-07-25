package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 魂燃客户端处理类 **/
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class SoulburnClient {

    private static final String SYMBOL_EMPTY = "□"; // 未充能符号
    private static final String SYMBOL_CHARGED = "■"; // 充能符号
    private static final int COLOR_SYMBOL_EMPTY = 0xAAAAAA; // 未充能符号颜色
    private static final int COLOR_SYMBOL_CHARGED = 0x55FF55; // 充能符号颜色
    private static final int DISPLAY_TICKS = 60; // 未充能时显示的时间

    private static int displayTicks = 0;
    private static boolean isDisplaying = false;

    private static final Map<UUID, Boolean> CHARGE_CACHE = new ConcurrentHashMap<>();

    public static void setChargeState(UUID playerUuid, boolean hasCharge) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            CHARGE_CACHE.put(playerUuid, hasCharge);
            isDisplaying = true;
            displayTicks = DISPLAY_TICKS;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (displayTicks > 0) {
            displayTicks--;
            if (displayTicks <= 0) {
                isDisplaying = false;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Boolean hasCharge = CHARGE_CACHE.get(playerUuid);

        if (hasCharge == null) return;

        if (isDisplaying || hasCharge) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Font font = minecraft.font;

            int colorText = ClientConfig.parseColor(ClientConfig.SOULBURN_COLOR_TEXT.get());
            int colorBackground = ClientConfig.parseColor(ClientConfig.SOULBURN_COLOR_BACKGROUND.get());
            int displayX = ClientConfig.SOULBURN_DISPLAY_X.get();
            int displayY = ClientConfig.SOULBURN_DISPLAY_Y.get();
            int padding = ClientConfig.SOULBURN_PADDING.get();
            double scale = ClientConfig.GLOBAL_SCALE.get();

            // 文字部分（在原始尺寸下计算）
            String text = Component.translatable("gui.funky_effect_lib.soulburn").getString() + " ";
            // 符号部分
            String symbol = hasCharge ? SYMBOL_CHARGED : SYMBOL_EMPTY;
            int symbolColor = hasCharge ? COLOR_SYMBOL_CHARGED : COLOR_SYMBOL_EMPTY;

            // 计算文本总宽度
            int textWidth = font.width(text);
            int symbolWidth = font.width(symbol);
            int totalWidth = textWidth + symbolWidth;
            int height = font.lineHeight;

            // 应用缩放后的尺寸
            int scaledTotalWidth = (int) (totalWidth * scale);
            int scaledHeight = (int) (height * scale);
            int scaledPadding = (int) (padding * scale);

            // 背景位置（使用缩放后的尺寸，在原始坐标系中计算）
            int bgX = displayX - scaledPadding;
            int bgY = displayY - scaledPadding / 2;
            int bgWidth = scaledTotalWidth + scaledPadding * 2;
            int bgHeight = scaledHeight + scaledPadding;

            // 绘制半透明黑色背景（原始坐标系）
            guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, colorBackground);

            // 绘制文字（先平移到目标位置，再缩放）
            guiGraphics.pose().pushPose();
            // 先平移到文字左上角位置，再缩放
            guiGraphics.pose().translate(displayX, displayY, 0);
            guiGraphics.pose().scale((float) scale, (float) scale, 1.0f);
            // 在缩放后的坐标系中从 (0,0) 开始绘制
            guiGraphics.drawString(font, text, 0, 0, colorText);
            guiGraphics.drawString(font, symbol, textWidth, 0, symbolColor);
            guiGraphics.pose().popPose();
        }
    }
}