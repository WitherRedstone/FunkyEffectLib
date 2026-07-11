package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.CreepingDarknessClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 蔓延黑暗的同步包 **/
public class CreepingDarknessSyncPacket {
    private final UUID entityId;
    private final int stack;

    public CreepingDarknessSyncPacket(UUID entityId, int stack) {
        this.entityId = entityId;
        this.stack = stack;
    }

    /** 将数据编码到网络缓冲区 **/
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(stack);
    }

    /** 从网络缓冲区解码数据 **/
    public static CreepingDarknessSyncPacket decode(FriendlyByteBuf buf) {
        return new CreepingDarknessSyncPacket(buf.readUUID(), buf.readInt());
    }

    /** 处理接收到的同步包，更新客户端蔓延黑暗叠加层数 **/
    public static void handle(CreepingDarknessSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            CreepingDarknessClient.setStack(packet.entityId, packet.stack);
        });
        context.setPacketHandled(true);
    }
}