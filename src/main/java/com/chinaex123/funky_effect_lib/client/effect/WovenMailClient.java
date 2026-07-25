package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import com.chinaex123.funky_effect_lib.client.renderer.effects.woven_mail.WovenMailClientData;
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

import static com.chinaex123.funky_effect_lib.effect.WovenMail.MAX_TANGLES;

/** 织造铠甲客户端处理类 **/
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class WovenMailClient {

    private static final int DISPLAY_TICKS = 60; // 未充能时显示的时间

    private static final Map<UUID, Integer> TANGLE_CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> REMAINING_SECONDS_CACHE = new ConcurrentHashMap<>();
    private static int displayTicks = 0;
    private static boolean isDisplaying = false;

    public static void setTangleCount(UUID playerUuid, int count, int remainingSeconds) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            TANGLE_CACHE.put(playerUuid, Math.min(count, MAX_TANGLES));
            REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);
            isDisplaying = true;
            displayTicks = DISPLAY_TICKS;
        }
    }

    public static void clearTangles(UUID playerUuid) {
        TANGLE_CACHE.remove(playerUuid);
        REMAINING_SECONDS_CACHE.remove(playerUuid);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
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
                    long remainingTicks = earliestExpiry - currentTime;
                    int remainingSeconds = (int) Math.max(0, Math.ceil(remainingTicks / 20.0));
                    REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);

                    // 如果时间到了，清除缠结
                    if (remainingSeconds <= 0) {
                        clearTangles(playerUuid);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Integer tangleCount = TANGLE_CACHE.get(playerUuid);

        if (tangleCount == null) return;

        if (isDisplaying || tangleCount > 0) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Font font = minecraft.font;

            int colorText = ClientConfig.parseColor(ClientConfig.WOVEN_MAIL_COLOR_TEXT.get());
            int colorBackground = ClientConfig.parseColor(ClientConfig.WOVEN_MAIL_COLOR_BACKGROUND.get());
            int displayX = ClientConfig.WOVEN_MAIL_DISPLAY_X.get();
            int displayY = ClientConfig.WOVEN_MAIL_DISPLAY_Y.get();
            int padding = ClientConfig.WOVEN_MAIL_PADDING.get();
            double scale = ClientConfig.GLOBAL_SCALE.get();

            // 文字部分（在原始尺寸下计算）
            Integer remainingSeconds = REMAINING_SECONDS_CACHE.get(playerUuid);
            String text = Component.translatable("gui.funky_effect_lib.woven_mail", tangleCount, remainingSeconds != null ? remainingSeconds : 0).getString();
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
            guiGraphics.pose().translate(displayX, displayY, 0);
            guiGraphics.pose().scale((float) scale, (float) scale, 1.0f);
            // 在缩放后的坐标系中从 (0,0) 开始绘制
            guiGraphics.drawString(font, text, 0, 0, colorText);
            guiGraphics.pose().popPose();
        }
    }
}