package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.FrostArmorClient;
import com.chinaex123.funky_effect_lib.client.renderer.effects.frost_armor.FrostArmorClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/** 冰冻铠甲的同步包 **/
public record FrostArmorSyncPacket(UUID entityId, int crystalCount, long earliestExpiry) implements CustomPacketPayload {

    public static final Type<FrostArmorSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "frost_armor_sync")
    );

    public static final StreamCodec<FriendlyByteBuf, FrostArmorSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(FrostArmorSyncPacket::encode, FrostArmorSyncPacket::decode);

    private static FrostArmorSyncPacket decode(FriendlyByteBuf buf) {
        return new FrostArmorSyncPacket(buf.readUUID(), buf.readInt(), buf.readLong());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(crystalCount);
        buf.writeLong(earliestExpiry);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FrostArmorSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.crystalCount <= 0) {
                FrostArmorClientData.clearCrystals(packet.entityId);
                FrostArmorClient.clearCrystals(packet.entityId);
            } else {
                FrostArmorClientData.setCrystalCount(packet.entityId, packet.crystalCount, packet.earliestExpiry);
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null && minecraft.player.getUUID().equals(packet.entityId)) {
                    FrostArmorClient.setCrystalCount(packet.entityId, packet.crystalCount);
                }
            }
        });
    }
}