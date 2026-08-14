package com.chinaex123.funky_effect_lib.init;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.effect.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface FELEffects {
    DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, FunkyEffectLib.MOD_ID);

    // ========================= 命运2效果 =========================
    // 暗影
    DeferredHolder<MobEffect, MobEffect> DARKNESS = EFFECTS.register("darkness", () -> new Darkness(0x996300));
    DeferredHolder<MobEffect, MobEffect> CREEPING_DARKNESS = EFFECTS.register("creeping_darkness", () -> new CreepingDarkness(0x996300));
    DeferredHolder<MobEffect, MobEffect> PERVADING_DARKNESS = EFFECTS.register("pervading_darkness", () -> new PervadingDarkness(0x996300));
    // 虚空
    DeferredHolder<MobEffect, MobEffect> INVISIBILITY = EFFECTS.register("invisibility", () -> new Invisibility(0x99099));
    DeferredHolder<MobEffect, MobEffect> VOLATILE = EFFECTS.register("volatile", () -> new Volatile(0x99099));
    DeferredHolder<MobEffect, MobEffect> DEVOUR = EFFECTS.register("devour", () -> new Devour(0x99099));
    DeferredHolder<MobEffect, MobEffect> SUPPRESSION = EFFECTS.register("suppression", () -> new Suppression(0x99099));
    DeferredHolder<MobEffect, MobEffect> VULNERABLE = EFFECTS.register("vulnerable", () -> new Vulnerable(0x99099));
    // 烈日
    DeferredHolder<MobEffect, MobEffect> CURE = EFFECTS.register("cure", () -> new Cure(0xCC6600));
    DeferredHolder<MobEffect, MobEffect> RESTORATION = EFFECTS.register("restoration", () -> new Restoration(0xCC6600));
    DeferredHolder<MobEffect, MobEffect> RADIANT = EFFECTS.register("radiant", () -> new Radiant(0xCC6600));
    DeferredHolder<MobEffect, MobEffect> SCORCH = EFFECTS.register("scorch", () -> new Scorch(0xCC6600));
    DeferredHolder<MobEffect, MobEffect> IGNITE = EFFECTS.register("ignite", () -> new Ignite(0xCC6600));
    // 电弧
    DeferredHolder<MobEffect, MobEffect> AMPLIFIED = EFFECTS.register("amplified", () -> new Amplified(0x00FFFF));
    DeferredHolder<MobEffect, MobEffect> JOLT = EFFECTS.register("jolt", () -> new Jolt(0x00FFFF));
    DeferredHolder<MobEffect, MobEffect> BLIND = EFFECTS.register("blind", () -> new Blind(0x00FFFF));
    DeferredHolder<MobEffect, MobEffect> BOLT_CHARGE = EFFECTS.register("bolt_charge", () -> new BoltCharge(0x00FFFF));
    // 缚丝
    // 缚丝
    DeferredHolder<MobEffect, MobEffect> THREADLING = EFFECTS.register("threadling", () -> new Threadling(0x008000));
    DeferredHolder<MobEffect, MobEffect> SEVER = EFFECTS.register("sever", () -> new Sever(0x008000));
    DeferredHolder<MobEffect, MobEffect> WOVEN_MAIL = EFFECTS.register("woven_mail", () -> new WovenMail(0x008000));
    // 冰影
    DeferredHolder<MobEffect, MobEffect> SLOW = EFFECTS.register("slow", () -> new Slow(0x1F91DC));
    DeferredHolder<MobEffect, MobEffect> FREEZE = EFFECTS.register("freeze", () -> new Freeze(0x1F91DC));
    DeferredHolder<MobEffect, MobEffect> SHATTERER = EFFECTS.register("shatter", () -> new Shatterer(0x1F91DC));
    DeferredHolder<MobEffect, MobEffect> FROST_ARMOR = EFFECTS.register("frost_armor", () -> new FrostArmor(0x1F91DC));

    // ========================= 其他效果 =========================
    DeferredHolder<MobEffect, MobEffect> WITHERED_SLASH = EFFECTS.register("withered_slash", () -> new WitheredSlash(0x555555));
    DeferredHolder<MobEffect, MobEffect> RAGE = EFFECTS.register("rage", () -> new Rage(0xFF0000));
    DeferredHolder<MobEffect, MobEffect> FIRE_ATTACK = EFFECTS.register("fire_attack", () -> new FireAttack(0xFF4500));
    DeferredHolder<MobEffect, MobEffect> PAYBACK = EFFECTS.register("payback", () -> new Payback(0x8B0000));
    DeferredHolder<MobEffect, MobEffect> BLOODTHIRSTY = EFFECTS.register("bloodthirsty", () -> new Bloodthirsty(0x8B0000));
    DeferredHolder<MobEffect, MobEffect> HARDENED = EFFECTS.register("hardened", () -> new Hardened(0x808080));
    DeferredHolder<MobEffect, MobEffect> NATURES_BLESSING = EFFECTS.register("natures_blessing", () -> new NaturesBlessing(0x228B22));
    DeferredHolder<MobEffect, MobEffect> FROSTBITE = EFFECTS.register("frostbite", () -> new Frostbite(0x87CEEB));
    DeferredHolder<MobEffect, MobEffect> FROSTFALL = EFFECTS.register("frostfall", () -> new Frostfall(0x4169E1));
    DeferredHolder<MobEffect, MobEffect> MAXIMIZATION = EFFECTS.register("maximization", () -> new Maximization(0xFF66FF));
    DeferredHolder<MobEffect, MobEffect> MINIFY = EFFECTS.register("minify", () -> new Minify(0xFF66FF));
    DeferredHolder<MobEffect, MobEffect> HAPPY = EFFECTS.register("happy", () -> new Happy(0xFFD700));
    DeferredHolder<MobEffect, MobEffect> HARDENED_SKIN = EFFECTS.register("hardened_skin", () -> new HardenedSkin(0x8B4513));
    DeferredHolder<MobEffect, MobEffect> FRAGILE_SKIN = EFFECTS.register("fragile_skin", () -> new FragileSkin(0xD3D3D3));
    DeferredHolder<MobEffect, MobEffect> LAVA_VISION = EFFECTS.register("lava_vision", () -> new LavaVision(0xFF4500));
    DeferredHolder<MobEffect, MobEffect> FLAME_WALKER = EFFECTS.register("flame_walker", () -> new FlameWalker(0xFF4500));
    DeferredHolder<MobEffect, MobEffect> WAVE_WALKER = EFFECTS.register("wave_walker", () -> new WaveWalker(0x4169E1));
    DeferredHolder<MobEffect, MobEffect> IRRITABLE = EFFECTS.register("irritable", () -> new Irritable(0xFF6347));
    DeferredHolder<MobEffect, MobEffect> ANGRY = EFFECTS.register("angry", () -> new Angry(0xB22222));
    DeferredHolder<MobEffect, MobEffect> CALM = EFFECTS.register("calm", () -> new Calm(0xADD8E6));
    DeferredHolder<MobEffect, MobEffect> ACHE = EFFECTS.register("ache", () -> new Ache(0x696969));
    DeferredHolder<MobEffect, MobEffect> BATTLE_FRENZY = EFFECTS.register("battle_frenzy", () -> new BattleFrenzy(0x8B0000));
    DeferredHolder<MobEffect, MobEffect> KNOWLEDGE_HARDENING = EFFECTS.register("knowledge_hardening", () -> new KnowledgeHardening(0x4169E1));
    DeferredHolder<MobEffect, MobEffect> ELECTRIC_SHOCK = EFFECTS.register("electric_shock", () -> new ElectricShock(0x1F91DC));
    DeferredHolder<MobEffect, MobEffect> FRAGMENTATION = EFFECTS.register("fragmentation", () -> new Fragmentation(0x808080));
    DeferredHolder<MobEffect, MobEffect> UNCHARTED = EFFECTS.register("uncharted", () -> new Uncharted(0xFF00FF));
    DeferredHolder<MobEffect, MobEffect> BLEEDING = EFFECTS.register("bleeding", () -> new Bleeding(0x8B0000));
    DeferredHolder<MobEffect, MobEffect> RADIATION = EFFECTS.register("radiation", () -> new Radiation(0x00FF00));
    DeferredHolder<MobEffect, MobEffect> MANA_BURN = EFFECTS.register("mana_burn", () -> new ManaBurn(0x9400D3));
    DeferredHolder<MobEffect, MobEffect> VACUUM_EROSION = EFFECTS.register("vacuum_erosion", () -> new VacuumErosion(0x000080));
    DeferredHolder<MobEffect, MobEffect> IMPURITY = EFFECTS.register("impurity", () -> new Impurity(0x8B008B));
    DeferredHolder<MobEffect, MobEffect> TREASURE_FINDER = EFFECTS.register("treasure_finder", () -> new TreasureFinder(0xFFD700));
    DeferredHolder<MobEffect, MobEffect> DANGER_SENSE = EFFECTS.register("danger_sense", () -> new DangerSense(0xFF0000));
    DeferredHolder<MobEffect, MobEffect> PROSPECTOR = EFFECTS.register("prospector", () -> new Prospector(0x8B4513));
    DeferredHolder<MobEffect, MobEffect> TAUNT = EFFECTS.register("taunt", () -> new Taunt(0xFF0000));
    DeferredHolder<MobEffect, MobEffect> AFTERIMAGE = EFFECTS.register("afterimage", () -> new Afterimage(0x808080));
    DeferredHolder<MobEffect, MobEffect> DISCERN = EFFECTS.register("discern", () -> new Discern(0xFFD700));
    DeferredHolder<MobEffect, MobEffect> BORROWED_TIME = EFFECTS.register("borrowed_time", () -> new BorrowedTime(0x808080));
    DeferredHolder<MobEffect, MobEffect> FLESH_REGROWTH = EFFECTS.register("flesh_regrowth", () -> new FleshRegrowth(0x228B22));
    DeferredHolder<MobEffect, MobEffect> IRON_WILL = EFFECTS.register("iron_will", () -> new IronWill(0xC0C0C0));
    DeferredHolder<MobEffect, MobEffect> AVARICE = EFFECTS.register("avarice", () -> new Avarice(0xFFD700));
    DeferredHolder<MobEffect, MobEffect> BOUNTIFUL = EFFECTS.register("bountiful", () -> new Bountiful(0x228B22));
    DeferredHolder<MobEffect, MobEffect> CRYSTAL_SHIELD = EFFECTS.register("crystal_shield", () -> new CrystalShield(0x87CEEB));
    DeferredHolder<MobEffect, MobEffect> DICE_OF_FATE = EFFECTS.register("dice_of_fate", () -> new DiceOfFate(0xFF00FF));
    DeferredHolder<MobEffect, MobEffect> OVERHEAL = EFFECTS.register("overheal", () -> new Overheal(0x00FF00));
    DeferredHolder<MobEffect, MobEffect> DOOM_FORETOLD = EFFECTS.register("doom_foretold", () -> new DoomForetold(0x8B0000));
    DeferredHolder<MobEffect, MobEffect> RESONANT_BURST = EFFECTS.register("resonant_burst", () -> new ResonantBurst(0xFFD700));
    DeferredHolder<MobEffect, MobEffect> DISINTEGRATE = EFFECTS.register("disintegrate", () -> new Disintegrate(0xFFFFFF));
    DeferredHolder<MobEffect, MobEffect> STINGER = EFFECTS.register("stinger", () -> new Stinger(0x00FF00));
    DeferredHolder<MobEffect, MobEffect> SOULBURN = EFFECTS.register("soulburn", () -> new Soulburn(0x9400D3));

}
