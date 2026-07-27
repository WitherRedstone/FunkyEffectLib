package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 踏浪者：可在水面行走，潜行时下潜
 * <p>
 * 机制：
 * <ol>
 *   <li>玩家可以在水面表面行走，如同在固体方块上</li>
 *   <li>潜行时允许正常下潜到水中</li>
 *   <li>检测玩家脚底下方0.4格是否有水</li>
 *   <li>调整玩家位置到水面表面，并重置垂直速度和掉落距离</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class WaveWalker extends MobEffect {

    /** 检测水面的垂直偏移量 **/
    private static final double WATER_CHECK_OFFSET = 0.4;
    /** 水面表面高度偏移 **/
    private static final double WATER_SURFACE_OFFSET = 1.0;

    public WaveWalker(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    /**
     * 玩家Tick事件处理
     * 实现水面行走效果
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        // 检查是否拥有踏浪者效果
        if (!player.hasEffect(FELEffects.WAVE_WALKER.get())) return;

        // 潜行时允许正常下潜到水中
        if (player.isCrouching()) return;

        // 如果玩家已经在水中，不干预（让玩家正常游泳）
        if (player.isInWater()) return;

        // 检测脚底下方是否有水
        BlockPos entityPos = player.blockPosition();
        BlockPos waterCheckPos = new BlockPos(
                entityPos.getX(),
                (int) Math.floor(player.getBoundingBox().minY - WATER_CHECK_OFFSET),
                entityPos.getZ()
        );

        boolean hasWaterBelow = player.level().getFluidState(waterCheckPos).is(Fluids.WATER);

        if (hasWaterBelow) {
            // 获取水面表面高度
            double waterHeight = waterCheckPos.getY() + WATER_SURFACE_OFFSET;

            // 如果玩家在水面表面上方，调整到水面表面高度
            if (player.getY() > waterHeight) {
                player.setPos(player.getX(), waterHeight, player.getZ());
            }

            // 设置垂直速度为0（防止下落）
            player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
            // 重置掉落距离
            player.fallDistance = 0;
            // 标记为在地面上（允许行走）
            player.setOnGround(true);
        }
    }
}