package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.FrostArmorClient;
import com.chinaex123.funky_effect_lib.client.renderer.effects.frost_armor.FrostArmorClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** 冰冻铠甲的同步包 **/
public record FrostArmorSyncPacket(UUID entityId, int crystalCount, long earliestExpiry) {
    /** 将数据编码到网络缓冲区 **/
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(crystalCount);
        buf.writeLong(earliestExpiry);
    }

    /** 从网络缓冲区解码数据 **/
    public static FrostArmorSyncPacket decode(FriendlyByteBuf buf) {
        return new FrostArmorSyncPacket(buf.readUUID(), buf.readInt(), buf.readLong());
    }

    /** 处理接收到的同步包，更新客户端冰冻铠甲状态 **/
    public static void handle(FrostArmorSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.crystalCount() <= 0) {
                FrostArmorClientData.clearCrystals(packet.entityId());
                FrostArmorClient.clearCrystals(packet.entityId());
            } else {
                FrostArmorClientData.setCrystalCount(packet.entityId(), packet.crystalCount(), packet.earliestExpiry());
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null && minecraft.player.getUUID().equals(packet.entityId())) {
                    FrostArmorClient.setCrystalCount(packet.entityId(), packet.crystalCount());
                }
            }
        });
        context.setPacketHandled(true);
    }
}