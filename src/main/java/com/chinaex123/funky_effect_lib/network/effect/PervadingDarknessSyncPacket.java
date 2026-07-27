package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.PervadingDarknessClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 弥漫暗影同步包
 * <p>
 * 功能：服务端 -> 客户端，同步弥漫暗影层数
 * <p>
 * 字段说明：
 * <ul>
 *   <li>entityId - 实体UUID</li>
 *   <li>stack - 当前弥漫暗影层数</li>
 * </ul>
 */
public record PervadingDarknessSyncPacket(UUID entityId, int stack) {

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(stack);
    }

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    public static PervadingDarknessSyncPacket decode(FriendlyByteBuf buf) {
        return new PervadingDarknessSyncPacket(buf.readUUID(), buf.readInt());
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新弥漫暗影层数显示
     *
     * @param packet 接收到的数据包
     * @param contextSupplier 网络上下文提供者
     */
    public static void handle(PervadingDarknessSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 更新客户端缓存
            PervadingDarknessClient.setStack(packet.entityId(), packet.stack());
        });
        context.setPacketHandled(true);
    }
}