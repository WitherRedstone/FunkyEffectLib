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

/** 电光充能客户端处理类 **/
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class BoltChargeClient {

    private static final int MAX_CHARGES = 10; // 最大充能次数

    /** 缓存每个玩家的充能层数 **/
    private static final Map<UUID, Integer> CHARGE_CACHE = new ConcurrentHashMap<>();

    /** 设置指定玩家的充能层数 **/
    public static void setChargeCount(UUID playerUuid, int count) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            CHARGE_CACHE.put(playerUuid, Math.min(count, MAX_CHARGES));
        }
    }

    /** 清除指定玩家的充能数据 **/
    public static void clearCharges(UUID playerUuid) {
        CHARGE_CACHE.remove(playerUuid);
    }

    /** 在游戏界面上渲染充能层数显示 **/
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Integer chargeCount = CHARGE_CACHE.get(playerUuid);

        if (chargeCount == null || chargeCount <= 0) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = minecraft.font;

        int colorText = ClientConfig.parseColor(ClientConfig.BOLT_CHARGE_COLOR_TEXT.get());
        int colorBackground = ClientConfig.parseColor(ClientConfig.BOLT_CHARGE_COLOR_BACKGROUND.get());
        int displayX = ClientConfig.BOLT_CHARGE_DISPLAY_X.get();
        int displayY = ClientConfig.BOLT_CHARGE_DISPLAY_Y.get();
        int padding = ClientConfig.BOLT_CHARGE_PADDING.get();

        // 文字部分
        String text = Component.translatable("gui.funky_effect_lib.bolt_charge", chargeCount).getString();
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