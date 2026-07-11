package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.PervadingDarknessClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 弥漫暗影的同步包 **/
public record PervadingDarknessSyncPacket(UUID entityId, int stack) {
    /** 将数据编码到网络缓冲区 **/
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(stack);
    }

    /** 从网络缓冲区解码数据 **/
    public static PervadingDarknessSyncPacket decode(FriendlyByteBuf buf) {
        return new PervadingDarknessSyncPacket(buf.readUUID(), buf.readInt());
    }

    /** 处理接收到的同步包，更新客户端漫漫暗影状态 **/
    public static void handle(PervadingDarknessSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            PervadingDarknessClient.setStack(packet.entityId(), packet.stack());
        });
        context.setPacketHandled(true);
    }
}