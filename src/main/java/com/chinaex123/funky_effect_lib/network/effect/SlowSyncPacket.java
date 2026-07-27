package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.SlowClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 减速层数同步包
 * <p>
 * 功能：服务端 -> 客户端，同步减速层数
 * <p>
 * 字段说明：
 * <ul>
 *   <li>entityId - 实体UUID</li>
 *   <li>stacks - 当前减速层数</li>
 * </ul>
 */
public record SlowSyncPacket(UUID entityId, int stacks) implements CustomPacketPayload {

    /** 数据包类型标识符 **/
    public static final CustomPacketPayload.Type<SlowSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "slow_sync"));

    /** 数据包流编解码器 **/
    public static final StreamCodec<FriendlyByteBuf, SlowSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(SlowSyncPacket::encode, SlowSyncPacket::decode);

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    private static SlowSyncPacket decode(FriendlyByteBuf buf) {
        return new SlowSyncPacket(buf.readUUID(), buf.readInt());
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
     * 在客户端更新减速层数显示
     *
     * @param packet 接收到的数据包
     * @param context 网络负载上下文
     */
    public static void handle(SlowSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 仅更新本地玩家的减速层数
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.player.getUUID().equals(packet.entityId())) {
                if (packet.stacks() <= 0) {
                    // 层数为0时清除缓存
                    SlowClient.clearStacks(packet.entityId());
                } else {
                    // 更新客户端缓存
                    SlowClient.setStacks(packet.entityId(), packet.stacks());
                }
            }
        });
    }
}