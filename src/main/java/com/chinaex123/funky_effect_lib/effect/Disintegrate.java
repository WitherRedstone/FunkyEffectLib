package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

/** 崩解：挖掘时有概率直接将方块转化为经验球 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Disintegrate extends MobEffect {

    private static final float DISINTEGRATE_CHANCE = 0.05f; // 转换概率
    private static final float BASE_XP_MULTIPLIER = 1.5f; // 基础经验倍率

    public Disintegrate(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if (player.level().isClientSide()) {
            return;
        }

        MobEffectInstance effect = player.getEffect(FELEffects.DISINTEGRATE.get());
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
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            event.setCanceled(true);
        }
    }

    private static int calculateXP(Block block, BlockPos pos, ServerLevel level, int amplifier) {
        int baseXP = block.getExpDrop(block.defaultBlockState(), level, level.random, pos, 0, 0);

        if (baseXP == 0) {
            baseXP = 1;
        }

        float multiplier = BASE_XP_MULTIPLIER + (amplifier * 0.5f);
        return (int) Math.ceil(baseXP * multiplier);
    }
}