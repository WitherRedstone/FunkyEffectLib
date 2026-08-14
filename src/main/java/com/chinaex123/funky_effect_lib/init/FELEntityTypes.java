package com.chinaex123.funky_effect_lib.init;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.entity.AfterimageClone;
import com.chinaex123.funky_effect_lib.entity.Threadling;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class FELEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, FunkyEffectLib.MOD_ID);

    /** 残影分身 */
    public static final Supplier<EntityType<AfterimageClone>> AFTERIMAGE_CLONE = ENTITY_TYPES.register(
            "afterimage_clone",
            () -> EntityType.Builder.<AfterimageClone>of(AfterimageClone::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .fireImmune()
                    .build(FunkyEffectLib.MOD_ID + ":afterimage_clone")
    );

    /** 线虫 */
    public static final Supplier<EntityType<Threadling>> THREADLING = ENTITY_TYPES.register(
            "threadling", () -> EntityType.Builder.of(Threadling::new, MobCategory.MISC)
                    .sized(0.4F, 0.3F)
                    .clientTrackingRange(64)
                    .updateInterval(3)
                    .fireImmune()
                    .build(FunkyEffectLib.MOD_ID + ":threadling")
    );
}