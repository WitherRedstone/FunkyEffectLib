package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.DoomForetoldClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 厄运预兆的同步包 **/
public record DoomMarkSyncPacket(UUID entityUuid, boolean hasMark) {
    /** 将数据编码到网络缓冲区 **/
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityUuid);
        buf.writeBoolean(hasMark);
    }

    /** 从网络缓冲区解码数据 **/
    public static DoomMarkSyncPacket decode(FriendlyByteBuf buf) {
        return new DoomMarkSyncPacket(buf.readUUID(), buf.readBoolean());
    }

    /** 处理接收到的同步包，更新客户端厄运预兆状态 **/
    public static void handle(DoomMarkSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DoomForetoldClient.setClientMark(packet.entityUuid(), packet.hasMark());
        });
        context.setPacketHandled(true);
    }
}