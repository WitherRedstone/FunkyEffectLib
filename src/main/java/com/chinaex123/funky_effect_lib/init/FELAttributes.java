package com.chinaex123.funky_effect_lib.init;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FELAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, FunkyEffectLib.MOD_ID);

    /** 伤害减免 **/
    public static final RegistryObject<Attribute> DAMAGE_REDUCTION = ATTRIBUTES.register("damage_reduction",
            () -> new RangedAttribute("attribute." + FunkyEffectLib.MOD_ID + ".damage_reduction", 0.0, 0.0, 1.0).setSyncable(true));

    @SubscribeEvent
    public static void addAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().stream()
                .filter(type -> type.getBaseClass().isAssignableFrom(LivingEntity.class))
                .forEach(type -> event.add(type, DAMAGE_REDUCTION.get()));
    }
}
