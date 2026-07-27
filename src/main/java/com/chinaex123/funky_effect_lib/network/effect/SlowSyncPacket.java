package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.SlowClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 减速层数同步包
 * <p>
 * 功能：服务端 -> 客户端，同步减速层数
 * <p>
 * 字段说明：
 * <ul>
 *   <li>entityId - 实体UUID</li>
 *   <li>stacks - 当前减速层数</li>
 * </ul>
 */
public record SlowSyncPacket(UUID entityId, int stacks) {

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(stacks);
    }

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    public static SlowSyncPacket decode(FriendlyByteBuf buf) {
        return new SlowSyncPacket(buf.readUUID(), buf.readInt());
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新减速层数显示
     *
     * @param packet 接收到的数据包
     * @param contextSupplier 网络上下文提供者
     */
    public static void handle(SlowSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 仅更新本地玩家的减速层数
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.player.getUUID().equals(packet.entityId())) {
                if (packet.stacks() <= 0) {
                    // 层数为0时清除缓存
                    SlowClient.clearStacks(packet.entityId());
                } else {
                    // 更新客户端缓存
                    SlowClient.setStacks(packet.entityId(), packet.stacks());
                }
            }
        });
        context.setPacketHandled(true);
    }
}