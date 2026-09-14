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

import static com.chinaex123.funky_effect_lib.api.event.Scorch.ScorchAPI.MAX_SCORCH_STACKS;

/**
 * 灼烧客户端处理类
 * <p>
 * 功能：管理灼烧层数HUD显示
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class ScorchClient {

    /** 缓存每个玩家的灼烧层数 **/
    private static final Map<UUID, Integer> SCORCH_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定玩家的灼烧层数
     *
     * @param playerUuid 玩家UUID
     * @param stacks 灼烧层数
     */
    public static void setScorchStacks(UUID playerUuid, int stacks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            SCORCH_CACHE.put(playerUuid, Math.min(stacks, MAX_SCORCH_STACKS));
        }
    }

    /**
     * 清除指定玩家的灼烧数据
     *
     * @param playerUuid 玩家UUID
     */
    public static void clearScorchStacks(UUID playerUuid) {
        SCORCH_CACHE.remove(playerUuid);
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
        Integer scorchStacks = SCORCH_CACHE.get(playerUuid);

        if (scorchStacks != null && scorchStacks > 0) {
            long currentTime = minecraft.player.level().getGameTime();
            String text = Component.translatable("gui.funky_effect_lib.scorch", scorchStacks).getString();
            HUDLayoutManager.HUDElement element = HUDElementFactory.createHUDElement(
                    text,
                    ClientConfig.parseColor(ClientConfig.SCORCH_COLOR_TEXT.get()),
                    ClientConfig.parseColor(ClientConfig.SCORCH_COLOR_BACKGROUND.get()),
                    ClientConfig.HUD_PADDING.get(),
                    ClientConfig.GLOBAL_SCALE.get(),
                    () -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null) return false;
                        Integer s = SCORCH_CACHE.get(mc.player.getUUID());
                        return s != null && s > 0;
                    }
            );
            HUDLayoutManager.registerElement("scorch", element, playerUuid, currentTime);
        } else {
            HUDLayoutManager.unregisterElement("scorch");
        }
    }
}