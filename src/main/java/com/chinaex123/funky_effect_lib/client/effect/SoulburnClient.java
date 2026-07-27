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

/**
 * 魂燃（Soulburn）客户端处理类
 * <p>
 * 功能：在客户端显示魂燃的充能状态HUD
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class SoulburnClient {

    /** 未充能符号 **/
    private static final String SYMBOL_EMPTY = "□";
    /** 充能符号 **/
    private static final String SYMBOL_CHARGED = "■";

    /** 未充能符号颜色 **/
    private static final int COLOR_SYMBOL_EMPTY = 0xAAAAAA;
    /** 充能符号颜色 **/
    private static final int COLOR_SYMBOL_CHARGED = 0x55FF55;

    /** 未充能时显示的时间 **/
    private static final int DISPLAY_TICKS = 60;

    /** 当前显示剩余时间 **/
    private static int displayTicks = 0;
    /** 是否正在显示 **/
    private static boolean isDisplaying = false;

    /** 缓存每个玩家的魂燃充能状态 **/
    private static final Map<UUID, Boolean> CHARGE_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的魂燃充能状态
     *
     * @param playerUuid 玩家UUID
     * @param hasCharge 是否有充能
     */
    public static void setChargeState(UUID playerUuid, boolean hasCharge) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            CHARGE_CACHE.put(playerUuid, hasCharge);
            // 触发显示
            isDisplaying = true;
            displayTicks = DISPLAY_TICKS;
        }
    }

    /**
     * 客户端Tick事件处理
     * 更新显示计时器
     *
     * @param event 客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (displayTicks > 0) {
            displayTicks--;
            if (displayTicks <= 0) {
                isDisplaying = false;
            }
        }
    }

    /**
     * 在游戏界面上渲染魂燃充能状态显示
     * 在游戏画面渲染结束后绘制HUD
     *
     * @param event 渲染GUI事件
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Boolean hasCharge = CHARGE_CACHE.get(playerUuid);

        if (hasCharge == null) return;

        // 只有在显示状态或有充能时才渲染
        if (isDisplaying || hasCharge) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Font font = minecraft.font;

            // 从配置读取显示参数
            int colorText = ClientConfig.parseColor(ClientConfig.SOULBURN_COLOR_TEXT.get());
            int colorBackground = ClientConfig.parseColor(ClientConfig.SOULBURN_COLOR_BACKGROUND.get());
            int displayX = ClientConfig.SOULBURN_DISPLAY_X.get();
            int displayY = ClientConfig.SOULBURN_DISPLAY_Y.get();
            int padding = ClientConfig.SOULBURN_PADDING.get();
            double scale = ClientConfig.GLOBAL_SCALE.get();

            // 准备显示文本
            String text = Component.translatable("gui.funky_effect_lib.soulburn").getString() + " ";
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

            // 计算背景位置和大小（在原始坐标系中）
            int bgX = displayX - scaledPadding;
            int bgY = displayY - scaledPadding / 2;
            int bgWidth = scaledTotalWidth + scaledPadding * 2;
            int bgHeight = scaledHeight + scaledPadding;

            // 绘制半透明背景
            guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, colorBackground);

            // 绘制文字（使用缩放变换）
            guiGraphics.pose().pushPose();
            // 平移至目标位置
            guiGraphics.pose().translate(displayX, displayY, 0);
            // 应用缩放
            guiGraphics.pose().scale((float) scale, (float) scale, 1.0f);
            // 在缩放后的坐标系中从 (0,0) 开始绘制
            guiGraphics.drawString(font, text, 0, 0, colorText);
            guiGraphics.drawString(font, symbol, textWidth, 0, symbolColor);
            guiGraphics.pose().popPose();
        }
    }
}