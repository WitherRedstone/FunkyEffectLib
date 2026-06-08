package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.SoulburnClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 魂燃的同步包 **/
public class SoulburnSyncPacket {
    private final UUID playerUuid;
    private final boolean hasCharge;

    public SoulburnSyncPacket(UUID playerUuid, boolean hasCharge) {
        this.playerUuid = playerUuid;
        this.hasCharge = hasCharge;
    }

    // 编码方法（实例方法）
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeBoolean(hasCharge);
    }

    // 解码方法（静态方法）
    public static SoulburnSyncPacket decode(FriendlyByteBuf buf) {
        return new SoulburnSyncPacket(buf.readUUID(), buf.readBoolean());
    }

    // 处理方法（静态方法）
    public static void handle(SoulburnSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            SoulburnClient.setChargeState(packet.playerUuid, packet.hasCharge);
        });
        context.setPacketHandled(true);
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public boolean hasCharge() {
        return hasCharge;
    }
}