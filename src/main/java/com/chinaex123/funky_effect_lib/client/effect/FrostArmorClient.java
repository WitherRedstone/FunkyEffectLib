package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import com.chinaex123.funky_effect_lib.client.renderer.effects.frost_armor.FrostArmorClientData;
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

import static com.chinaex123.funky_effect_lib.effect.FrostArmor.MAX_CRYSTALS;

/**
 * 冰霜护甲客户端处理类
 * <p>
 * 功能：在客户端显示冰霜护甲的冰晶数量和剩余时间HUD
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class FrostArmorClient {

    /** 未充能时显示的时间 **/
    private static final int DISPLAY_TICKS = 60;

    /** 缓存每个玩家的冰晶数量 **/
    private static final Map<UUID, Integer> CRYSTAL_CACHE = new ConcurrentHashMap<>();
    /** 缓存每个玩家的剩余时间 **/
    private static final Map<UUID, Integer> REMAINING_SECONDS_CACHE = new ConcurrentHashMap<>();

    /** 当前显示剩余时间 **/
    private static int displayTicks = 0;
    /** 是否正在显示 **/
    private static boolean isDisplaying = false;

    /**
     * 设置指定玩家的冰晶数量和剩余时间
     *
     * @param playerUuid 玩家UUID
     * @param count 冰晶数量
     * @param remainingSeconds 剩余时间（秒）
     */
    public static void setCrystalCount(UUID playerUuid, int count, int remainingSeconds) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            CRYSTAL_CACHE.put(playerUuid, Math.min(count, MAX_CRYSTALS));
            REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);
            // 触发显示
            isDisplaying = true;
            displayTicks = DISPLAY_TICKS;
        }
    }

    /**
     * 清除指定玩家的冰霜护甲数据
     *
     * @param playerUuid 玩家UUID
     */
    public static void clearCrystals(UUID playerUuid) {
        CRYSTAL_CACHE.remove(playerUuid);
        REMAINING_SECONDS_CACHE.remove(playerUuid);
    }

    /**
     * 客户端Tick事件处理
     * 更新显示计时器并实时计算剩余时间倒计时
     *
     * @param event 客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
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
            Integer crystalCount = CRYSTAL_CACHE.get(playerUuid);

            if (crystalCount != null && crystalCount > 0) {
                long currentTime = minecraft.level.getGameTime();
                long earliestExpiry = FrostArmorClientData.getEarliestExpiry(playerUuid);

                if (earliestExpiry > 0) {
                    // 计算剩余刻数并转换为秒（向上取整）
                    long remainingTicks = earliestExpiry - currentTime;
                    int remainingSeconds = (int) Math.max(0, Math.ceil(remainingTicks / 20.0));
                    REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);

                    // 如果时间到，清除冰晶
                    if (remainingSeconds <= 0) {
                        clearCrystals(playerUuid);
                    }
                }
            }
        }
    }

    /**
     * 在游戏界面上渲染冰霜护甲显示
     * 在游戏画面渲染结束后绘制HUD
     *
     * @param event 渲染GUI事件
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Integer crystalCount = CRYSTAL_CACHE.get(playerUuid);

        if (crystalCount == null) return;

        // 只有在显示状态或冰晶数量大于0时才渲染
        if (isDisplaying || crystalCount > 0) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Font font = minecraft.font;

            // 从配置读取显示参数
            int colorText = ClientConfig.parseColor(ClientConfig.FROST_ARMOR_COLOR_TEXT.get());
            int colorBackground = ClientConfig.parseColor(ClientConfig.FROST_ARMOR_COLOR_BACKGROUND.get());
            int displayX = ClientConfig.FROST_ARMOR_DISPLAY_X.get();
            int displayY = ClientConfig.FROST_ARMOR_DISPLAY_Y.get();
            int padding = ClientConfig.FROST_ARMOR_PADDING.get();
            double scale = ClientConfig.GLOBAL_SCALE.get();

            // 准备显示文本
            Integer remainingSeconds = REMAINING_SECONDS_CACHE.get(playerUuid);
            String text = Component.translatable("gui.funky_effect_lib.frost_armor",
                    crystalCount, remainingSeconds != null ? remainingSeconds : 0).getString();
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
            // 在缩放后的坐标系中从 (0,0) 开始绘制
            guiGraphics.drawString(font, text, 0, 0, colorText);
            guiGraphics.pose().popPose();
        }
    }
}