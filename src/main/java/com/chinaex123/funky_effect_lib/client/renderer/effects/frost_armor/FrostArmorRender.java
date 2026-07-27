package com.chinaex123.funky_effect_lib.client.renderer.effects.frost_armor;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 冰霜护甲渲染类
 * <p>
 * 功能：在拥有冰霜护甲效果的实体周围渲染旋转的冰晶粒子
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class FrostArmorRender {

    /** 冰晶粒子颜色和大小 **/
    private static final DustParticleOptions ICE_CRYSTAL = new DustParticleOptions(
            new Vector3f(0.8f, 0.9f, 1.0f), 0.5f
    );

    /** 冰晶旋转速度 **/
    private static final float ROTATION_SPEED = 0.05f;
    /** 冰晶环绕半径 **/
    private static final float CRYSTAL_RADIUS = 1.2f;
    /** 粒子位置随机偏移范围 **/
    private static final float PARTICLE_OFFSET_RANGE = 0.1f;

    /** 缓存每个实体的当前旋转角度 **/
    private static final Map<UUID, Float> rotationAngleMap = new HashMap<>();

    /**
     * 客户端Tick事件处理
     * 在每帧结束时为拥有冰霜护甲效果的实体生成冰晶粒子
     *
     * @param event 客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // 仅在Tick结束阶段执行
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        // 获取玩家实体
        LivingEntity livingEntity = mc.player;

        // 检查玩家是否拥有冰霜护甲效果
        if (!livingEntity.hasEffect(FELEffects.FROST_ARMOR.get())) {
            UUID entityId = livingEntity.getUUID();
            rotationAngleMap.remove(entityId);
            return;
        }

        UUID entityId = livingEntity.getUUID();
        // 从客户端数据缓存获取冰晶数量
        int crystalCount = FrostArmorClientData.getCrystalCount(entityId);

        if (crystalCount <= 0) {
            rotationAngleMap.remove(entityId);
            return;
        }

        // 更新旋转角度
        float currentAngle = rotationAngleMap.getOrDefault(entityId, 0.0f);
        currentAngle += ROTATION_SPEED;
        rotationAngleMap.put(entityId, currentAngle);

        // 为每个冰晶生成粒子
        for (int i = 0; i < crystalCount; i++) {
            // 计算冰晶在圆周上的位置
            float angle = (float) (2 * Math.PI * i / crystalCount) + currentAngle;
            float x = (float) livingEntity.getX() + (float) Math.cos(angle) * CRYSTAL_RADIUS;
            float y = (float) livingEntity.getY() + (float) (livingEntity.getBbHeight() / 2) + (float) Math.sin(angle * 2) * 0.3f;
            float z = (float) livingEntity.getZ() + (float) Math.sin(angle) * CRYSTAL_RADIUS;

            // 添加随机偏移，使粒子看起来更自然
            float offsetX = (livingEntity.getRandom().nextFloat() - 0.5f) * PARTICLE_OFFSET_RANGE;
            float offsetY = (livingEntity.getRandom().nextFloat() - 0.5f) * PARTICLE_OFFSET_RANGE;
            float offsetZ = (livingEntity.getRandom().nextFloat() - 0.5f) * PARTICLE_OFFSET_RANGE;

            // 在客户端添加冰晶粒子
            mc.level.addParticle(ICE_CRYSTAL,
                    x + offsetX, y + offsetY, z + offsetZ,
                    0, 0, 0  // 速度为零，粒子静止
            );
        }
    }
}