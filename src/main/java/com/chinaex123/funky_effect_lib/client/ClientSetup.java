package com.chinaex123.funky_effect_lib.client;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.model.ThreadlingModel;
import com.chinaex123.funky_effect_lib.client.renderer.AfterimageCloneRenderer;
import com.chinaex123.funky_effect_lib.client.renderer.entity.ThreadlingRenderer;
import com.chinaex123.funky_effect_lib.init.FELEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = FunkyEffectLib.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID, value = Dist.CLIENT)
public class ClientSetup {

    public ClientSetup(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

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