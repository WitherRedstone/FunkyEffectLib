package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.CreepingDarknessClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

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
public record CreepingDarknessSyncPacket(UUID entityId, int stack) implements CustomPacketPayload {

    /** 数据包类型标识符 **/
    public static final Type<CreepingDarknessSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "creeping_darkness_sync")
    );

    /** 数据包流编解码器 **/
    public static final StreamCodec<FriendlyByteBuf, CreepingDarknessSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(CreepingDarknessSyncPacket::encode, CreepingDarknessSyncPacket::decode);

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    private static CreepingDarknessSyncPacket decode(FriendlyByteBuf buf) {
        return new CreepingDarknessSyncPacket(buf.readUUID(), buf.readInt());
    }

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(stack);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新蔓延黑暗层数显示
     *
     * @param packet 接收到的数据包
     * @param context 网络负载上下文
     */
    public static void handle(CreepingDarknessSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 更新客户端缓存
            CreepingDarknessClient.setStack(packet.entityId, packet.stack);
        });
    }
}