package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 弥漫暗影客户端处理类 **/
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class PervadingDarknessClient {

    private static final int COLOR_TEXT = 0xFF5555; // 显示颜色
    private static final int COLOR_BACKGROUND = 0x88000000; // 背景颜色
    private static final int DISPLAY_X = 10; // 显示位置X
    private static final int DISPLAY_Y = 100; // 显示位置Y
    private static final int PADDING = 4; // 背景内边距

    private static final Map<UUID, Integer> STACK_CACHE = new ConcurrentHashMap<>();

    public static void setStack(UUID playerUuid, int stack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            if (stack <= 0) {
                STACK_CACHE.remove(playerUuid);
            } else {
                STACK_CACHE.put(playerUuid, stack);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Integer stack = STACK_CACHE.get(playerUuid);

        if (stack == null || stack <= 0) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = minecraft.font;

        String text = Component.translatable("gui.funky_effect_lib.pervading_darkness").getString()
                + " x" + stack;

        int textWidth = font.width(text);
        int lineHeight = font.lineHeight;

        int bgX = DISPLAY_X - PADDING;
        int bgY = DISPLAY_Y - PADDING / 2;
        int bgWidth = textWidth + PADDING * 2;
        int bgHeight = lineHeight + PADDING;

        guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, COLOR_BACKGROUND);
        guiGraphics.drawString(font, text, DISPLAY_X, DISPLAY_Y, COLOR_TEXT);
    }
}