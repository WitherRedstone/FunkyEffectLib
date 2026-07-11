package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.SoulburnClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 魂燃的同步包 **/
public record SoulburnSyncPacket(UUID playerUuid, boolean hasCharge) {
    /** 将数据编码到网络缓冲区 **/
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeBoolean(hasCharge);
    }

    /** 从网络缓冲区解码数据 **/
    public static SoulburnSyncPacket decode(FriendlyByteBuf buf) {
        return new SoulburnSyncPacket(buf.readUUID(), buf.readBoolean());
    }

    /** 处理接收到的同步包，更新客户端魂燃状态 **/
    public static void handle(SoulburnSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            SoulburnClient.setChargeState(packet.playerUuid(), packet.hasCharge());
        });
        context.setPacketHandled(true);
    }
}