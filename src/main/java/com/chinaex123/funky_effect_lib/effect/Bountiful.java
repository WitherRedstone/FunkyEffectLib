package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 丰饶：破坏农作物时有概率双倍掉落，并有概率恢复饥饿值
 * <p>
 * 机制：
 * <ol>
 *   <li>基础双倍掉落概率25%，每级增加5%</li>
 *   <li>基础恢复饥饿值概率15%，每级增加5%</li>
 *   <li>双倍掉落时同时双倍经验值</li>
 *   <li>恢复饥饿值每次恢复1点，不超过20点</li>
 *   <li>仅对农作物有效（使用CROPS标签）</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Bountiful extends MobEffect {

    /** 基础双倍掉落概率 **/
    private static final float DOUBLE_DROP_CHANCE = 0.25f;
    /** 每级增加的双倍掉落概率 **/
    private static final float CHANCE_PER_LEVEL = 0.05f;
    /** 基础恢复饥饿值概率 **/
    private static final float HUNGER_RESTORE_CHANCE = 0.15f;
    /** 恢复饥饿值数量 **/
    private static final int HUNGER_RESTORE_AMOUNT = 1;

    public Bountiful(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
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
     * 方块掉落事件处理
     * 当方块被破坏并产生掉落物时，触发双倍掉落和恢复饥饿值效果
     *
     * @param event 方块掉落事件
     */
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        // 获取破坏者
        if (!(event.getBreaker() instanceof Player player)) {
            return;
        }

        // 仅在服务端执行
        if (event.getLevel().isClientSide()) {
            return;
        }

        // 检查玩家是否拥有丰饶效果
        var effect = player.getEffect(FELEffects.BOUNTIFUL);
        if (effect == null) {
            return;
        }

        BlockState state = event.getState();
        // 仅处理农作物
        if (!isCrop(state)) {
            return;
        }

        int amplifier = effect.getAmplifier();
        // 计算概率：基础 + 等级 × 每级加成
        float doubleDropChance = DOUBLE_DROP_CHANCE + (amplifier * CHANCE_PER_LEVEL);
        float hungerRestoreChance = HUNGER_RESTORE_CHANCE + (amplifier * CHANCE_PER_LEVEL);

        // 双倍物品掉落
        if (player.getRandom().nextFloat() < doubleDropChance) {
            List<ItemEntity> drops = event.getDrops();
            // 复制当前掉落列表，避免并发修改
            List<ItemEntity> originalDrops = new ArrayList<>(drops);

            for (ItemEntity itemEntity : originalDrops) {
                if (!itemEntity.getItem().isEmpty()) {
                    // 创建新的掉落物副本
                    ItemEntity newItemEntity = new ItemEntity(
                            event.getLevel(),
                            itemEntity.getX(),
                            itemEntity.getY(),
                            itemEntity.getZ(),
                            itemEntity.getItem().copy()
                    );
                    // 添加到掉落列表
                    drops.add(newItemEntity);
                }
            }
        }

        // =双倍经验
        if (player.getRandom().nextFloat() < doubleDropChance) {
            event.setDroppedExperience(event.getDroppedExperience() * 2);
        }

        // 恢复饥饿值
        if (player.getRandom().nextFloat() < hungerRestoreChance) {
            int newFoodLevel = Math.min(20, player.getFoodData().getFoodLevel() + HUNGER_RESTORE_AMOUNT);
            player.getFoodData().setFoodLevel(newFoodLevel);
        }
    }

    /**
     * 判断方块是否为农作物
     * 使用Minecraft的CROPS标签
     *
     * @param state 方块状态
     * @return true表示是农作物
     */
    private static boolean isCrop(BlockState state) {
        return state.is(BlockTags.CROPS);
    }
}