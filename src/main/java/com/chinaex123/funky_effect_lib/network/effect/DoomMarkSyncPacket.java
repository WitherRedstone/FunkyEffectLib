package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.DoomForetoldClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 厄运预兆同步包
 * <p>
 * 功能：服务端 -> 客户端，同步厄运预兆标记状态
 * <p>
 * 字段说明：
 * <ul>
 *   <li>entityUuid - 实体UUID</li>
 *   <li>hasMark - 是否拥有厄运预兆标记</li>
 * </ul>
 */
public record DoomMarkSyncPacket(UUID entityUuid, boolean hasMark) {

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityUuid);
        buf.writeBoolean(hasMark);
    }

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    public static DoomMarkSyncPacket decode(FriendlyByteBuf buf) {
        return new DoomMarkSyncPacket(buf.readUUID(), buf.readBoolean());
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新厄运预兆标记状态
     *
     * @param packet 接收到的数据包
     * @param contextSupplier 网络上下文提供者
     */
    public static void handle(DoomMarkSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 更新客户端缓存
            DoomForetoldClient.setClientMark(packet.entityUuid(), packet.hasMark());
        });
        context.setPacketHandled(true);
    }
}