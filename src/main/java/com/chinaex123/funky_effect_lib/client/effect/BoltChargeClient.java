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
 * 电光充能客户端处理类
 * <p>
 * 功能：管理电光充能层数HUD显示
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class BoltChargeClient {

    /** 最大充能层数 **/
    private static final int MAX_CHARGES = 10;

    /** 缓存每个玩家的充能层数 **/
    private static final Map<UUID, Integer> CHARGE_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的充能层数
     *
     * @param playerUuid 玩家UUID
     * @param count 充能层数
     */
    public static void setChargeCount(UUID playerUuid, int count) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            CHARGE_CACHE.put(playerUuid, Math.min(count, MAX_CHARGES));
        }
    }

    /**
     * 清除指定玩家的充能数据
     *
     * @param playerUuid 玩家UUID
     */
    public static void clearCharges(UUID playerUuid) {
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
        Integer chargeCount = CHARGE_CACHE.get(playerUuid);

        if (chargeCount != null && chargeCount > 0) {
            long currentTime = minecraft.player.level().getGameTime();
            String text = Component.translatable("gui.funky_effect_lib.bolt_charge", chargeCount).getString();
            HUDLayoutManager.HUDElement element = HUDElementFactory.createHUDElement(
                    text,
                    ClientConfig.parseColor(ClientConfig.BOLT_CHARGE_COLOR_TEXT.get()),
                    ClientConfig.parseColor(ClientConfig.BOLT_CHARGE_COLOR_BACKGROUND.get()),
                    ClientConfig.HUD_PADDING.get(),
                    ClientConfig.GLOBAL_SCALE.get(),
                    () -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null) return false;
                        Integer c = CHARGE_CACHE.get(mc.player.getUUID());
                        return c != null && c > 0;
                    }
            );
            HUDLayoutManager.registerElement("bolt_charge", element, playerUuid, currentTime);
        } else {
            HUDLayoutManager.unregisterElement("bolt_charge");
        }
    }
}