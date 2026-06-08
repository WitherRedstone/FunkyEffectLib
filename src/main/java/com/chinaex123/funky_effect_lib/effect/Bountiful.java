package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** 丰饶：破坏农作物时有概率双倍掉落，并有概率恢复饥饿值 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Bountiful extends MobEffect {

    private static final float DOUBLE_DROP_CHANCE = 0.25f; // 基础双倍掉落概率
    private static final float CHANCE_PER_LEVEL = 0.05f; // 每级增加的概率
    private static final float HUNGER_RESTORE_CHANCE = 0.15f; // 基础恢复饥饿值概率
    private static final int HUNGER_RESTORE_AMOUNT = 1; // 恢复饥饿值数量

    public Bountiful(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
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

        MobEffectInstance effect = player.getEffect(FELEffects.BOUNTIFUL.get());
        if (effect == null) {
            return;
        }

        BlockState state = event.getState();
        if (!isCrop(state)) {
            return;
        }

        int amplifier = effect.getAmplifier();
        float doubleDropChance = DOUBLE_DROP_CHANCE + (amplifier * CHANCE_PER_LEVEL);
        float hungerRestoreChance = HUNGER_RESTORE_CHANCE + (amplifier * CHANCE_PER_LEVEL);

        // 双倍物品掉落
        if (player.getRandom().nextFloat() < doubleDropChance) {
            ServerLevel serverLevel = (ServerLevel) player.level();
            BlockPos pos = event.getPos();

            // 获取方块原本会掉落的物品
            List<ItemStack> originalDrops = Block.getDrops(
                    state,
                    serverLevel,
                    pos,
                    serverLevel.getBlockEntity(pos),
                    player,
                    player.getMainHandItem()
            );

            // 复制一份掉落物添加到世界中
            for (ItemStack stack : originalDrops) {
                if (!stack.isEmpty()) {
                    ItemStack copyStack = stack.copy();
                    ItemEntity itemEntity = new ItemEntity(
                            serverLevel,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            copyStack
                    );
                    itemEntity.setDefaultPickUpDelay();
                    serverLevel.addFreshEntity(itemEntity);
                }
            }
        }

        // 双倍经验
        if (player.getRandom().nextFloat() < doubleDropChance) {
            event.setExpToDrop(event.getExpToDrop() * 2);
        }

        // 恢复饥饿值
        if (player.getRandom().nextFloat() < hungerRestoreChance) {
            int newFoodLevel = Math.min(20, player.getFoodData().getFoodLevel() + HUNGER_RESTORE_AMOUNT);
            player.getFoodData().setFoodLevel(newFoodLevel);
        }
    }

    private static boolean isCrop(BlockState state) {
        return state.is(BlockTags.CROPS);
    }
}