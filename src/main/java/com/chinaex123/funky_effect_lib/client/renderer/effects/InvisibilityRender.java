package com.chinaex123.funky_effect_lib.client.renderer.effects;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 隐身渲染类：完全隐藏实体渲染（包括盔甲和手持物） **/
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class InvisibilityRender {

    /**
     * 完全隐藏实体渲染（包括盔甲和手持物）
     */
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(FELEffects.INVISIBILITY.get())) {
            event.setCanceled(true);
        }
    }

    /**
     * 隐藏名字标签
     */
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        LivingEntity entity = event.getEntity().getControllingPassenger();

        // 如果实体有隐身效果，直接取消事件渲染来隐藏名字标签
        if (entity != null && entity.hasEffect(FELEffects.INVISIBILITY.get())) {
            event.setCanceled(true);
        }
    }
}