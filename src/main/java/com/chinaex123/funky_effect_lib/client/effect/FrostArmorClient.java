package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import com.chinaex123.funky_effect_lib.client.hud.HUDElementFactory;
import com.chinaex123.funky_effect_lib.client.hud.HUDLayoutManager;
import com.chinaex123.funky_effect_lib.client.renderer.effects.frost_armor.FrostArmorClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 冰霜护甲客户端处理类
 * <p>
 * 功能：管理冰霜护甲的冰晶数量和剩余时间HUD显示
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class FrostArmorClient {

    /** 缓存每个玩家的冰晶数量 **/
    private static final Map<UUID, Integer> CRYSTAL_CACHE = new ConcurrentHashMap<>();
    /** 缓存每个玩家的剩余时间 **/
    private static final Map<UUID, Integer> REMAINING_SECONDS_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的冰晶数量和剩余时间
     *
     * @param playerUuid 玩家UUID
     * @param count 冰晶数量
     * @param remainingSeconds 剩余时间（秒）
     */
    public static void setCrystalCount(UUID playerUuid, int count, int remainingSeconds) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            CRYSTAL_CACHE.put(playerUuid, Math.min(count, 10));
            REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);
        }
    }

    /**
     * 清除指定玩家的冰霜护甲数据
     *
     * @param playerUuid 玩家UUID
     */
    public static void clearCrystals(UUID playerUuid) {
        CRYSTAL_CACHE.remove(playerUuid);
        REMAINING_SECONDS_CACHE.remove(playerUuid);
    }

    /**
     * 客户端Tick事件处理
     * 更新剩余时间
     *
     * @param event 客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        Integer crystalCount = CRYSTAL_CACHE.get(playerUuid);

        if (crystalCount != null && crystalCount > 0) {
            long currentTime = minecraft.level.getGameTime();
            long earliestExpiry = FrostArmorClientData.getEarliestExpiry(playerUuid);

            if (earliestExpiry > 0) {
                long remainingTicks = earliestExpiry - currentTime;
                int remainingSeconds = (int) Math.max(0, Math.ceil(remainingTicks / 20.0));
                REMAINING_SECONDS_CACHE.put(playerUuid, remainingSeconds);

                if (remainingSeconds <= 0) {
                    clearCrystals(playerUuid);
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
        Integer crystalCount = CRYSTAL_CACHE.get(playerUuid);
        Integer remainingSeconds = REMAINING_SECONDS_CACHE.get(playerUuid);

        if (crystalCount != null && crystalCount > 0 && remainingSeconds != null && remainingSeconds > 0) {
            long currentTime = minecraft.player.level().getGameTime();
            String text = Component.translatable("gui.funky_effect_lib.frost_armor", crystalCount, remainingSeconds).getString();
            HUDLayoutManager.HUDElement element = HUDElementFactory.createHUDElement(
                    text,
                    ClientConfig.parseColor(ClientConfig.FROST_ARMOR_COLOR_TEXT.get()),
                    ClientConfig.parseColor(ClientConfig.FROST_ARMOR_COLOR_BACKGROUND.get()),
                    ClientConfig.HUD_PADDING.get(),
                    ClientConfig.GLOBAL_SCALE.get(),
                    () -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null) return false;
                        Integer c = CRYSTAL_CACHE.get(mc.player.getUUID());
                        Integer s = REMAINING_SECONDS_CACHE.get(mc.player.getUUID());
                        return c != null && c > 0 && s != null && s > 0;
                    }
            );
            HUDLayoutManager.registerElement("frost_armor", element, playerUuid, currentTime);
        } else {
            HUDLayoutManager.unregisterElement("frost_armor");
        }
    }
}