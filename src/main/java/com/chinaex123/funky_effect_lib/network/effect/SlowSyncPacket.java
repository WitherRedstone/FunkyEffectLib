package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.SlowClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 减速层数同步包 **/
public record SlowSyncPacket(UUID entityId, int stacks) {
    /** 将数据编码到网络缓冲区 **/
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(stacks);
    }

    /** 从网络缓冲区解码数据 **/
    public static SlowSyncPacket decode(FriendlyByteBuf buf) {
        return new SlowSyncPacket(buf.readUUID(), buf.readInt());
    }

    /** 处理接收到的同步包，更新客户端减速层数 **/
    public static void handle(SlowSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.player.getUUID().equals(packet.entityId())) {
                if (packet.stacks() <= 0) {
                    SlowClient.clearStacks(packet.entityId());
                } else {
                    SlowClient.setStacks(packet.entityId(), packet.stacks());
                }
            }
        });
        context.setPacketHandled(true);
    }
}