package com.chinaex123.funky_effect_lib.client.renderer.effects;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 隐身渲染类
 * <p>
 * 功能：完全隐藏拥有隐身效果的实体的渲染
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class InvisibilityRender {

    /**
     * 实体渲染事件处理
     * 完全隐藏拥有隐身效果的实体渲染
     *
     * @param event 实体渲染事件
     */
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        // 检查实体是否拥有隐身效果
        if (entity.hasEffect(FELEffects.INVISIBILITY.get())) {
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
        // 获取实体的控制乘客
        LivingEntity entity = event.getEntity().getControllingPassenger();

        // 如果实体拥有隐身效果，不显示名字标签
        if (entity != null && entity.hasEffect(FELEffects.INVISIBILITY.get())) {
            // 取消名字标签渲染
            event.setCanceled(true);
        }
    }
}