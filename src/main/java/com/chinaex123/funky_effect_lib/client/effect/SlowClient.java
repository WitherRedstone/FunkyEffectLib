package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import com.chinaex123.funky_effect_lib.client.hud.HUDLayoutManager;
import com.chinaex123.funky_effect_lib.client.hud.HUDElementFactory;
import net.minecraft.client.Minecraft;
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
 * 减速客户端处理类
 * <p>
 * 功能：管理减速层数HUD显示
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class SlowClient {

    /** 缓存玩家UUID与减速层数的映射 **/
    private static final Map<UUID, Integer> STACKS_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的减速层数
     *
     * @param entityUuid 玩家UUID
     * @param stacks 减速层数
     */
    public static void setStacks(UUID entityUuid, int stacks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(entityUuid)) {
            STACKS_CACHE.put(entityUuid, stacks);
        }
    }

    /**
     * 清除指定玩家的减速层数
     *
     * @param entityUuid 玩家UUID
     */
    public static void clearStacks(UUID entityUuid) {
        STACKS_CACHE.remove(entityUuid);
    }

    /**
     * 渲染GUI事件处理
     * 创建并注册HUD元素
     *
     * @param event 渲染GUI事件
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Integer stacks = STACKS_CACHE.get(playerUuid);

        if (stacks != null && stacks > 0) {
            long currentTime = minecraft.player.level().getGameTime();
            String text = Component.translatable("gui.funky_effect_lib.slow_stacks", stacks, 100).getString();
            HUDLayoutManager.HUDElement element = HUDElementFactory.createHUDElement(
                    text,
                    ClientConfig.parseColor(ClientConfig.SLOW_COLOR_TEXT.get()),
                    ClientConfig.parseColor(ClientConfig.SLOW_COLOR_BACKGROUND.get()),
                    ClientConfig.HUD_PADDING.get(),
                    ClientConfig.GLOBAL_SCALE.get(),
                    () -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null) return false;
                        Integer s = STACKS_CACHE.get(mc.player.getUUID());
                        return s != null && s > 0;
                    }
            );
            HUDLayoutManager.registerElement("slow", element, playerUuid, currentTime);
        } else {
            HUDLayoutManager.unregisterElement("slow");
        }
    }
}