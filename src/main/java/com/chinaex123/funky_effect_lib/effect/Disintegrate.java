package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

/** 崩解：挖掘时有概率直接将方块转化为经验球 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Disintegrate extends MobEffect {

    private static final float DISINTEGRATE_CHANCE = 0.05f; // 转换概率
    private static final float BASE_XP_MULTIPLIER = 1.5f; // 基础经验倍率

    public Disintegrate(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if (player.level().isClientSide()) {
            return;
        }

        var effect = player.getEffect(FELEffects.DISINTEGRATE);
        if (effect == null) {
            return;
        }

        if (player.getRandom().nextFloat() < DISINTEGRATE_CHANCE) {
            disintegrateBlock(event.getPos(), event.getState(), (ServerLevel) player.level(), player, effect.getAmplifier(), event);
        }
    }

    private static void disintegrateBlock(BlockPos pos, BlockState state, ServerLevel level, Player player, int amplifier, BlockEvent.BreakEvent event) {
        Block block = state.getBlock();

        int xpAmount = calculateXP(block, pos, level, amplifier);

        if (xpAmount > 0) {
            Vec3 vecPos = Vec3.atCenterOf(pos);
            ExperienceOrb.award(level, vecPos, xpAmount);
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            event.setCanceled(true);
        }
    }

    private static int calculateXP(Block block, BlockPos pos, ServerLevel level, int amplifier) {
        ItemStack miningTool = Items.DIAMOND_PICKAXE.getDefaultInstance();
        int baseXP = block.getExpDrop(block.defaultBlockState(), level, pos, null, null, miningTool);

        if (baseXP == 0) {
            baseXP = 1;
        }

        float multiplier = BASE_XP_MULTIPLIER + (amplifier * 0.5f);
        return (int) Math.ceil(baseXP * multiplier);
    }
}