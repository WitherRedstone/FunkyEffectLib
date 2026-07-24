package com.chinaex123.funky_effect_lib.network;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.network.effect.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** 网络处理类，用于注册和处理网络包 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkHandler {

    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            FunkyEffectLib.id("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    /** 注册网络包 **/
    @SubscribeEvent
    public static void register(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 冰冻铠甲的同步包
            CHANNEL.registerMessage(packetId++, FrostArmorSyncPacket.class,
                    FrostArmorSyncPacket::encode,
                    FrostArmorSyncPacket::decode,
                    FrostArmorSyncPacket::handle
            );

            // 织造铠甲的同步包
            CHANNEL.registerMessage(packetId++, WovenMailSyncPacket.class,
                    WovenMailSyncPacket::encode,
                    WovenMailSyncPacket::decode,
                    WovenMailSyncPacket::handle
            );

            // 厄运预兆的同步包
            CHANNEL.registerMessage(packetId++, DoomMarkSyncPacket.class,
                    DoomMarkSyncPacket::encode,
                    DoomMarkSyncPacket::decode,
                    DoomMarkSyncPacket::handle
            );

            // 魂燃的同步包
            CHANNEL.registerMessage(packetId++, SoulburnSyncPacket.class,
                    SoulburnSyncPacket::encode,
                    SoulburnSyncPacket::decode,
                    SoulburnSyncPacket::handle
            );

            // 蔓延黑暗的同步包
            CHANNEL.registerMessage(packetId++, CreepingDarknessSyncPacket.class,
                    CreepingDarknessSyncPacket::encode,
                    CreepingDarknessSyncPacket::decode,
                    CreepingDarknessSyncPacket::handle
            );

            // 弥漫暗影的同步包
            CHANNEL.registerMessage(packetId++, PervadingDarknessSyncPacket.class,
                    PervadingDarknessSyncPacket::encode,
                    PervadingDarknessSyncPacket::decode,
                    PervadingDarknessSyncPacket::handle
            );

            // 电光充能的同步包
            CHANNEL.registerMessage(packetId++, BoltChargeSyncPacket.class,
                    BoltChargeSyncPacket::encode,
                    BoltChargeSyncPacket::decode,
                    BoltChargeSyncPacket::handle
            );

            // 减速的同步包
            CHANNEL.registerMessage(packetId++, SlowSyncPacket.class,
                    SlowSyncPacket::encode,
                    SlowSyncPacket::decode,
                    SlowSyncPacket::handle
            );

        });
    }
}