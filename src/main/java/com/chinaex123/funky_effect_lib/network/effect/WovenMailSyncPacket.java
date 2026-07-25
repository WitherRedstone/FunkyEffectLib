package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.WovenMailClient;
import com.chinaex123.funky_effect_lib.client.renderer.effects.woven_mail.WovenMailClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 织造铠甲的同步包 **/
public record WovenMailSyncPacket(UUID entityId, int tangleCount, long earliestExpiry, int remainingSeconds) {
    /** 将数据编码到网络缓冲区 **/
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(tangleCount);
        buf.writeLong(earliestExpiry);
        buf.writeInt(remainingSeconds);
    }

    /** 从网络缓冲区解码数据 **/
    public static WovenMailSyncPacket decode(FriendlyByteBuf buf) {
        return new WovenMailSyncPacket(buf.readUUID(), buf.readInt(), buf.readLong(), buf.readInt());
    }

    /** 处理接收到的同步包，更新客户端织造铠甲数据 **/
    public static void handle(WovenMailSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.tangleCount() <= 0) {
                WovenMailClientData.clearTangles(packet.entityId());
                WovenMailClient.clearTangles(packet.entityId());
            } else {
                WovenMailClientData.setTangleCount(packet.entityId(), packet.tangleCount(), packet.earliestExpiry());
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null && minecraft.player.getUUID().equals(packet.entityId())) {
                    WovenMailClient.setTangleCount(packet.entityId(), packet.tangleCount(), packet.remainingSeconds());
                }
            }
        });
        context.setPacketHandled(true);
    }
}