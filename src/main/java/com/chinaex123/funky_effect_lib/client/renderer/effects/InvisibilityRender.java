package com.chinaex123.funky_effect_lib.client.renderer.effects;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;

/**
 * 隐身渲染类
 * <p>
 * 功能：完全隐藏拥有隐身效果的实体的渲染
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class InvisibilityRender {

    /**
     * 实体渲染事件处理
     * 完全隐藏拥有隐身效果的实体渲染
     *
     * @param event 实体渲染事件
     */
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        // 检查实体是否为LivingEntity且拥有隐身效果
        if (event.getEntity() instanceof LivingEntity entity && entity.hasEffect(FELEffects.INVISIBILITY)) {
            // 取消实体渲染，实现完全隐身
            event.setCanceled(true);
        }
    }

    /**
     * 名字标签渲染事件处理
     * 隐藏拥有隐身效果的实体的名字标签
     *
     * @param event 名字标签渲染事件
     */
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        // 获取实体的控制乘客（如果是载具则获取乘客，否则获取实体本身）
        LivingEntity entity = event.getEntity().getControllingPassenger();

        // 如果实体拥有隐身效果，不显示名字标签
        if (entity != null && entity.hasEffect(FELEffects.INVISIBILITY)) {
            // 设置为不渲染
            event.setCanRender(TriState.FALSE);
        }
    }
}