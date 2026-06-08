package com.chinaex123.funky_effect_lib.client;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.renderer.AfterimageCloneRenderer;
import com.chinaex123.funky_effect_lib.init.FELEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 客户端设置类 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FELEntityTypes.AFTERIMAGE_CLONE.get(), AfterimageCloneRenderer::new);
    }
}