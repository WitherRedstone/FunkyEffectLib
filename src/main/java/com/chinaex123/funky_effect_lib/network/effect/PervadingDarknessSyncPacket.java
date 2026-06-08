package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.PervadingDarknessClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 弥漫暗影的同步包 **/
public class PervadingDarknessSyncPacket {
    private final UUID entityId;
    private final int stack;

    public PervadingDarknessSyncPacket(UUID entityId, int stack) {
        this.entityId = entityId;
        this.stack = stack;
    }

    // 编码方法（实例方法）
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(stack);
    }

    // 解码方法（静态方法）
    public static PervadingDarknessSyncPacket decode(FriendlyByteBuf buf) {
        return new PervadingDarknessSyncPacket(buf.readUUID(), buf.readInt());
    }

    // 处理方法（静态方法）
    public static void handle(PervadingDarknessSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            PervadingDarknessClient.setStack(packet.entityId, packet.stack);
        });
        context.setPacketHandled(true);
    }

    public UUID getEntityId() {
        return entityId;
    }

    public int getStack() {
        return stack;
    }
}