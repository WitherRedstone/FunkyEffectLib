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
 * 蔓延黑暗客户端处理类
 * <p>
 * 功能：管理蔓延黑暗层数HUD显示
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class CreepingDarknessClient {

    /** 缓存每个玩家的蔓延黑暗层数 **/
    private static final Map<UUID, Integer> STACK_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的蔓延黑暗层数
     *
     * @param playerUuid 玩家UUID
     * @param stack 层数
     */
    public static void setStack(UUID playerUuid, int stack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            if (stack <= 0) {
                STACK_CACHE.remove(playerUuid);
            } else {
                STACK_CACHE.put(playerUuid, stack);
            }
        }
    }

    /**
     * 清除指定玩家的蔓延黑暗层数
     *
     * @param playerUuid 玩家UUID
     */
    public static void clearStack(UUID playerUuid) {
        STACK_CACHE.remove(playerUuid);
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
        Integer stack = STACK_CACHE.get(playerUuid);

        if (stack != null && stack > 0) {
            long currentTime = minecraft.player.level().getGameTime();
            String text = Component.translatable("gui.funky_effect_lib.creeping_darkness", stack).getString();
            HUDLayoutManager.HUDElement element = HUDElementFactory.createHUDElement(
                    text,
                    ClientConfig.parseColor(ClientConfig.CREEPING_DARKNESS_COLOR_TEXT.get()),
                    ClientConfig.parseColor(ClientConfig.CREEPING_DARKNESS_COLOR_BACKGROUND.get()),
                    ClientConfig.HUD_PADDING.get(),
                    ClientConfig.GLOBAL_SCALE.get(),
                    () -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null) return false;
                        Integer s = STACK_CACHE.get(mc.player.getUUID());
                        return s != null && s > 0;
                    }
            );
            HUDLayoutManager.registerElement("creeping_darkness", element, playerUuid, currentTime);
        } else {
            HUDLayoutManager.unregisterElement("creeping_darkness");
        }
    }
}