package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import com.chinaex123.funky_effect_lib.client.renderer.effects.woven_mail.WovenMailClientData;
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
 * 织造铠甲客户端处理类
 * <p>
 * 功能：在客户端显示织造铠甲的缠结数量和剩余时间HUD
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class WovenMailClient {

    /** 未充能时显示的时间（60刻 = 3秒） **/
    private static final int DISPLAY_TICKS = 60;

    /** 缓存每个玩家的缠结数量 **/
    private static final Map<UUID, Integer> TANGLE_CACHE = new ConcurrentHashMap<>();
    /** 缓存每个玩家的剩余时间 **/
    private static final Map<UUID, Integer> REMAINING_SECONDS_CACHE = new ConcurrentHashMap<>();

    /** 当前显示剩余时间 **/
    private static int displayTicks = 0;
    /** 是否正在显示 **/
    private static boolean isDisplaying = false;

    /**
     * 设置指定玩家的缠结数量和剩余时间
     *
     * @param playerUuid 玩家UUID
     * @param count 缠结数量
     * @param remainingSeconds 剩余时间（秒）
     */
    public static void setTangleCount(UUID playerUuid, int count, int remainingSeconds) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            TANGLE_CACHE.put(playerUuid, Math.min(count, 10));
            REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);
            // 触发显示
            isDisplaying = true;
            displayTicks = DISPLAY_TICKS;
        }
    }

    /**
     * 清除指定玩家的织造铠甲数据
     *
     * @param playerUuid 玩家UUID
     */
    public static void clearTangles(UUID playerUuid) {
        TANGLE_CACHE.remove(playerUuid);
        REMAINING_SECONDS_CACHE.remove(playerUuid);
    }

    /**
     * 客户端Tick事件处理
     * 更新显示计时器并实时计算剩余时间倒计时
     *
     * @param event 客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // 更新显示计时器
        if (displayTicks > 0) {
            displayTicks--;
            if (displayTicks <= 0) {
                isDisplaying = false;
            }
        }

        // 实时更新剩余秒数倒计时
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.level != null) {
            UUID playerUuid = minecraft.player.getUUID();
            Integer tangleCount = TANGLE_CACHE.get(playerUuid);

            if (tangleCount != null && tangleCount > 0) {
                // 从WovenMailClientData获取最早过期时间
                long currentTime = minecraft.level.getGameTime();
                long earliestExpiry = WovenMailClientData.getEarliestExpiry(playerUuid);

                if (earliestExpiry > 0) {
                    // 计算剩余刻数并转换为秒（向上取整）
                    long remainingTicks = earliestExpiry - currentTime;
                    int remainingSeconds = (int) Math.max(0, Math.ceil(remainingTicks / 20.0));
                    REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);

                    // 如果时间到，清除缠结
                    if (remainingSeconds <= 0) {
                        clearTangles(playerUuid);
                    }
                }
            }
        }
    }

    /**
     * 在游戏界面上渲染织造铠甲显示
     * 在游戏画面渲染结束后绘制HUD
     *
     * @param event 渲染GUI事件
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Integer tangleCount = TANGLE_CACHE.get(playerUuid);

        if (tangleCount == null) return;

        // 只有在显示状态或缠结数量大于0时才渲染
        if (isDisplaying || tangleCount > 0) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Font font = minecraft.font;

            // 从配置读取显示参数
            int colorText = ClientConfig.parseColor(ClientConfig.WOVEN_MAIL_COLOR_TEXT.get());
            int colorBackground = ClientConfig.parseColor(ClientConfig.WOVEN_MAIL_COLOR_BACKGROUND.get());
            int displayX = ClientConfig.WOVEN_MAIL_DISPLAY_X.get();
            int displayY = ClientConfig.WOVEN_MAIL_DISPLAY_Y.get();
            int padding = ClientConfig.WOVEN_MAIL_PADDING.get();
            double scale = ClientConfig.GLOBAL_SCALE.get();

            // 准备显示文本
            Integer remainingSeconds = REMAINING_SECONDS_CACHE.get(playerUuid);
            String text = Component.translatable("gui.funky_effect_lib.woven_mail",
                    tangleCount, remainingSeconds != null ? remainingSeconds : 0).getString();
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
            guiGraphics.drawString(font, text, 0, 0, colorText);
            guiGraphics.pose().popPose();
        }
    }
}