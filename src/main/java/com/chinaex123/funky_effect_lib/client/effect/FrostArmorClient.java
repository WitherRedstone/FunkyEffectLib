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

/** 冰冻铠甲客户端处理类 **/
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class FrostArmorClient {

    private static final String SYMBOL_EMPTY = "□"; // 未充能符号
    private static final String SYMBOL_CHARGED = "■"; // 充能符号
    private static final int COLOR_SYMBOL_EMPTY = 0xAAAAAA; // 未充能符号颜色
    private static final int COLOR_SYMBOL_CHARGED = 0x55AAFF; // 充能符号颜色
    private static final int DISPLAY_TICKS = 60; // 未充能时显示的时间
    private static final int MAX_CRYSTALS = 10; // 固定最多10个

    private static final Map<UUID, Integer> CRYSTAL_CACHE = new ConcurrentHashMap<>();
    private static int displayTicks = 0;
    private static boolean isDisplaying = false;

    public static void setCrystalCount(UUID playerUuid, int count) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            CRYSTAL_CACHE.put(playerUuid, Math.min(count, MAX_CRYSTALS));
            isDisplaying = true;
            displayTicks = DISPLAY_TICKS;
        }
    }

    public static void clearCrystals(UUID playerUuid) {
        CRYSTAL_CACHE.remove(playerUuid);
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
        Integer crystalCount = CRYSTAL_CACHE.get(playerUuid);

        if (crystalCount == null) return;

        if (isDisplaying || crystalCount > 0) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Font font = minecraft.font;

            int colorText = ClientConfig.parseColor(ClientConfig.FROST_ARMOR_COLOR_TEXT.get());
            int colorBackground = ClientConfig.parseColor(ClientConfig.FROST_ARMOR_COLOR_BACKGROUND.get());
            int displayX = ClientConfig.FROST_ARMOR_DISPLAY_X.get();
            int displayY = ClientConfig.FROST_ARMOR_DISPLAY_Y.get();
            int padding = ClientConfig.FROST_ARMOR_PADDING.get();

            // 文字部分
            String text = Component.translatable("gui.funky_effect_lib.frost_armor").getString();
            int textWidth = font.width(text);
            int lineHeight = font.lineHeight;
            int symbolWidth = font.width(SYMBOL_CHARGED);

            // 每行显示5个，共2行
            int symbolsPerRow = 5;
            int totalRows = 2;
            int totalWidth = textWidth + symbolsPerRow * symbolWidth;
            int totalHeight = lineHeight * totalRows;

            // 绘制背景
            int bgX = displayX - padding;
            int bgY = displayY - padding / 2;
            int bgWidth = totalWidth + padding * 2;
            int bgHeight = totalHeight + padding;

            guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, colorBackground);

            // 计算文字垂直居中的 Y 位置
            int textY = displayY + (totalHeight - lineHeight) / 2;
            guiGraphics.drawString(font, text, displayX, textY, colorText);

            // 渲染符号（5x2 网格）
            for (int i = 0; i < MAX_CRYSTALS; i++) {
                int row = i / symbolsPerRow;
                int col = i % symbolsPerRow;
                boolean hasCrystal = i < crystalCount;

                String symbol = hasCrystal ? SYMBOL_CHARGED : SYMBOL_EMPTY;
                int color = hasCrystal ? COLOR_SYMBOL_CHARGED : COLOR_SYMBOL_EMPTY;

                int symbolX = displayX + textWidth + col * symbolWidth;
                int symbolY = displayY + row * lineHeight;

                guiGraphics.drawString(font, symbol, symbolX, symbolY, color);
            }
        }
    }
}