package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.WovenMailClient;
import com.chinaex123.funky_effect_lib.client.renderer.effects.woven_mail.WovenMailClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 织造铠甲的同步包 **/
public class WovenMailSyncPacket {
    private final UUID entityId;
    private final int tangleCount;
    private final long earliestExpiry;

    public WovenMailSyncPacket(UUID entityId, int tangleCount, long earliestExpiry) {
        this.entityId = entityId;
        this.tangleCount = tangleCount;
        this.earliestExpiry = earliestExpiry;
    }

    // 编码方法（实例方法）
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(tangleCount);
        buf.writeLong(earliestExpiry);
    }

    // 解码方法（静态方法）
    public static WovenMailSyncPacket decode(FriendlyByteBuf buf) {
        return new WovenMailSyncPacket(buf.readUUID(), buf.readInt(), buf.readLong());
    }

    // 处理方法（静态方法）
    public static void handle(WovenMailSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.tangleCount <= 0) {
                WovenMailClientData.clearTangles(packet.entityId);
                WovenMailClient.clearTangles(packet.entityId);
            } else {
                WovenMailClientData.setTangleCount(packet.entityId, packet.tangleCount, packet.earliestExpiry);
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null && minecraft.player.getUUID().equals(packet.entityId)) {
                    WovenMailClient.setTangleCount(packet.entityId, packet.tangleCount);
                }
            }
        });
        context.setPacketHandled(true);
    }

    public UUID getEntityId() {
        return entityId;
    }

    public int getTangleCount() {
        return tangleCount;
    }

    public long getEarliestExpiry() {
        return earliestExpiry;
    }
}