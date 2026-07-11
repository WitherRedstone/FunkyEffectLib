package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.BoltChargeClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** 电光充能的同步包 **/
public record BoltChargeSyncPacket(UUID entityId, int chargeCount) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BoltChargeSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "bolt_charge_sync"));

    public static final StreamCodec<FriendlyByteBuf, BoltChargeSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(BoltChargeSyncPacket::encode, BoltChargeSyncPacket::decode);

    private static BoltChargeSyncPacket decode(FriendlyByteBuf buf) {
        return new BoltChargeSyncPacket(buf.readUUID(), buf.readInt());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(chargeCount);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 处理接收到的同步包，更新客户端充能数据 **/
    public static void handle(BoltChargeSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            BoltChargeClient.setChargeCount(packet.entityId(), packet.chargeCount());
        });
    }
}