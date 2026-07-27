package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.DoomForetoldClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
public record DoomMarkSyncPacket(UUID entityUuid, boolean hasMark) implements CustomPacketPayload {

    /** 数据包类型标识符 **/
    public static final CustomPacketPayload.Type<DoomMarkSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "doom_mark_sync"));

    /** 数据包流编解码器 **/
    public static final StreamCodec<FriendlyByteBuf, DoomMarkSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(DoomMarkSyncPacket::encode, DoomMarkSyncPacket::decode);

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    private static DoomMarkSyncPacket decode(FriendlyByteBuf buf) {
        return new DoomMarkSyncPacket(buf.readUUID(), buf.readBoolean());
    }

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityUuid);
        buf.writeBoolean(hasMark);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新厄运预兆标记状态
     *
     * @param packet 接收到的数据包
     * @param context 网络负载上下文
     */
    public static void handle(DoomMarkSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 更新客户端缓存
            DoomForetoldClient.setClientMark(packet.entityUuid, packet.hasMark);
        });
    }
}