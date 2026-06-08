package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.DoomForetoldClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** 厄运预兆的同步包 **/
public record DoomMarkSyncPacket(UUID entityUuid, boolean hasMark) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DoomMarkSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "doom_mark_sync"));

    public static final StreamCodec<FriendlyByteBuf, DoomMarkSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(DoomMarkSyncPacket::encode, DoomMarkSyncPacket::decode);

    private static DoomMarkSyncPacket decode(FriendlyByteBuf buf) {
        return new DoomMarkSyncPacket(buf.readUUID(), buf.readBoolean());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityUuid);
        buf.writeBoolean(hasMark);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DoomMarkSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            DoomForetoldClient.setClientMark(packet.entityUuid, packet.hasMark);
        });
    }
}