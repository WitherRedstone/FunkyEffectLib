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

/** 踏焰者：可在岩浆上行走，潜行时下潜到岩浆下 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class FlameWalker extends MobEffect {

    public FlameWalker(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        // 检查是否有踏焰者效果
        if (!player.hasEffect(FELEffects.FLAME_WALKER.get())) return;

        // 如果玩家潜行，允许正常下潜
        if (player.isCrouching()) return;

        // 检查玩家是否不在岩浆中
        if (player.isInLava()) return;

        // 检测脚底下方0.4格的位置是否有岩浆
        BlockPos entityPos = player.blockPosition();
        BlockPos lavaCheckPos = new BlockPos(
                entityPos.getX(),
                (int) Math.floor(player.getBoundingBox().minY - 0.4),
                entityPos.getZ()
        );

        boolean hasLavaBelow = player.level().getFluidState(lavaCheckPos).is(Fluids.LAVA);

        if (hasLavaBelow) {
            // 获取岩浆面高度
            double lavaHeight = lavaCheckPos.getY() + 1.0;

            // 如果玩家在岩浆面上方，调整到岩浆面高度
            if (player.getY() > lavaHeight) {
                player.setPos(player.getX(), lavaHeight, player.getZ());
            }

            // 设置垂直速度为0
            player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
            // 重置掉落距离
            player.fallDistance = 0;
            // 标记为在地面上
            player.setOnGround(true);
        }
    }
}