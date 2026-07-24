package com.chinaex123.funky_effect_lib;

import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import com.chinaex123.funky_effect_lib.entity.AfterimageClone;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.init.FELEntityTypes;
import com.chinaex123.funky_effect_lib.init.FELSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(FunkyEffectLib.MOD_ID)
@SuppressWarnings("removal")
public class FunkyEffectLib {
    public static final String MOD_ID = "funky_effect_lib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FunkyEffectLib() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        FELEffects.EFFECTS.register(modEventBus);
        FELSounds.SOUND_EVENTS.register(modEventBus);
        FELEntityTypes.ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(FunkyEffectLib::registerAttributes);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(FELEntityTypes.AFTERIMAGE_CLONE.get(), AfterimageClone.createAttributes());
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}