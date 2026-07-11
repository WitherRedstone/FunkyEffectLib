package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.BoltChargeClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 电光充能的同步包 **/
public record BoltChargeSyncPacket(UUID entityId, int chargeCount) {

    /** 将数据编码到网络缓冲区 **/
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(chargeCount);
    }

    /** 从网络缓冲区解码数据 **/
    public static BoltChargeSyncPacket decode(FriendlyByteBuf buf) {
        return new BoltChargeSyncPacket(buf.readUUID(), buf.readInt());
    }

    /** 处理接收到的同步包，更新客户端充能数据 **/
    public static void handle(BoltChargeSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            BoltChargeClient.setChargeCount(packet.entityId(), packet.chargeCount());
        });
        context.setPacketHandled(true);
    }
}