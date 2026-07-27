package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.BoltChargeClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
public record BoltChargeSyncPacket(UUID entityId, int chargeCount) implements CustomPacketPayload {

    /** 数据包类型标识符 **/
    public static final CustomPacketPayload.Type<BoltChargeSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "bolt_charge_sync"));

    /** 数据包流编解码器 **/
    public static final StreamCodec<FriendlyByteBuf, BoltChargeSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(BoltChargeSyncPacket::encode, BoltChargeSyncPacket::decode);

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    private static BoltChargeSyncPacket decode(FriendlyByteBuf buf) {
        return new BoltChargeSyncPacket(buf.readUUID(), buf.readInt());
    }

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(chargeCount);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新电光充能层数显示
     *
     * @param packet 接收到的数据包
     * @param context 网络负载上下文
     */
    public static void handle(BoltChargeSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 更新客户端缓存
            BoltChargeClient.setChargeCount(packet.entityId(), packet.chargeCount());
        });
    }
}