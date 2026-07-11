package com.chinaex123.funky_effect_lib.init;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.effect.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public interface FELEffects {
    DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, FunkyEffectLib.MOD_ID);

    // ========================= 命运2效果 =========================
    // 暗影
    RegistryObject<MobEffect> DARKNESS = EFFECTS.register("darkness", () -> new Darkness(0x996300));
    RegistryObject<MobEffect> CREEPING_DARKNESS = EFFECTS.register("creeping_darkness", () -> new CreepingDarkness(0x996300));
    RegistryObject<MobEffect> PERVADING_DARKNESS = EFFECTS.register("pervading_darkness", () -> new PervadingDarkness(0x996300));
    // 虚空
    RegistryObject<MobEffect> INVISIBILITY = EFFECTS.register("invisibility", () -> new Invisibility(0x99099));
    RegistryObject<MobEffect> VOLATILE = EFFECTS.register("volatile", () -> new Volatile(0x99099));
    RegistryObject<MobEffect> DEVOUR = EFFECTS.register("devour", () -> new Devour(0x99099));
    RegistryObject<MobEffect> SUPPRESSION = EFFECTS.register("suppression", () -> new Suppression(0x99099));
    RegistryObject<MobEffect> VULNERABLE = EFFECTS.register("vulnerable", () -> new Vulnerable(0x99099));
    // 烈日
    RegistryObject<MobEffect> CURE = EFFECTS.register("cure", () -> new Cure(0xCC6600));
    RegistryObject<MobEffect> RESTORATION = EFFECTS.register("restoration", () -> new Restoration(0xCC6600));
    RegistryObject<MobEffect> RADIANT = EFFECTS.register("radiant", () -> new Radiant(0xCC6600));
    RegistryObject<MobEffect> SCORCH = EFFECTS.register("scorch", () -> new Scorch(0xCC6600));
    RegistryObject<MobEffect> IGNITE = EFFECTS.register("ignite", () -> new Ignite(0xCC6600));
    // 电弧
    RegistryObject<MobEffect> AMPLIFIED = EFFECTS.register("amplified", () -> new Amplified(0x00FFFF));
    RegistryObject<MobEffect> JOLT = EFFECTS.register("jolt", () -> new Jolt(0x00FFFF));
    RegistryObject<MobEffect> BLIND = EFFECTS.register("blind", () -> new Blind(0x00FFFF));
    RegistryObject<MobEffect> BOLT_CHARGE = EFFECTS.register("bolt_charge", () -> new BoltCharge(0x00FFFF));
    // 缚丝
    RegistryObject<MobEffect> SEVER = EFFECTS.register("sever", () -> new Sever(0x008000));
    RegistryObject<MobEffect> WOVEN_MAIL = EFFECTS.register("woven_mail", () -> new WovenMail(0x008000));
    // 冰影
    RegistryObject<MobEffect> SLOW = EFFECTS.register("slow", () -> new Slow(0x1F91DC));
    RegistryObject<MobEffect> FREEZE = EFFECTS.register("freeze", () -> new Freeze(0x1F91DC));
    RegistryObject<MobEffect> SHATTERER = EFFECTS.register("shatter", () -> new Shatterer(0x1F91DC));
    RegistryObject<MobEffect> FROST_ARMOR = EFFECTS.register("frost_armor", () -> new FrostArmor(0x1F91DC));

    // ========================= 其他效果 =========================
    RegistryObject<MobEffect> WITHERED_SLASH = EFFECTS.register("withered_slash", () -> new WitheredSlash(0x555555));
    RegistryObject<MobEffect> RAGE = EFFECTS.register("rage", () -> new Rage(0xFF0000));
    RegistryObject<MobEffect> FIRE_ATTACK = EFFECTS.register("fire_attack", () -> new FireAttack(0xFF4500));
    RegistryObject<MobEffect> PAYBACK = EFFECTS.register("payback", () -> new Payback(0x8B0000));
    RegistryObject<MobEffect> BLOODTHIRSTY = EFFECTS.register("bloodthirsty", () -> new Bloodthirsty(0x8B0000));
    RegistryObject<MobEffect> HARDENED = EFFECTS.register("hardened", () -> new Hardened(0x808080));
    RegistryObject<MobEffect> NATURES_BLESSING = EFFECTS.register("natures_blessing", () -> new NaturesBlessing(0x228B22));
    RegistryObject<MobEffect> FROSTBITE = EFFECTS.register("frostbite", () -> new Frostbite(0x87CEEB));
    RegistryObject<MobEffect> FROSTFALL = EFFECTS.register("frostfall", () -> new Frostfall(0x4169E1));
    RegistryObject<MobEffect> HAPPY = EFFECTS.register("happy", () -> new Happy(0xFFD700));
    RegistryObject<MobEffect> HARDENED_SKIN = EFFECTS.register("hardened_skin", () -> new HardenedSkin(0x8B4513));
    RegistryObject<MobEffect> FRAGILE_SKIN = EFFECTS.register("fragile_skin", () -> new FragileSkin(0xD3D3D3));
    RegistryObject<MobEffect> LAVA_VISION = EFFECTS.register("lava_vision", () -> new LavaVision(0xFF4500));
    RegistryObject<MobEffect> FLAME_WALKER = EFFECTS.register("flame_walker", () -> new FlameWalker(0xFF4500));
    RegistryObject<MobEffect> WAVE_WALKER = EFFECTS.register("wave_walker", () -> new WaveWalker(0x4169E1));
    RegistryObject<MobEffect> IRRITABLE = EFFECTS.register("irritable", () -> new Irritable(0xFF6347));
    RegistryObject<MobEffect> ANGRY = EFFECTS.register("angry", () -> new Angry(0xB22222));
    RegistryObject<MobEffect> CALM = EFFECTS.register("calm", () -> new Calm(0xADD8E6));
    RegistryObject<MobEffect> ACHE = EFFECTS.register("ache", () -> new Ache(0x696969));
    RegistryObject<MobEffect> BATTLE_FRENZY = EFFECTS.register("battle_frenzy", () -> new BattleFrenzy(0x8B0000));
    RegistryObject<MobEffect> KNOWLEDGE_HARDENING = EFFECTS.register("knowledge_hardening", () -> new KnowledgeHardening(0x4169E1));
    RegistryObject<MobEffect> ELECTRIC_SHOCK = EFFECTS.register("electric_shock", () -> new ElectricShock(0x1F91DC));
    RegistryObject<MobEffect> FRAGMENTATION = EFFECTS.register("fragmentation", () -> new Fragmentation(0x808080));
    RegistryObject<MobEffect> UNCHARTED = EFFECTS.register("uncharted", () -> new Uncharted(0xFF00FF));
    RegistryObject<MobEffect> BLEEDING = EFFECTS.register("bleeding", () -> new Bleeding(0x8B0000));
    RegistryObject<MobEffect> RADIATION = EFFECTS.register("radiation", () -> new Radiation(0x00FF00));
    RegistryObject<MobEffect> MANA_BURN = EFFECTS.register("mana_burn", () -> new ManaBurn(0x9400D3));
    RegistryObject<MobEffect> VACUUM_EROSION = EFFECTS.register("vacuum_erosion", () -> new VacuumErosion(0x000080));
    RegistryObject<MobEffect> IMPURITY = EFFECTS.register("impurity", () -> new Impurity(0x8B008B));
    RegistryObject<MobEffect> TREASURE_FINDER = EFFECTS.register("treasure_finder", () -> new TreasureFinder(0xFFD700));
    RegistryObject<MobEffect> DANGER_SENSE = EFFECTS.register("danger_sense", () -> new DangerSense(0xFF0000));
    RegistryObject<MobEffect> PROSPECTOR = EFFECTS.register("prospector", () -> new Prospector(0x8B4513));
    RegistryObject<MobEffect> TAUNT = EFFECTS.register("taunt", () -> new Taunt(0xFF0000));
    RegistryObject<MobEffect> AFTERIMAGE = EFFECTS.register("afterimage", () -> new Afterimage(0x808080));
    RegistryObject<MobEffect> DISCERN = EFFECTS.register("discern", () -> new Discern(0xFFD700));
    RegistryObject<MobEffect> BORROWED_TIME = EFFECTS.register("borrowed_time", () -> new BorrowedTime(0x808080));
    RegistryObject<MobEffect> FLESH_REGROWTH = EFFECTS.register("flesh_regrowth", () -> new FleshRegrowth(0x228B22));
    RegistryObject<MobEffect> IRON_WILL = EFFECTS.register("iron_will", () -> new IronWill(0xC0C0C0));
    RegistryObject<MobEffect> AVARICE = EFFECTS.register("avarice", () -> new Avarice(0xFFD700));
    RegistryObject<MobEffect> BOUNTIFUL = EFFECTS.register("bountiful", () -> new Bountiful(0x228B22));
    RegistryObject<MobEffect> CRYSTAL_SHIELD = EFFECTS.register("crystal_shield", () -> new CrystalShield(0x87CEEB));
    RegistryObject<MobEffect> DICE_OF_FATE = EFFECTS.register("dice_of_fate", () -> new DiceOfFate(0xFF00FF));
    RegistryObject<MobEffect> OVERHEAL = EFFECTS.register("overheal", () -> new Overheal(0x00FF00));
    RegistryObject<MobEffect> DOOM_FORETOLD = EFFECTS.register("doom_foretold", () -> new DoomForetold(0x8B0000));
    RegistryObject<MobEffect> RESONANT_BURST = EFFECTS.register("resonant_burst", () -> new ResonantBurst(0xFFD700));
    RegistryObject<MobEffect> DISINTEGRATE = EFFECTS.register("disintegrate", () -> new Disintegrate(0xFFFFFF));
    RegistryObject<MobEffect> STINGER = EFFECTS.register("stinger", () -> new Stinger(0x00FF00));
    RegistryObject<MobEffect> SOULBURN = EFFECTS.register("soulburn", () -> new Soulburn(0x9400D3));

}
