package com.chinaex123.funky_effect_lib.client.renderer.effects;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.effect.DangerSense;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 危险感知渲染类
 * <p>
 * 功能：在客户端显示周围敌对生物的发光效果
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class DangerSenseRender {

    /** 存储当前需要显示发光效果的敌对生物 **/
    private static final Set<LivingEntity> glowingEntities = new HashSet<>();

    /** 危险感知效果是否激活 **/
    private static boolean effectActive = false;

    /**
     * 客户端Tick事件处理
     * 更新危险感知的检测范围和发光实体列表
     *
     * @param event 客户端Tick事件
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // 检查玩家是否拥有危险感知效果
        boolean hasDangerSense = mc.player.hasEffect(FELEffects.DANGER_SENSE.get());

        if (hasDangerSense) {
            // 获取效果等级（Amplifier）
            int amplifier = Objects.requireNonNull(mc.player.getEffect(FELEffects.DANGER_SENSE.get())).getAmplifier();
            // 根据等级计算检测半径
            int detectionRadius = DangerSense.getDetectionRadius(amplifier);
            // 更新发光实体列表
            updateGlowingEntities(mc.level, mc.player, detectionRadius);
            effectActive = true;
        } else {
            // 效果消失时，立即清空发光实体列表
            glowingEntities.clear();
            effectActive = false;
        }
    }

    /**
     * 更新发光实体列表
     * 在指定半径内搜索敌对生物并更新列表
     *
     * @param level 世界对象
     * @param player 玩家实体
     * @param radius 检测半径
     */
    private static void updateGlowingEntities(Level level, LivingEntity player, int radius) {
        Set<LivingEntity> newGlowingEntities = new HashSet<>();

        // 创建检测区域（以玩家为中心的正方体区域）
        AABB searchArea = player.getBoundingBox().inflate(radius);

        // 搜索区域内的所有怪物类实体
        newGlowingEntities.addAll(level.getEntitiesOfClass(Mob.class, searchArea, entity -> {
            // 排除玩家自身
            if (entity == player) return false;
            // 排除已移除的实体
            if (entity.isRemoved()) return false;
            // 仅检测怪物类别（敌对生物）
            return entity.getType().getCategory() == MobCategory.MONSTER;
        }));

        // 更新发光实体列表
        glowingEntities.clear();
        glowingEntities.addAll(newGlowingEntities);
    }

    /**
     * 检查危险感知效果是否激活
     *
     * @return true表示效果激活，false表示未激活
     */
    public static boolean isEffectActive() {
        return effectActive;
    }

    /**
     * 检查指定实体是否应该发光
     *
     * @param entity 要检查的实体
     * @return true表示应该发光，false表示不应该发光
     */
    public static boolean shouldEntityGlow(LivingEntity entity) {
        return glowingEntities.contains(entity);
    }
}