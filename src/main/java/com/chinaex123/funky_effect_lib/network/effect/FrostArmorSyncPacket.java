package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.FrostArmorClient;
import com.chinaex123.funky_effect_lib.client.renderer.effects.frost_armor.FrostArmorClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 冰霜护甲同步包
 * <p>
 * 功能：服务端 -> 客户端，同步冰霜护甲状态
 * <p>
 * 字段说明：
 * <ul>
 *   <li>entityId - 实体UUID</li>
 *   <li>crystalCount - 冰晶数量</li>
 *   <li>earliestExpiry - 最早过期时间（游戏刻）</li>
 *   <li>remainingSeconds - 剩余时间（秒）</li>
 * </ul>
 */
public record FrostArmorSyncPacket(UUID entityId, int crystalCount, long earliestExpiry, int remainingSeconds) {

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(crystalCount);
        buf.writeLong(earliestExpiry);
        buf.writeInt(remainingSeconds);
    }

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    public static FrostArmorSyncPacket decode(FriendlyByteBuf buf) {
        return new FrostArmorSyncPacket(buf.readUUID(), buf.readInt(), buf.readLong(), buf.readInt());
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新冰霜护甲状态
     *
     * @param packet 接收到的数据包
     * @param contextSupplier 网络上下文提供者
     */
    public static void handle(FrostArmorSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.crystalCount() <= 0) {
                // 冰晶数量为0，清除数据
                FrostArmorClientData.clearCrystals(packet.entityId());
                FrostArmorClient.clearCrystals(packet.entityId());
            } else {
                // 更新客户端数据缓存
                FrostArmorClientData.setCrystalCount(packet.entityId(), packet.crystalCount(), packet.earliestExpiry());
                // 如果是本地玩家，更新HUD显示
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null && minecraft.player.getUUID().equals(packet.entityId())) {
                    FrostArmorClient.setCrystalCount(packet.entityId(), packet.crystalCount(), packet.remainingSeconds());
                }
            }
        });
        context.setPacketHandled(true);
    }
}