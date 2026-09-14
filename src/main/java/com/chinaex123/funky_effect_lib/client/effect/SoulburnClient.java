package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import com.chinaex123.funky_effect_lib.client.hud.HUDElementFactory;
import com.chinaex123.funky_effect_lib.client.hud.HUDLayoutManager;
import net.minecraft.client.Minecraft;
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
 * 魂燃客户端处理类
 * <p>
 * 功能：管理魂燃的充能状态HUD显示
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class SoulburnClient {

    /** 缓存每个玩家的魂燃充能状态 **/
    private static final Map<UUID, Boolean> CHARGE_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的魂燃充能状态
     *
     * @param playerUuid 玩家UUID
     * @param hasCharge 是否有充能
     */
    public static void setChargeState(UUID playerUuid, boolean hasCharge) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            CHARGE_CACHE.put(playerUuid, hasCharge);
        }
    }

    /**
     * 清除魂燃充能状态
     *
     * @param playerUuid 玩家UUID
     */
    public static void clearChargeState(UUID playerUuid) {
        CHARGE_CACHE.remove(playerUuid);
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
        Boolean hasCharge = CHARGE_CACHE.get(playerUuid);

        if (hasCharge != null && hasCharge) {
            long currentTime = minecraft.player.level().getGameTime();
            String text = Component.translatable("gui.funky_effect_lib.soulburn", "1").getString();
            HUDLayoutManager.HUDElement element = HUDElementFactory.createHUDElement(
                    text,
                    ClientConfig.parseColor(ClientConfig.SOULBURN_COLOR_TEXT.get()),
                    ClientConfig.parseColor(ClientConfig.SOULBURN_COLOR_BACKGROUND.get()),
                    ClientConfig.HUD_PADDING.get(),
                    ClientConfig.GLOBAL_SCALE.get(),
                    () -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null) return false;
                        Boolean charge = CHARGE_CACHE.get(mc.player.getUUID());
                        return charge != null && charge;
                    }
            );
            HUDLayoutManager.registerElement("soulburn", element, playerUuid, currentTime);
        } else {
            HUDLayoutManager.unregisterElement("soulburn");
        }
    }
}