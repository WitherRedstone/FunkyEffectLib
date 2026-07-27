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
 * 踏焰者：可在岩浆上行走，潜行时下潜到岩浆下
 * <p>
 * 机制：
 * <ol>
 *   <li>玩家可以在岩浆表面行走，如同在固体方块上</li>
 *   <li>潜行时允许正常下潜到岩浆中</li>
 *   <li>检测玩家脚底下方0.4格是否有岩浆</li>
 *   <li>调整玩家位置到岩浆表面，并重置垂直速度和掉落距离</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class FlameWalker extends MobEffect {

    /** 检测岩浆的垂直偏移量 **/
    private static final double LAVA_CHECK_OFFSET = 0.4;
    /** 岩浆表面高度偏移 **/
    private static final double LAVA_SURFACE_OFFSET = 1.0;

    public FlameWalker(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    /**
     * 玩家Tick事件处理
     * 实现岩浆行走效果
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        // 检查是否拥有踏焰者效果
        if (!player.hasEffect(FELEffects.FLAME_WALKER.get())) return;

        // 潜行时允许正常下潜到岩浆中
        if (player.isCrouching()) return;

        // 如果玩家已经在岩浆中，不干预（让玩家正常游泳）
        if (player.isInLava()) return;

        // 检测脚底下方是否有岩浆
        BlockPos entityPos = player.blockPosition();
        BlockPos lavaCheckPos = new BlockPos(
                entityPos.getX(),
                (int) Math.floor(player.getBoundingBox().minY - LAVA_CHECK_OFFSET),
                entityPos.getZ()
        );

        boolean hasLavaBelow = player.level().getFluidState(lavaCheckPos).is(Fluids.LAVA);

        if (hasLavaBelow) {
            // 获取岩浆表面高度
            double lavaHeight = lavaCheckPos.getY() + LAVA_SURFACE_OFFSET;

            // 如果玩家在岩浆表面上方，调整到岩浆表面高度
            if (player.getY() > lavaHeight) {
                player.setPos(player.getX(), lavaHeight, player.getZ());
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