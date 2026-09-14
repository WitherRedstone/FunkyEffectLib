package com.chinaex123.funky_effect_lib.init;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("removal")
public class FELSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, FunkyEffectLib.MOD_ID);

    public static final RegistryObject<SoundEvent> IGNITE_EXPLODE = register("fel.effect.ignite_explode");
    public static final RegistryObject<SoundEvent> DIASTOLE = register("fel.effect.diastole");
    public static final RegistryObject<SoundEvent> DIASTOLE_1 = register("fel.effect.diastole_1");
    public static final RegistryObject<SoundEvent> DIASTOLE_2 = register("fel.effect.diastole_2");
    public static final RegistryObject<SoundEvent> DIASTOLE_3 = register("fel.effect.diastole_3");
    public static final RegistryObject<SoundEvent> DIASTOLE_4 = register("fel.effect.diastole_4");

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation location = new ResourceLocation(FunkyEffectLib.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(location));
    }
}