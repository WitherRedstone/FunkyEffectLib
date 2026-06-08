package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 魂燃客户端处理类 **/
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class SoulburnClient {

    private static final String SYMBOL_EMPTY = "□"; // 未充能符号
    private static final String SYMBOL_CHARGED = "■"; // 充能符号
    private static final int COLOR_SYMBOL_EMPTY = 0xAAAAAA; // 未充能符号颜色
    private static final int COLOR_SYMBOL_CHARGED = 0x55FF55; // 充能符号颜色
    private static final int COLOR_TEXT = 0xFFFFFF; // 文字颜色白色
    private static final int COLOR_BACKGROUND = 0x88000000; // 背景颜色
    private static final int DISPLAY_X = 10; // 显示位置X
    private static final int DISPLAY_Y = 90; // 显示位置Y
    private static final int DISPLAY_TICKS = 60; // 未充能时显示的时间
    private static final int PADDING = 4; // 背景内边距

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
    public static void onClientTick(ClientTickEvent.Post event) {
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

            // 文字部分
            String text = Component.translatable("gui.funky_effect_lib.soulburn").getString() + " ";
            // 符号部分
            String symbol = hasCharge ? SYMBOL_CHARGED : SYMBOL_EMPTY;
            int symbolColor = hasCharge ? COLOR_SYMBOL_CHARGED : COLOR_SYMBOL_EMPTY;

            // 计算文本总宽度
            int textWidth = font.width(text);
            int symbolWidth = font.width(symbol);
            int totalWidth = textWidth + symbolWidth;
            int height = font.lineHeight;

            // 绘制半透明黑色背景
            guiGraphics.fill(
                    DISPLAY_X - PADDING,
                    DISPLAY_Y - PADDING / 2,
                    DISPLAY_X + totalWidth + PADDING,
                    DISPLAY_Y + height + PADDING / 2,
                    COLOR_BACKGROUND
            );

            // 先渲染文字（白色）
            guiGraphics.drawString(font, text, DISPLAY_X, DISPLAY_Y, COLOR_TEXT);
            // 再渲染符号（带颜色）
            guiGraphics.drawString(font, symbol, DISPLAY_X + textWidth, DISPLAY_Y, symbolColor);
        }
    }
}