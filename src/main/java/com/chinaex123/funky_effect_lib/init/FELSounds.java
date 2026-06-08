package com.chinaex123.funky_effect_lib.init;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class FELSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, FunkyEffectLib.MOD_ID);

    public static final Supplier<SoundEvent> IGNITE_EXPLODE = register("fel.effect.ignite_explode");

    private static Supplier<SoundEvent> register(String name) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(location));
    }
}
