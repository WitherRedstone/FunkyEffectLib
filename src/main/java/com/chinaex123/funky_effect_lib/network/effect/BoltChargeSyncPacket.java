package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.BoltChargeClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 电光充能同步包
 * <p>
 * 功能：服务端 -> 客户端，同步电光充能层数
 * <p>
 * 字段说明：
 * <ul>
 *   <li>entityId - 实体UUID</li>
 *   <li>chargeCount - 当前充能层数</li>
 * </ul>
 */
public record BoltChargeSyncPacket(UUID entityId, int chargeCount) {

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(chargeCount);
    }

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    public static BoltChargeSyncPacket decode(FriendlyByteBuf buf) {
        return new BoltChargeSyncPacket(buf.readUUID(), buf.readInt());
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新电光充能层数显示
     *
     * @param packet 接收到的数据包
     * @param contextSupplier 网络上下文提供者
     */
    public static void handle(BoltChargeSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 更新客户端缓存
            BoltChargeClient.setChargeCount(packet.entityId(), packet.chargeCount());
        });
        context.setPacketHandled(true);
    }
}