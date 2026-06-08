//package com.chinaex123.funky_effect_lib.client.renderer.effect;
//
//import com.chinaex123.funky_effect_lib.FunkyEffectLib;
//import com.chinaex123.funky_effect_lib.init.FELEffects;
//import net.minecraft.client.Minecraft;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.client.event.ViewportEvent;
//
//@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
//public class FlipGravityRender {
//
//    @SubscribeEvent
//    public static void onRenderCamera(ViewportEvent.ComputeCameraAngles event) {
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.player == null) return;
//
//        var effect = mc.player.getEffect(FELEffects.FLIP_GRAVITY);
//        if (effect == null) return;
//
//        event.setPitch(event.getPitch() + 180);
//    }
//}