package com.chinaex123.funky_effect_lib;

import com.chinaex123.funky_effect_lib.client.config.ClientConfig;
import com.chinaex123.funky_effect_lib.entity.AfterimageClone;
import com.chinaex123.funky_effect_lib.init.FELAttributes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.init.FELEntityTypes;
import com.chinaex123.funky_effect_lib.init.FELSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

@Mod(FunkyEffectLib.MOD_ID)
public class FunkyEffectLib {
    public static final String MOD_ID = "funky_effect_lib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FunkyEffectLib(IEventBus modEventBus, ModContainer modContainer) {
        FELEffects.EFFECTS.register(modEventBus);
        FELSounds.SOUND_EVENTS.register(modEventBus);
        FELEntityTypes.ENTITY_TYPES.register(modEventBus);
        FELAttributes.ATTRIBUTES.register(modEventBus);

        modEventBus.addListener(FunkyEffectLib::registerAttributes);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(FELEntityTypes.AFTERIMAGE_CLONE.get(), AfterimageClone.createAttributes());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

}