//package com.chinaex123.funky_effect_lib.mixin;
//
//import com.chinaex123.funky_effect_lib.init.FELEffects;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.MouseHandler;
//import net.minecraft.world.effect.MobEffectInstance;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.ModifyArgs;
//import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
//
//@Mixin(MouseHandler.class)
//public class MouseHandlerMixin {
//
//    @ModifyArgs(
//            method = "turnPlayer",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
//            )
//    )
//    private void reverseMouseBoth(Args args) {
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.player == null) return;
//
//        MobEffectInstance effect = mc.player.getEffect(FELEffects.FLIP_GRAVITY);
//        if (effect != null) {
//            double yawDelta = args.get(0);
//            double pitchDelta = args.get(1);
//
//            args.set(0, -yawDelta);
//            args.set(1, -pitchDelta);
//        }
//    }
//}