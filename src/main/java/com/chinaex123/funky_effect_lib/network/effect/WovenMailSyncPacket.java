package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.WovenMailClient;
import com.chinaex123.funky_effect_lib.client.renderer.effects.woven_mail.WovenMailClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** 织造铠甲的同步包 **/
public record WovenMailSyncPacket(UUID entityId, int tangleCount, long earliestExpiry, int remainingSeconds) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WovenMailSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "woven_mail_sync"));

    public static final StreamCodec<FriendlyByteBuf, WovenMailSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(WovenMailSyncPacket::encode, WovenMailSyncPacket::decode);

    private static WovenMailSyncPacket decode(FriendlyByteBuf buf) {
        return new WovenMailSyncPacket(buf.readUUID(), buf.readInt(), buf.readLong(), buf.readInt());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(tangleCount);
        buf.writeLong(earliestExpiry);
        buf.writeInt(remainingSeconds);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 处理接收到的同步包，更新客户端织造铠甲数据 **/
    public static void handle(WovenMailSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.tangleCount() <= 0) {
                WovenMailClientData.clearTangles(packet.entityId());
                WovenMailClient.clearTangles(packet.entityId());
            } else {
                WovenMailClientData.setTangleCount(packet.entityId(), packet.tangleCount(), packet.earliestExpiry());
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null && minecraft.player.getUUID().equals(packet.entityId())) {
                    WovenMailClient.setTangleCount(packet.entityId(), packet.tangleCount(), packet.remainingSeconds());
                }
            }
        });
    }
}