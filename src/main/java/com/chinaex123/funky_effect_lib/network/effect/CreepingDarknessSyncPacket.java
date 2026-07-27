package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.CreepingDarknessClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 蔓延黑暗同步包
 * <p>
 * 功能：服务端 -> 客户端，同步蔓延黑暗层数
 * <p>
 * 字段说明：
 * <ul>
 *   <li>entityId - 实体UUID</li>
 *   <li>stack - 当前蔓延黑暗层数</li>
 * </ul>
 */
public record CreepingDarknessSyncPacket(UUID entityId, int stack) {

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
    public static CreepingDarknessSyncPacket decode(FriendlyByteBuf buf) {
        return new CreepingDarknessSyncPacket(buf.readUUID(), buf.readInt());
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新蔓延黑暗层数显示
     *
     * @param packet 接收到的数据包
     * @param contextSupplier 网络上下文提供者
     */
    public static void handle(CreepingDarknessSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 更新客户端缓存
            CreepingDarknessClient.setStack(packet.entityId, packet.stack);
        });
        context.setPacketHandled(true);
    }
}