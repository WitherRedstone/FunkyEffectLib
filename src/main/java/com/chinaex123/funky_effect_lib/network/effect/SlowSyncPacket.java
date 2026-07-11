package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.SlowClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** 减速层数同步包 **/
public record SlowSyncPacket(UUID entityId, int stacks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SlowSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "slow_sync"));

    public static final StreamCodec<FriendlyByteBuf, SlowSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(SlowSyncPacket::encode, SlowSyncPacket::decode);

    private static SlowSyncPacket decode(FriendlyByteBuf buf) {
        return new SlowSyncPacket(buf.readUUID(), buf.readInt());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(stacks);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 处理接收到的同步包，更新客户端减速层数 **/
    public static void handle(SlowSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.player.getUUID().equals(packet.entityId())) {
                if (packet.stacks() <= 0) {
                    SlowClient.clearStacks(packet.entityId());
                } else {
                    SlowClient.setStacks(packet.entityId(), packet.stacks());
                }
            }
        });
    }
}