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

/**
 * 崩解：挖掘时有概率直接将方块转化为经验球
 * <p>
 * 机制：
 * <ol>
 *   <li>基础触发概率为5%</li>
 *   <li>转化时直接移除方块并生成经验球</li>
 *   <li>基础经验倍率为1.5倍，每级增加0.5倍</li>
 *   <li>取消原方块的掉落物</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Disintegrate extends MobEffect {

    /** 崩解触发概率 **/
    private static final float DISINTEGRATE_CHANCE = 0.05f;
    /** 基础经验倍率 **/
    private static final float BASE_XP_MULTIPLIER = 1.5f;

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

    /**
     * 方块破坏事件处理
     * 有概率将方块转化为经验球
     *
     * @param event 方块破坏事件
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if (player.level().isClientSide()) {
            return;
        }

        // 检查玩家是否拥有崩解效果
        var effect = player.getEffect(FELEffects.DISINTEGRATE);
        if (effect == null) {
            return;
        }

        // 判定是否触发崩解
        if (player.getRandom().nextFloat() < DISINTEGRATE_CHANCE) {
            disintegrateBlock(event.getPos(), event.getState(), (ServerLevel) player.level(), player, effect.getAmplifier(), event);
        }
    }

    /**
     * 执行方块崩解
     * 将方块转化为经验球并取消掉落
     *
     * @param pos 方块位置
     * @param state 方块状态
     * @param level 服务端世界
     * @param player 玩家
     * @param amplifier 效果等级
     * @param event 方块破坏事件
     */
    private static void disintegrateBlock(BlockPos pos, BlockState state, ServerLevel level, Player player, int amplifier, BlockEvent.BreakEvent event) {
        Block block = state.getBlock();

        // 计算经验值
        int xpAmount = calculateXP(block, pos, level, amplifier);

        if (xpAmount > 0) {
            // 在方块位置生成经验球
            Vec3 vecPos = Vec3.atCenterOf(pos);
            ExperienceOrb.award(level, vecPos, xpAmount);
            // 将方块设为空气
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            // 取消原事件（阻止掉落物）
            event.setCanceled(true);
        }
    }

    /**
     * 计算崩解获得的经验值
     *
     * @param block 方块类型
     * @param pos 方块位置
     * @param level 服务端世界
     * @param amplifier 效果等级
     * @return 经验值数量
     */
    private static int calculateXP(Block block, BlockPos pos, ServerLevel level, int amplifier) {
        // 使用钻石镐作为默认工具获取经验值
        ItemStack miningTool = Items.DIAMOND_PICKAXE.getDefaultInstance();
        int baseXP = block.getExpDrop(block.defaultBlockState(), level, pos, null, null, miningTool);

        // 如果基础经验为0，设为1
        if (baseXP == 0) {
            baseXP = 1;
        }

        // 计算经验倍率：基础 + 等级 × 0.5
        float multiplier = BASE_XP_MULTIPLIER + (amplifier * 0.5f);
        // 向上取整
        return (int) Math.ceil(baseXP * multiplier);
    }
}