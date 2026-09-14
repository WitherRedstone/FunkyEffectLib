package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.DiastoleClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 舒张（Diastole）效果同步数据包
 * <p>
 * 用于将服务端的舒张效果状态同步到客户端，供HUD显示使用。
 * 该数据包在叠层数变化、进入冷却以及效果结束时发送。
 * <p>
 * 数据内容：
 * <ul>
 *   <li>playerUuid：目标玩家的UUID，用于客户端校验</li>
 *   <li>stacks：当前叠层数（0-4）</li>
 *   <li>cooldownEnd：冷却结束时间（游戏刻），无冷却时为0</li>
 * </ul>
 * <p>
 * 客户端收到后会调用 {@link DiastoleClient#setDiastoleState(UUID, int, long)}
 * 更新本地缓存，触发HUD刷新。
 * <p>
 * 本数据包基于 NeoForge 的 {@link CustomPacketPayload} 体系实现，
 * 通过 {@link #STREAM_CODEC} 完成编解码，通过 {@link #handle} 处理接收逻辑。
 *
 * @param playerUuid 目标玩家的UUID
 * @param stacks 当前叠层数
 * @param cooldownEnd 冷却结束时间（游戏刻）
 */
public record DiastoleSyncPacket(UUID playerUuid, int stacks, long cooldownEnd) implements CustomPacketPayload {

    /**
     * 数据包类型标识
     * <p>
     * 使用模组ID和名称"diastole_sync"构造唯一的资源位置，
     * 用于在网络系统中注册和识别该数据包
     */
    public static final CustomPacketPayload.Type<DiastoleSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "diastole_sync"));

    /**
     * 流编解码器
     * <p>
     * 将 {@link #encode(FriendlyByteBuf)} 和 {@link #decode(FriendlyByteBuf)}
     * 组合为一个完整的编解码器，用于数据包的序列化和反序列化
     */
    public static final StreamCodec<FriendlyByteBuf, DiastoleSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(DiastoleSyncPacket::encode, DiastoleSyncPacket::decode);

    /**
     * 解码数据包
     * <p>
     * 从网络缓冲区读取数据并构造数据包实例
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包实例
     */
    private static DiastoleSyncPacket decode(FriendlyByteBuf buf) {
        return new DiastoleSyncPacket(buf.readUUID(), buf.readInt(), buf.readLong());
    }

    /**
     * 编码数据包
     * <p>
     * 将数据包内容写入网络缓冲区，用于发送
     *
     * @param buf 网络缓冲区
     */
    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);   // 写入玩家UUID
        buf.writeInt(stacks);        // 写入叠层数
        buf.writeLong(cooldownEnd);  // 写入冷却结束时间
    }

    /**
     * 获取数据包类型
     *
     * @return 该数据包的类型标识
     */
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理数据包
     * <p>
     * 在客户端主线程中执行数据包逻辑，
     * 将同步数据传递给 {@link DiastoleClient} 更新状态缓存
     *
     * @param packet 数据包实例
     * @param context 网络负载上下文
     */
    public static void handle(DiastoleSyncPacket packet, IPayloadContext context) {
        // 将处理逻辑排入主线程队列，避免线程安全问题
        context.enqueueWork(() -> {
            DiastoleClient.setDiastoleState(packet.playerUuid(), packet.stacks(), packet.cooldownEnd());
        });
    }
}