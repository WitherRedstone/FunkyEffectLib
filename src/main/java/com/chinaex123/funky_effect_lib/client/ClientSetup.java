package com.chinaex123.funky_effect_lib.client;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.renderer.AfterimageCloneRenderer;
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
    }
}