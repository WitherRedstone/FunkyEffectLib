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

/** 隐身渲染类：完全隐藏实体渲染（包括盔甲和手持物） **/
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class InvisibilityRender {

    /**
     * 完全隐藏实体渲染（包括盔甲和手持物）
     */
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity() instanceof LivingEntity entity && entity.hasEffect(FELEffects.INVISIBILITY)) {
            event.setCanceled(true);
        }
    }

    /**
     * 隐藏名字标签
     */
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        LivingEntity entity = event.getEntity().getControllingPassenger();

        // 如果实体有隐身效果，不显示名字标签
        if (entity != null && entity.hasEffect(FELEffects.INVISIBILITY)) {
            event.setCanRender(TriState.FALSE);
        }
    }
}