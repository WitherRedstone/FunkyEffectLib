package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.SoulburnClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** 魂燃的同步包 **/
public record SoulburnSyncPacket(UUID playerUuid, boolean hasCharge) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SoulburnSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "soulburn_sync"));

    public static final StreamCodec<FriendlyByteBuf, SoulburnSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(SoulburnSyncPacket::encode, SoulburnSyncPacket::decode);

    private static SoulburnSyncPacket decode(FriendlyByteBuf buf) {
        return new SoulburnSyncPacket(buf.readUUID(), buf.readBoolean());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeBoolean(hasCharge);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SoulburnSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            SoulburnClient.setChargeState(packet.playerUuid, packet.hasCharge);
        });
    }
}