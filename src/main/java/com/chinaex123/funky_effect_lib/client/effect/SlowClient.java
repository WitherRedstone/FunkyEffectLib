package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 减速客户端处理类
 * <p>
 * 功能：在客户端显示当前玩家的减速层数HUD
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class SlowClient {

    /** 缓存玩家UUID与减速层数的映射 **/
    private static final Map<UUID, Integer> STACKS_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的减速层数缓存
     *
     * @param entityUuid 玩家UUID
     * @param stacks 减速层数
     */
    public static void setStacks(UUID entityUuid, int stacks) {
        Minecraft minecraft = Minecraft.getInstance();
        // 只缓存本地玩家的数据
        if (minecraft.player != null && minecraft.player.getUUID().equals(entityUuid)) {
            STACKS_CACHE.put(entityUuid, stacks);
        }
    }

    /**
     * 清除指定玩家的减速层数缓存
     *
     * @param entityUuid 玩家UUID
     */
    public static void clearStacks(UUID entityUuid) {
        STACKS_CACHE.remove(entityUuid);
    }

    /**
     * 在游戏界面渲染完成后绘制减速层数显示
     * 在游戏画面渲染结束后绘制HUD，如果玩家在屏幕界面中则不显示
     *
     * @param event 渲染GUI事件
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        // 如果玩家为null或正在查看屏幕界面，不显示
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        // 获取当前玩家的减速层数
        Integer stacks = STACKS_CACHE.get(minecraft.player.getUUID());
        if (stacks == null || stacks <= 0) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = minecraft.font;

        // 准备显示文本（显示当前层数/最大层数）
        String text = Component.translatable("gui.funky_effect_lib.slow_stacks", stacks, 100).getString();
        Component component = Component.literal(text);
        int textWidth = font.width(component);
        int textHeight = font.lineHeight;

        // 从配置读取显示参数
        int bgColor = ClientConfig.parseColor(ClientConfig.SLOW_COLOR_BACKGROUND.get());
        int textColor = ClientConfig.parseColor(ClientConfig.SLOW_COLOR_TEXT.get());
        int displayX = ClientConfig.SLOW_DISPLAY_X.get();
        int displayY = ClientConfig.SLOW_DISPLAY_Y.get();
        int padding = ClientConfig.SLOW_PADDING.get();
        double scale = ClientConfig.GLOBAL_SCALE.get();

        // 应用缩放后的尺寸
        int scaledTextWidth = (int) (textWidth * scale);
        int scaledTextHeight = (int) (textHeight * scale);
        int scaledPadding = (int) (padding * scale);

        // 计算背景位置和大小（在原始坐标系中）
        int bgX = displayX - scaledPadding;
        int bgY = displayY - scaledPadding / 2;
        int bgWidth = scaledTextWidth + scaledPadding * 2;
        int bgHeight = scaledTextHeight + scaledPadding;

        // 绘制半透明背景
        guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, bgColor);

        // 绘制文字（使用缩放变换）
        guiGraphics.pose().pushPose();
        // 平移至目标位置
        guiGraphics.pose().translate(displayX, displayY, 0);
        // 应用缩放
        guiGraphics.pose().scale((float) scale, (float) scale, 1.0f);
        // 在缩放后的坐标系中从 (0,0) 开始绘制文字
        guiGraphics.drawString(font, component, 0, 0, textColor);
        guiGraphics.pose().popPose();
    }
}