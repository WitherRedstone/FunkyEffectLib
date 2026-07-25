package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import com.chinaex123.funky_effect_lib.client.renderer.effects.frost_armor.FrostArmorClientData;
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

import static com.chinaex123.funky_effect_lib.effect.FrostArmor.MAX_CRYSTALS;

/** 冰霜护甲客户端处理类 **/
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class FrostArmorClient {

    private static final int DISPLAY_TICKS = 60; // 未充能时显示的时间

    private static final Map<UUID, Integer> CRYSTAL_CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> REMAINING_SECONDS_CACHE = new ConcurrentHashMap<>();
    private static int displayTicks = 0;
    private static boolean isDisplaying = false;

    public static void setCrystalCount(UUID playerUuid, int count, int remainingSeconds) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            CRYSTAL_CACHE.put(playerUuid, Math.min(count, MAX_CRYSTALS));
            REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);
            isDisplaying = true;
            displayTicks = DISPLAY_TICKS;
        }
    }

    public static void clearCrystals(UUID playerUuid) {
        CRYSTAL_CACHE.remove(playerUuid);
        REMAINING_SECONDS_CACHE.remove(playerUuid);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
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
                    long remainingTicks = earliestExpiry - currentTime;
                    int remainingSeconds = (int) Math.max(0, Math.ceil(remainingTicks / 20.0));
                    REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);
                    
                    // 如果时间到了，清除冰晶
                    if (remainingSeconds <= 0) {
                        clearCrystals(playerUuid);
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
        Integer crystalCount = CRYSTAL_CACHE.get(playerUuid);

        if (crystalCount == null) return;

        if (isDisplaying || crystalCount > 0) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Font font = minecraft.font;

            int colorText = ClientConfig.parseColor(ClientConfig.FROST_ARMOR_COLOR_TEXT.get());
            int colorBackground = ClientConfig.parseColor(ClientConfig.FROST_ARMOR_COLOR_BACKGROUND.get());
            int displayX = ClientConfig.FROST_ARMOR_DISPLAY_X.get();
            int displayY = ClientConfig.FROST_ARMOR_DISPLAY_Y.get();
            int padding = ClientConfig.FROST_ARMOR_PADDING.get();
            double scale = ClientConfig.GLOBAL_SCALE.get();

            // 文字部分（在原始尺寸下计算）
            Integer remainingSeconds = REMAINING_SECONDS_CACHE.get(playerUuid);
            String text = Component.translatable("gui.funky_effect_lib.frost_armor", crystalCount, remainingSeconds != null ? remainingSeconds : 0).getString();
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