package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import com.chinaex123.funky_effect_lib.client.hud.HUDLayoutManager;
import com.chinaex123.funky_effect_lib.client.hud.HUDElementFactory;
import com.chinaex123.funky_effect_lib.client.renderer.effects.woven_mail.WovenMailClientData;
import net.minecraft.client.Minecraft;
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

/**
 * 织造铠甲客户端处理类
 * <p>
 * 功能：管理织造铠甲的缠结数量和剩余时间HUD显示
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class WovenMailClient {

    /** 缓存每个玩家的缠结数量 **/
    private static final Map<UUID, Integer> TANGLE_CACHE = new ConcurrentHashMap<>();
    /** 缓存每个玩家的剩余时间 **/
    private static final Map<UUID, Integer> REMAINING_SECONDS_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的缠结数量和剩余时间
     *
     * @param playerUuid 玩家UUID
     * @param count 缠结数量
     * @param remainingSeconds 剩余时间（秒）
     */
    public static void setTangleCount(UUID playerUuid, int count, int remainingSeconds) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            TANGLE_CACHE.put(playerUuid, Math.min(count, 10));
            REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);
        }
    }

    /**
     * 清除指定玩家的织造铠甲数据
     *
     * @param playerUuid 玩家UUID
     */
    public static void clearTangles(UUID playerUuid) {
        TANGLE_CACHE.remove(playerUuid);
        REMAINING_SECONDS_CACHE.remove(playerUuid);
    }

    /**
     * 客户端Tick事件处理
     * 更新剩余时间
     *
     * @param event 客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Integer tangleCount = TANGLE_CACHE.get(playerUuid);

        if (tangleCount != null && tangleCount > 0) {
            long currentTime = minecraft.level.getGameTime();
            long earliestExpiry = WovenMailClientData.getEarliestExpiry(playerUuid);

            if (earliestExpiry > 0) {
                long remainingTicks = earliestExpiry - currentTime;
                int remainingSeconds = (int) Math.max(0, Math.ceil(remainingTicks / 20.0));
                REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);

                if (remainingSeconds <= 0) {
                    clearTangles(playerUuid);
                }
            }
        }
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
        Integer tangleCount = TANGLE_CACHE.get(playerUuid);
        Integer remainingSeconds = REMAINING_SECONDS_CACHE.get(playerUuid);

        if (tangleCount != null && tangleCount > 0 && remainingSeconds != null && remainingSeconds > 0) {
            long currentTime = minecraft.player.level().getGameTime();
            String text = Component.translatable("gui.funky_effect_lib.woven_mail", tangleCount, remainingSeconds).getString();
            HUDLayoutManager.HUDElement element = HUDElementFactory.createHUDElement(
                    text,
                    ClientConfig.parseColor(ClientConfig.WOVEN_MAIL_COLOR_TEXT.get()),
                    ClientConfig.parseColor(ClientConfig.WOVEN_MAIL_COLOR_BACKGROUND.get()),
                    ClientConfig.HUD_PADDING.get(),
                    ClientConfig.GLOBAL_SCALE.get(),
                    () -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null) return false;
                        Integer c = TANGLE_CACHE.get(mc.player.getUUID());
                        Integer s = REMAINING_SECONDS_CACHE.get(mc.player.getUUID());
                        return c != null && c > 0 && s != null && s > 0;
                    }
            );
            HUDLayoutManager.registerElement("woven_mail", element, playerUuid, currentTime);
        } else {
            HUDLayoutManager.unregisterElement("woven_mail");
        }
    }
}