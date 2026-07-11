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

/** 减速客户端处理类：在游戏画面上显示当前玩家的减速层数 **/
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class SlowClient {

    /** 缓存玩家UUID与减速层数的映射 **/
    private static final Map<UUID, Integer> STACKS_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的减速层数缓存
     * @param entityUuid 玩家UUID
     * @param stacks 减速层数
     **/
    public static void setStacks(UUID entityUuid, int stacks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(entityUuid)) {
            STACKS_CACHE.put(entityUuid, stacks);
        }
    }

    /**
     * 清除指定玩家的减速层数缓存
     * @param entityUuid 玩家UUID
     **/
    public static void clearStacks(UUID entityUuid) {
        STACKS_CACHE.remove(entityUuid);
    }

    /** 在游戏界面渲染完成后绘制减速层数显示 **/
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }

        Integer stacks = STACKS_CACHE.get(minecraft.player.getUUID());
        if (stacks == null || stacks <= 0) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = minecraft.font;

        String text = Component.translatable("gui.funky_effect_lib.slow_stacks", stacks, 100).getString();
        Component component = Component.literal(text);
        int textWidth = font.width(component);
        int textHeight = font.lineHeight;

        int x = ClientConfig.SLOW_DISPLAY_X.get();
        int y = ClientConfig.SLOW_DISPLAY_Y.get();
        int padding = ClientConfig.SLOW_PADDING.get();

        int bgColor = ClientConfig.parseColor(ClientConfig.SLOW_COLOR_BACKGROUND.get());
        int textColor = ClientConfig.parseColor(ClientConfig.SLOW_COLOR_TEXT.get());

        guiGraphics.fill(x, y, x + textWidth + padding * 2, y + textHeight + padding * 2, bgColor);
        guiGraphics.drawString(font, component, x + padding, y + padding, textColor);
    }
}