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
 * 舒张效果客户端处理器
 * <p>
 * 负责在客户端接收并缓存舒张效果的同步数据，
 * 并在HUD上渲染叠层进度显示（□□□□ → ■■■■）。
 * <p>
 * 显示规则：
 * <ul>
 *   <li>叠层期间：显示对应数量的实心方块（■）</li>
 *   <li>冷却期间：所有方块显示为空心（□）</li>
 *   <li>无效果时：不显示HUD元素</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class DiastoleClient {

    /** 玩家舒张状态缓存（UUID → 状态） */
    private static final Map<UUID, DiastoleState> STATE_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置玩家的舒张状态
     * <p>
     * 接收服务端同步的数据并更新缓存，仅当玩家UUID匹配时才更新
     *
     * @param playerUuid 玩家UUID
     * @param stacks 当前叠层数
     * @param cooldownEnd 冷却结束时间（游戏刻）
     */
    public static void setDiastoleState(UUID playerUuid, int stacks, long cooldownEnd) {
        Minecraft minecraft = Minecraft.getInstance();
        // 仅当同步的UUID是本地玩家时才更新缓存
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerUuid)) {
            STATE_CACHE.put(playerUuid, new DiastoleState(stacks, cooldownEnd));
        }
    }

    /**
     * 清除玩家的舒张状态缓存
     * <p>
     * 当效果结束或玩家不再拥有该效果时调用
     *
     * @param playerUuid 玩家UUID
     */
    public static void clearDiastoleState(UUID playerUuid) {
        STATE_CACHE.remove(playerUuid);
    }

    /**
     * GUI渲染事件处理
     * <p>
     * 在每帧GUI渲染完成后执行：
     * <ol>
     *   <li>检查本地玩家是否有舒张状态</li>
     *   <li>生成显示文本（效果名称 + 叠层方块）</li>
     *   <li>创建HUD元素并注册到布局管理器</li>
     *   <li>如果无状态则注销HUD元素</li>
     * </ol>
     *
     * @param event GUI渲染事件（Post阶段）
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID playerUuid = minecraft.player.getUUID();
        DiastoleState state = STATE_CACHE.get(playerUuid);

        if (state != null) {
            long currentTime = minecraft.player.level().getGameTime();
            // 生成显示文本（包含叠层进度）
            String displayText = generateDisplayText(state.stacks, state.cooldownEnd, currentTime);

            // 创建HUD元素
            HUDLayoutManager.HUDElement element = HUDElementFactory.createHUDElement(
                    displayText,
                    ClientConfig.parseColor(ClientConfig.DIASTOLE_COLOR_TEXT.get()),
                    ClientConfig.parseColor(ClientConfig.DIASTOLE_COLOR_BACKGROUND.get()),
                    ClientConfig.HUD_PADDING.get(),
                    ClientConfig.GLOBAL_SCALE.get(),
                    // 可见性条件：有叠层或仍在冷却中
                    () -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null) return false;
                        DiastoleState currentState = STATE_CACHE.get(mc.player.getUUID());
                        long currentGameTime = mc.player.level().getGameTime();
                        return currentState != null && (currentState.stacks > 0 || currentGameTime < currentState.cooldownEnd);
                    }
            );

            // 注册HUD元素到布局管理器
            HUDLayoutManager.registerElement("diastole", element, playerUuid, currentTime);
        } else {
            // 无状态时注销HUD元素
            HUDLayoutManager.unregisterElement("diastole");
        }
    }

    /**
     * 生成HUD显示文本
     * <p>
     * 格式：效果名称 + 4个方块（■表示已叠层，□表示未叠层）
     * 冷却期间所有方块显示为空心（□）
     *
     * @param stacks 当前叠层数
     * @param cooldownEnd 冷却结束时间（游戏刻）
     * @param currentTime 当前游戏刻
     * @return 格式化后的显示字符串
     */
    private static String generateDisplayText(int stacks, long cooldownEnd, long currentTime) {
        StringBuilder sb = new StringBuilder();
        // 添加效果名称（从语言文件读取）
        sb.append(Component.translatable("gui.funky_effect_lib.diastole").getString());
        sb.append(" ");

        // 冷却期间显示为0层（全部空心）
        int displayStacks = stacks;
        if (currentTime < cooldownEnd) {
            displayStacks = 0;
        }

        // 生成4个方块表示叠层进度
        for (int i = 0; i < 4; i++) {
            if (i < displayStacks) {
                sb.append("§b■§r");  // 表示已叠层
            } else {
                sb.append("□");  // 表示未叠层
            }
        }
        return sb.toString();
    }

    /**
     * 舒张状态记录
     * <p>
     * 存储玩家的当前叠层数和冷却结束时间
     *
     * @param stacks 当前叠层数
     * @param cooldownEnd 冷却结束时间（游戏刻）
     */
    private record DiastoleState(int stacks, long cooldownEnd) {}
}