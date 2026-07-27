package com.chinaex123.funky_effect_lib.client.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 厄运预示客户端处理类
 * <p>
 * 功能：在客户端为带有厄运预示标记的实体显示红色粒子特效
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class DoomForetoldClient {

    /** 缓存每个实体的厄运预示标记状态 **/
    private static final Map<UUID, Boolean> CLIENT_MARK_CACHE = new ConcurrentHashMap<>();

    /**
     * 设置指定实体的厄运预示标记状态
     *
     * @param uuid 实体UUID
     * @param hasMark 是否有标记
     */
    public static void setClientMark(UUID uuid, boolean hasMark) {
        if (hasMark) {
            CLIENT_MARK_CACHE.put(uuid, true);
        } else {
            CLIENT_MARK_CACHE.remove(uuid);
        }
    }

    /**
     * 检查指定实体是否有厄运预示标记
     *
     * @param uuid 实体UUID
     * @return true表示有标记，false表示无标记
     */
    public static boolean hasClientMark(UUID uuid) {
        return CLIENT_MARK_CACHE.getOrDefault(uuid, false);
    }

    /**
     * 客户端Tick事件处理
     * 每帧为带有厄运预示标记的实体生成红色粒子特效
     *
     * @param event 客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        // 检查世界是否已加载
        if (minecraft.level == null) {
            return;
        }

        // 遍历所有可渲染实体
        for (var entity : minecraft.level.entitiesForRendering()) {
            // 检查是否为LivingEntity且带有厄运预示标记
            if (entity instanceof LivingEntity living && CLIENT_MARK_CACHE.containsKey(living.getUUID())) {
                // 每帧生成2个粒子
                for (int i = 0; i < 2; i++) {
                    // 在实体包围盒内随机位置生成粒子
                    double x = living.getX() + (living.getRandom().nextDouble() - 0.5) * living.getBbWidth();
                    double y = living.getY() + living.getRandom().nextDouble() * living.getBbHeight();
                    double z = living.getZ() + (living.getRandom().nextDouble() - 0.5) * living.getBbWidth();

                    // 添加红色粒子（RGB: 1.0, 0.0, 0.0），带轻微向上速度
                    minecraft.level.addParticle(
                            new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F),
                            x, y, z,
                            0, 0.05, 0  // 速度：x=0, y=0.05（向上飘动）, z=0
                    );
                }
            }
        }
    }
}