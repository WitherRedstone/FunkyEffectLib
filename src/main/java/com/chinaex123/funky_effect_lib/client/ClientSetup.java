package com.chinaex123.funky_effect_lib.client;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.model.ThreadlingModel;
import com.chinaex123.funky_effect_lib.client.renderer.AfterimageCloneRenderer;
import com.chinaex123.funky_effect_lib.client.renderer.entity.ThreadlingRenderer;
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
        event.registerEntityRenderer(FELEntityTypes.THREADLING.get(), ThreadlingRenderer::new);
    }

    /**
     * 注册所有模型层定义
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 注册线虫的模型层
        event.registerLayerDefinition(ThreadlingModel.LAYER_LOCATION, ThreadlingModel::createBodyLayer);
    }
}