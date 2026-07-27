package com.chinaex123.funky_effect_lib.network;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.network.effect.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** 网络处理类，用于注册和处理网络包 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class NetworkHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0");

        // 冰冻铠甲的同步包
        registrar.playToClient(
                FrostArmorSyncPacket.TYPE,
                FrostArmorSyncPacket.STREAM_CODEC,
                FrostArmorSyncPacket::handle
        );

        // 织造铠甲的同步包
        registrar.playToClient(
                WovenMailSyncPacket.TYPE,
                WovenMailSyncPacket.STREAM_CODEC,
                WovenMailSyncPacket::handle
        );

        // 厄运预兆的同步包
        registrar.playToClient(
                DoomMarkSyncPacket.TYPE,
                DoomMarkSyncPacket.STREAM_CODEC,
                DoomMarkSyncPacket::handle
        );

        // 魂燃的同步包
        registrar.playToClient(
                SoulburnSyncPacket.TYPE,
                SoulburnSyncPacket.STREAM_CODEC,
                SoulburnSyncPacket::handle
        );

        // 蔓延黑暗的同步包
        registrar.playToClient(
                CreepingDarknessSyncPacket.TYPE,
                CreepingDarknessSyncPacket.STREAM_CODEC,
                CreepingDarknessSyncPacket::handle
        );

        // 弥漫暗影的同步包
        registrar.playToClient(
                PervadingDarknessSyncPacket.TYPE,
                PervadingDarknessSyncPacket.STREAM_CODEC,
                PervadingDarknessSyncPacket::handle
        );

        // 电光充能的同步包
        registrar.playToClient(
                BoltChargeSyncPacket.TYPE,
                BoltChargeSyncPacket.STREAM_CODEC,
                BoltChargeSyncPacket::handle
        );

        // 减速的同步包
        registrar.playToClient(
                SlowSyncPacket.TYPE,
                SlowSyncPacket.STREAM_CODEC,
                SlowSyncPacket::handle
        );

        // 灼烧的同步包
        registrar.playToClient(
                ScorchSyncPacket.TYPE,
                ScorchSyncPacket.STREAM_CODEC,
                ScorchSyncPacket::handle
        );
    }
}