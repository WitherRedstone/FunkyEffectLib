package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.SoulburnClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
public record SoulburnSyncPacket(UUID playerUuid, boolean hasCharge) implements CustomPacketPayload {

    /** 数据包类型标识符 **/
    public static final CustomPacketPayload.Type<SoulburnSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "soulburn_sync"));

    /** 数据包流编解码器 **/
    public static final StreamCodec<FriendlyByteBuf, SoulburnSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(SoulburnSyncPacket::encode, SoulburnSyncPacket::decode);

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    private static SoulburnSyncPacket decode(FriendlyByteBuf buf) {
        return new SoulburnSyncPacket(buf.readUUID(), buf.readBoolean());
    }

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);
        buf.writeBoolean(hasCharge);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新魂燃充能状态显示
     *
     * @param packet 接收到的数据包
     * @param context 网络负载上下文
     */
    public static void handle(SoulburnSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 更新客户端缓存
            SoulburnClient.setChargeState(packet.playerUuid, packet.hasCharge);
        });
    }
}