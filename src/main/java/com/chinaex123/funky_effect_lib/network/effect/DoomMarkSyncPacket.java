package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.DoomForetoldClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 厄运预兆的同步包 **/
public class DoomMarkSyncPacket {
    private final UUID entityUuid;
    private final boolean hasMark;

    public DoomMarkSyncPacket(UUID entityUuid, boolean hasMark) {
        this.entityUuid = entityUuid;
        this.hasMark = hasMark;
    }

    // 编码方法（实例方法）
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityUuid);
        buf.writeBoolean(hasMark);
    }

    // 解码方法（静态方法）
    public static DoomMarkSyncPacket decode(FriendlyByteBuf buf) {
        return new DoomMarkSyncPacket(buf.readUUID(), buf.readBoolean());
    }

    // 处理方法（静态方法）
    public static void handle(DoomMarkSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DoomForetoldClient.setClientMark(packet.entityUuid, packet.hasMark);
        });
        context.setPacketHandled(true);
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public boolean hasMark() {
        return hasMark;
    }
}