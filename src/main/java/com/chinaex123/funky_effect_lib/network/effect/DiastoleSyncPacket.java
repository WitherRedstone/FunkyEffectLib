package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.client.effect.DiastoleClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 舒张效果同步数据包
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
 *
 * @param playerUuid 目标玩家的UUID
 * @param stacks 当前叠层数
 * @param cooldownEnd 冷却结束时间（游戏刻）
 */
public record DiastoleSyncPacket(UUID playerUuid, int stacks, long cooldownEnd) {

    /**
     * 编码数据包
     * <p>
     * 将数据包内容写入网络缓冲区，用于发送到客户端
     *
     * @param buf 网络缓冲区
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerUuid);   // 写入玩家UUID
        buf.writeInt(stacks);        // 写入叠层数
        buf.writeLong(cooldownEnd);  // 写入冷却结束时间
    }

    /**
     * 解码数据包
     * <p>
     * 从网络缓冲区读取数据并构造数据包实例，用于客户端接收
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包实例
     */
    public static DiastoleSyncPacket decode(FriendlyByteBuf buf) {
        return new DiastoleSyncPacket(buf.readUUID(), buf.readInt(), buf.readLong());
    }

    /**
     * 处理数据包
     * <p>
     * 在客户端主线程中执行数据包逻辑，
     * 将同步数据传递给 {@link DiastoleClient} 更新状态缓存
     *
     * @param packet 数据包实例
     * @param contextSupplier 网络事件上下文提供者
     */
    public static void handle(DiastoleSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        // 将处理逻辑排入主线程队列，避免线程安全问题
        context.enqueueWork(() -> {
            DiastoleClient.setDiastoleState(packet.playerUuid(), packet.stacks(), packet.cooldownEnd());
        });
        // 标记数据包已处理
        context.setPacketHandled(true);
    }
}