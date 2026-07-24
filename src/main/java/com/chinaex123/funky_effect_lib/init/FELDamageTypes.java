package com.chinaex123.funky_effect_lib.init;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class FELDamageTypes {

    /** 震颤伤害类型：无击退效果 **/
    public static final ResourceKey<DamageType> JOIT = create("joit");
    /** 触电伤害类型：无视护甲、无视伤害减免、无视抗性 **/
    public static final ResourceKey<DamageType> ELECTRIC_SHOCK = create("electric_shock");
    /** 流血伤害类型：无视护甲、无击退效果 **/
    public static final ResourceKey<DamageType> BLEEDING = create("bleeding");
    /** 辐射伤害类型：无视护甲、无视伤害减免、无视抗性 **/
    public static final ResourceKey<DamageType> RADIATION = create("radiation");
    /** 魔力灼烧伤害类型：无视护甲、无视伤害减免、无视抗性 **/
    public static final ResourceKey<DamageType> MANA_BURN = create("mana_burn");
    /** 真空侵蚀伤害类型：无视护甲、无视伤害减免、无视抗性 **/
    public static final ResourceKey<DamageType> VACUUM_EROSION = create("vacuum_erosion");
    /** 真实伤害类型：全部无视 **/
    public static final ResourceKey<DamageType> REAL_DAMAGE = create("real_damage");

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(
                Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, name)
        );
    }
}