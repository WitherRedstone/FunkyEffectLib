package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.PervadingDarknessClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/** 弥漫暗影的同步包 **/
public record PervadingDarknessSyncPacket(UUID entityId, int stack) implements CustomPacketPayload {

    public static final Type<PervadingDarknessSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "pervading_darkness_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, PervadingDarknessSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(PervadingDarknessSyncPacket::encode, PervadingDarknessSyncPacket::decode);

    private static PervadingDarknessSyncPacket decode(FriendlyByteBuf buf) {
        return new PervadingDarknessSyncPacket(buf.readUUID(), buf.readInt());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(stack);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PervadingDarknessSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            PervadingDarknessClient.setStack(packet.entityId, packet.stack);
        });
    }
}