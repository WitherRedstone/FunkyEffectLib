package com.chinaex123.funky_effect_lib.client.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * HUD元素工厂类
 * <p>
 * 提供静态工厂方法用于创建各种HUD元素实例
 * 通过封装通用的创建逻辑，避免在多个地方重复编写匿名内部类
 */
@OnlyIn(Dist.CLIENT)
public class HUDElementFactory {

    /**
     * 创建基础的HUD元素实例
     * <p>
     * 该方法封装了HUDElement所有接口方法的默认实现，
     * 调用方只需提供必要的显示参数和显示逻辑即可快速创建HUD元素
     *
     * @param textSupplier 文本内容提供者，支持动态更新文本
     * @param colorText 文本颜色值（ARGB格式的int值），例如 0xFFFFFF 表示白色
     * @param colorBackground 背景颜色值（ARGB格式的int值），0x00000000 表示透明背景
     * @param padding 元素内边距（像素），控制文本与边框之间的间距
     * @param scale 缩放比例，控制元素在屏幕上的显示大小，1.0为原始大小
     * @param shouldDisplaySupplier 是否显示的判断函数式接口，通过Lambda表达式或方法引用传入动态判断逻辑
     * @return 配置好的HUDElement实例，可直接注册到HUDLayoutManager中使用
     */
    public static HUDLayoutManager.HUDElement createHUDElement(
            Supplier<String> textSupplier,
            int colorText,
            int colorBackground,
            int padding,
            double scale,
            BooleanSupplier shouldDisplaySupplier) {

        return new HUDLayoutManager.HUDElement() {

            @Override
            public String getText() {
                return textSupplier.get();
            }

            @Override
            public int getTextWidth(Font font) {
                return font.width(getText());
            }

            @Override
            public int getLineHeight(Font font) {
                return font.lineHeight;
            }

            @Override
            public double getScale() {
                return scale;
            }

            @Override
            public int getPadding() {
                return padding;
            }

            @Override
            public int getTextColor() {
                return colorText;
            }

            @Override
            public int getBackgroundColor() {
                return colorBackground;
            }

            @Override
            public boolean shouldDisplay() {
                return shouldDisplaySupplier.getAsBoolean();
            }

            @Override
            public void drawContent(GuiGraphics guiGraphics, Font font, int x, int y) {
                guiGraphics.drawString(font, getText(), 0, 0, colorText);
            }
        };
    }

    /**
     * 创建静态文本HUD元素（文本内容固定）
     *
     * @param text 静态文本内容
     * @param colorText 文本颜色
     * @param colorBackground 背景颜色
     * @param padding 内边距
     * @param scale 缩放比例
     * @param shouldDisplaySupplier 是否显示判断
     * @return HUD元素实例
     */
    public static HUDLayoutManager.HUDElement createHUDElement(
            String text,
            int colorText,
            int colorBackground,
            int padding,
            double scale,
            BooleanSupplier shouldDisplaySupplier) {
        return createHUDElement(() -> text, colorText, colorBackground, padding, scale, shouldDisplaySupplier);
    }
}