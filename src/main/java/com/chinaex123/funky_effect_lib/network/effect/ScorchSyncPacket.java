package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.ScorchClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 灼烧同步包
 * <p>
 * 功能：服务端 -> 客户端，同步灼烧层数
 * <p>
 * 字段说明：
 * <ul>
 *   <li>entityId - 实体UUID</li>
 *   <li>stacks - 当前灼烧层数</li>
 * </ul>
 */
public record ScorchSyncPacket(UUID entityId, int stacks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ScorchSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "scorch_sync"));

    public static final StreamCodec<FriendlyByteBuf, ScorchSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(ScorchSyncPacket::encode, ScorchSyncPacket::decode);

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    private static ScorchSyncPacket decode(FriendlyByteBuf buf) {
        return new ScorchSyncPacket(buf.readUUID(), buf.readInt());
    }

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(stacks);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新灼烧层数显示
     *
     * @param packet 接收到的数据包
     * @param context 网络上下文
     */
    public static void handle(ScorchSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 更新客户端缓存
            ScorchClient.setScorchStacks(packet.entityId(), packet.stacks());
        });
    }
}