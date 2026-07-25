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
        double scale = ClientConfig.GLOBAL_SCALE.get();

        // 文字部分（在原始尺寸下计算）
        String text = Component.translatable("gui.funky_effect_lib.bolt_charge", chargeCount).getString();
        int textWidth = font.width(text);
        int lineHeight = font.lineHeight;

        // 应用缩放后的尺寸
        int scaledTextWidth = (int) (textWidth * scale);
        int scaledLineHeight = (int) (lineHeight * scale);
        int scaledPadding = (int) (padding * scale);

        // 背景位置（使用缩放后的尺寸，在原始坐标系中计算）
        int bgX = displayX - scaledPadding;
        int bgY = displayY - scaledPadding / 2;
        int bgWidth = scaledTextWidth + scaledPadding * 2;
        int bgHeight = scaledLineHeight + scaledPadding;

        // 绘制背景（原始坐标系）
        guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, colorBackground);

        // 绘制文字（先平移到目标位置，再缩放）
        guiGraphics.pose().pushPose();
        // 先平移到文字左上角位置，再缩放
        guiGraphics.pose().translate(displayX, displayY, 0);
        guiGraphics.pose().scale((float) scale, (float) scale, 1.0f);
        // 在缩放后的坐标系中从 (0,0) 开始绘制
        guiGraphics.drawString(font, text, 0, 0, colorText);
        guiGraphics.pose().popPose();
    }
}