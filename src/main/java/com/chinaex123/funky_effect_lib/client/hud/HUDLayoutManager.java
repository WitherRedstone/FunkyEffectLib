package com.chinaex123.funky_effect_lib.client.hud;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HUD布局管理器
 * <p>
 * 功能：管理多个HUD元素的垂直堆叠布局，避免重叠
 * <p>
 * 特性：
 * <ul>
 *   <li>自动计算HUD元素位置，避免重叠</li>
 *   <li>支持配置起始位置、间距、最大高度等</li>
 *   <li>动态管理HUD元素的显示和隐藏</li>
 *   <li>自动排序（最新的在上面）</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class HUDLayoutManager {

    /** HUD元素接口 **/
    public interface HUDElement {
        String getText();
        int getTextWidth(Font font);
        int getLineHeight(Font font);
        double getScale();
        int getPadding();
        int getTextColor();
        int getBackgroundColor();
        boolean shouldDisplay();
        void drawContent(GuiGraphics guiGraphics, Font font, int x, int y);
    }

    /** HUD元素数据记录类 **/
    private record HUDElementData(HUDElement element, String id, UUID playerUuid, long timestamp) { }

    /** 元素缓存 **/
    private static final Map<String, HUDElementData> ELEMENT_CACHE = new ConcurrentHashMap<>();

    /**
     * 注册HUD元素
     *
     * @param id 元素ID
     * @param element HUD元素
     * @param playerUuid 玩家UUID
     * @param timestamp 时间戳
     */
    public static void registerElement(String id, HUDElement element, UUID playerUuid, long timestamp) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.getUUID().equals(playerUuid)) return;

        HUDElementData data = new HUDElementData(element, id, playerUuid, timestamp);
        ELEMENT_CACHE.put(id, data);
    }

    /**
     * 注册HUD元素（简化版，不检查玩家UUID）
     *
     * @param id 元素ID
     * @param element HUD元素
     */
    public static void registerElement(String id, HUDElement element) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        registerElement(id, element, minecraft.player.getUUID(), minecraft.player.level().getGameTime());
    }

    /**
     * 注销HUD元素
     *
     * @param id 元素ID
     */
    public static void unregisterElement(String id) {
        ELEMENT_CACHE.remove(id);
    }

    /**
     * 清除所有HUD元素
     */
    public static void clearAllElements() {
        ELEMENT_CACHE.clear();
    }

    /**
     * 在游戏界面上渲染所有HUD元素
     *
     * @param event 渲染GUI事件
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        // 清理过期的元素
        cleanupExpiredElements(minecraft);

        // 渲染左上角HUD元素（动态显示，自动排序）
        renderDynamicElements(event, ELEMENT_CACHE,
                ClientConfig.HUD_LAYOUT_RELATIVE_X.get(),
                ClientConfig.HUD_LAYOUT_RELATIVE_Y.get());
    }

    /**
     * 清理过期的元素
     */
    private static void cleanupExpiredElements(Minecraft minecraft) {
        long currentTime = 0;
        if (minecraft.player != null) {
            currentTime = minecraft.player.level().getGameTime();
        }
        long expiryTime = currentTime - 100; // 5秒后过期

        ELEMENT_CACHE.entrySet().removeIf(entry -> entry.getValue().timestamp() < expiryTime);
    }

    /**
     * 渲染动态HUD元素（左上角，自动排序，限制最大显示数量）
     */
    private static void renderDynamicElements(RenderGuiEvent.Post event,
                                               Map<String, HUDElementData> elementCache,
                                               double relativeX, double relativeY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        // 获取应该显示的元素
        List<HUDElementData> displayElements = new ArrayList<>();
        for (HUDElementData data : elementCache.values()) {
            if (data.element.shouldDisplay()) {
                displayElements.add(data);
            }
        }

        if (displayElements.isEmpty()) return;

        // 按时间戳排序（最新的在上面）
        displayElements.sort((a, b) -> Long.compare(b.timestamp(), a.timestamp()));

        // 限制最大显示数量
        int maxDisplayCount = ClientConfig.HUD_DYNAMIC_MAX_DISPLAY_COUNT.get();
        if (displayElements.size() > maxDisplayCount) {
            displayElements = displayElements.subList(0, maxDisplayCount);
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = minecraft.font;

        // 获取配置参数
        int spacing = ClientConfig.HUD_LAYOUT_SPACING.get();

        // 获取屏幕尺寸
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // 计算起始位置
        int startX = (int) (screenWidth * relativeX);
        int startY = (int) (screenHeight * relativeY);

        // 渲染每个元素
        for (int i = 0; i < displayElements.size(); i++) {
            HUDElementData data = displayElements.get(i);
            HUDElement element = data.element;

            double scale = element.getScale();
            int padding = element.getPadding();
            int textWidth = element.getTextWidth(font);
            int lineHeight = element.getLineHeight(font);

            int scaledTextWidth = (int) (textWidth * scale);
            int scaledLineHeight = (int) (lineHeight * scale);
            int scaledPadding = (int) (padding * scale);

            // 计算背景位置和大小
            int bgX = startX - scaledPadding;
            int bgY = startY + i * (scaledLineHeight + spacing) - scaledPadding / 2;
            int bgWidth = scaledTextWidth + scaledPadding * 2;
            int bgHeight = scaledLineHeight + scaledPadding;

            // 绘制背景
            guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, element.getBackgroundColor());

            // 绘制内容
            int textX = startX;
            int textY = startY + i * (scaledLineHeight + spacing);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(textX, textY, 0);
            guiGraphics.pose().scale((float) scale, (float) scale, 1.0f);
            element.drawContent(guiGraphics, font, 0, 0);
            guiGraphics.pose().popPose();
        }
    }
}