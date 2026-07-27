package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.SoulburnClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 魂燃同步包
 * <p>
 * 功能：服务端 -> 客户端，同步魂燃充能状态
 * <p>
 * 字段说明：
 * <ul>
 *   <li>playerUuid - 玩家UUID</li>
 *   <li>hasCharge - 是否已充能</li>
 * </ul>
 */
public record SoulburnSyncPacket(UUID playerUuid, boolean hasCharge) {

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeBoolean(hasCharge);
    }

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    public static SoulburnSyncPacket decode(FriendlyByteBuf buf) {
        return new SoulburnSyncPacket(buf.readUUID(), buf.readBoolean());
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新魂燃充能状态显示
     *
     * @param packet 接收到的数据包
     * @param contextSupplier 网络上下文提供者
     */
    public static void handle(SoulburnSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 更新客户端缓存
            SoulburnClient.setChargeState(packet.playerUuid(), packet.hasCharge());
        });
        context.setPacketHandled(true);
    }
}