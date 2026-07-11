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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 蔓延黑暗客户端处理类 **/
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class CreepingDarknessClient {

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

        int colorText = ClientConfig.parseColor(ClientConfig.CREEPING_DARKNESS_COLOR_TEXT.get());
        int colorBackground = ClientConfig.parseColor(ClientConfig.CREEPING_DARKNESS_COLOR_BACKGROUND.get());
        int displayX = ClientConfig.CREEPING_DARKNESS_DISPLAY_X.get();
        int displayY = ClientConfig.CREEPING_DARKNESS_DISPLAY_Y.get();
        int padding = ClientConfig.CREEPING_DARKNESS_PADDING.get();

        // 文字部分
        String text = Component.translatable("gui.funky_effect_lib.creeping_darkness", stack).getString();
        int textWidth = font.width(text);
        int lineHeight = font.lineHeight;

        // 计算文本总宽度
        int bgX = displayX - padding;
        int bgY = displayY - padding / 2;
        int bgWidth = textWidth + padding * 2;
        int bgHeight = lineHeight + padding;

        guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, colorBackground);
        guiGraphics.drawString(font, text, displayX, displayY, colorText);
    }
}