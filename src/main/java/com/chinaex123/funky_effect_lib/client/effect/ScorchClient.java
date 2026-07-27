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

/**
 * 灼烧客户端处理类
 * <p>
 * 功能：在客户端显示灼烧层数的HUD
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class ScorchClient {

    /** 最大灼烧层数 **/
    private static final int MAX_SCORCH_STACKS = 100;

    /** 缓存每个玩家的灼烧层数 **/
    private static final Map<UUID, Integer> SCORCH_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的灼烧层数
     * 仅在客户端玩家自身匹配时更新缓存
     *
     * @param playerUuid 玩家UUID
     * @param stacks 灼烧层数
     */
    public static void setScorchStacks(UUID playerUuid, int stacks) {
        Minecraft minecraft = Minecraft.getInstance();
        // 只缓存本地玩家的数据
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            SCORCH_CACHE.put(playerUuid, Math.min(stacks, MAX_SCORCH_STACKS));
        }
    }

    /**
     * 清除指定玩家的灼烧数据
     *
     * @param playerUuid 玩家UUID
     */
    public static void clearScorchStacks(UUID playerUuid) {
        SCORCH_CACHE.remove(playerUuid);
    }

    /**
     * 在游戏界面上渲染灼烧层数显示
     * 在游戏画面渲染结束后绘制HUD
     *
     * @param event 渲染GUI事件
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Integer scorchStacks = SCORCH_CACHE.get(playerUuid);

        // 如果没有灼烧或层数为0，不显示
        if (scorchStacks == null || scorchStacks <= 0) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = minecraft.font;

        // 从配置读取显示参数
        int colorText = ClientConfig.parseColor(ClientConfig.SCORCH_COLOR_TEXT.get());
        int colorBackground = ClientConfig.parseColor(ClientConfig.SCORCH_COLOR_BACKGROUND.get());
        int displayX = ClientConfig.SCORCH_DISPLAY_X.get();
        int displayY = ClientConfig.SCORCH_DISPLAY_Y.get();
        int padding = ClientConfig.SCORCH_PADDING.get();
        double scale = ClientConfig.GLOBAL_SCALE.get();

        // 准备显示文本
        String text = Component.translatable("gui.funky_effect_lib.scorch", scorchStacks).getString();
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
        // 平移至目标位置
        guiGraphics.pose().translate(displayX, displayY, 0);
        // 应用缩放
        guiGraphics.pose().scale((float) scale, (float) scale, 1.0f);
        // 在缩放后的坐标系中从 (0,0) 开始绘制文字
        guiGraphics.drawString(font, text, 0, 0, colorText);
        guiGraphics.pose().popPose();
    }
}