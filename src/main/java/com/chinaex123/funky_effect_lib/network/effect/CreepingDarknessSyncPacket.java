package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.CreepingDarknessClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/** 蔓延黑暗的同步包 **/
public record CreepingDarknessSyncPacket(UUID entityId, int stack) implements CustomPacketPayload {

    public static final Type<CreepingDarknessSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "creeping_darkness_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, CreepingDarknessSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(CreepingDarknessSyncPacket::encode, CreepingDarknessSyncPacket::decode);

    private static CreepingDarknessSyncPacket decode(FriendlyByteBuf buf) {
        return new CreepingDarknessSyncPacket(buf.readUUID(), buf.readInt());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(stack);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CreepingDarknessSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CreepingDarknessClient.setStack(packet.entityId, packet.stack);
        });
    }
}