package com.chinaex123.funky_effect_lib.init;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class FELAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, FunkyEffectLib.MOD_ID);

    /** 伤害减免 **/
    public static final Holder<Attribute> DAMAGE_REDUCTION = ATTRIBUTES.register("damage_reduction",
            () -> new PercentageAttribute("attribute." + FunkyEffectLib.MOD_ID + ".damage_reduction", 0.0, 0.0, 1.0).setSyncable(true));

    @SubscribeEvent
    public static void addAttributes(EntityAttributeModificationEvent event) {
        event.getTypes().stream()
                .filter(type -> type.getBaseClass().isAssignableFrom(LivingEntity.class))
                .forEach(type -> event.add(type, DAMAGE_REDUCTION));
    }
}
