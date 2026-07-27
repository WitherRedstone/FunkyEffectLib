package com.chinaex123.funky_effect_lib.network.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.effect.FrostArmorClient;
import com.chinaex123.funky_effect_lib.client.renderer.effects.frost_armor.FrostArmorClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 冰霜护甲同步包
 * <p>
 * 功能：服务端 -> 客户端，同步冰霜护甲状态
 * <p>
 * 字段说明：
 * <ul>
 *   <li>entityId - 实体UUID</li>
 *   <li>crystalCount - 冰晶数量</li>
 *   <li>earliestExpiry - 最早过期时间（游戏刻）</li>
 *   <li>remainingSeconds - 剩余时间（秒）</li>
 * </ul>
 */
public record FrostArmorSyncPacket(UUID entityId, int crystalCount, long earliestExpiry, int remainingSeconds) implements CustomPacketPayload {

    /** 数据包类型标识符 **/
    public static final CustomPacketPayload.Type<FrostArmorSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "frost_armor_sync"));

    /** 数据包流编解码器 **/
    public static final StreamCodec<FriendlyByteBuf, FrostArmorSyncPacket> STREAM_CODEC =
            StreamCodec.ofMember(FrostArmorSyncPacket::encode, FrostArmorSyncPacket::decode);

    /**
     * 从网络缓冲区解码数据
     *
     * @param buf 网络缓冲区
     * @return 解码后的数据包
     */
    private static FrostArmorSyncPacket decode(FriendlyByteBuf buf) {
        return new FrostArmorSyncPacket(buf.readUUID(), buf.readInt(), buf.readLong(), buf.readInt());
    }

    /**
     * 将数据编码到网络缓冲区
     *
     * @param buf 网络缓冲区
     */
    private void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(crystalCount);
        buf.writeLong(earliestExpiry);
        buf.writeInt(remainingSeconds);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理接收到的同步包
     * 在客户端更新冰霜护甲状态
     *
     * @param packet 接收到的数据包
     * @param context 网络负载上下文
     */
    public static void handle(FrostArmorSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.crystalCount <= 0) {
                // 冰晶数量为0，清除数据
                FrostArmorClientData.clearCrystals(packet.entityId);
                FrostArmorClient.clearCrystals(packet.entityId);
            } else {
                // 更新客户端数据缓存
                FrostArmorClientData.setCrystalCount(packet.entityId, packet.crystalCount, packet.earliestExpiry);
                // 如果是本地玩家，更新HUD显示
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null && minecraft.player.getUUID().equals(packet.entityId)) {
                    FrostArmorClient.setCrystalCount(packet.entityId, packet.crystalCount, packet.remainingSeconds);
                }
            }
        });
    }
}