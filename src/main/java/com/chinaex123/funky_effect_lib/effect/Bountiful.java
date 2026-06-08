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

/** 丰饶：破坏农作物时有概率双倍掉落，并有概率恢复饥饿值 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Bountiful extends MobEffect {

    private static final float DOUBLE_DROP_CHANCE = 0.25f; // 基础双倍掉落概率
    private static final float CHANCE_PER_LEVEL = 0.05f; // 每级增加的概率
    private static final float HUNGER_RESTORE_CHANCE = 0.15f; // 基础恢复饥饿值概率
    private static final int HUNGER_RESTORE_AMOUNT = 1; // 恢复饥饿值数量

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

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player)) {
            return;
        }

        if (event.getLevel().isClientSide()) {
            return;
        }

        var effect = player.getEffect(FELEffects.BOUNTIFUL);
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

        // 双倍掉落物品
        if (player.getRandom().nextFloat() < doubleDropChance) {
            List<ItemEntity> drops = event.getDrops();
            List<ItemEntity> originalDrops = new ArrayList<>(drops);

            for (ItemEntity itemEntity : originalDrops) {
                if (!itemEntity.getItem().isEmpty()) {
                    ItemEntity newItemEntity = new ItemEntity(
                            event.getLevel(),
                            itemEntity.getX(),
                            itemEntity.getY(),
                            itemEntity.getZ(),
                            itemEntity.getItem().copy()
                    );
                    drops.add(newItemEntity);
                }
            }
        }

        // 双倍经验
        if (player.getRandom().nextFloat() < doubleDropChance) {
            event.setDroppedExperience(event.getDroppedExperience() * 2);
        }

        // 恢复饥饿值
        if (player.getRandom().nextFloat() < hungerRestoreChance) {
            player.getFoodData().setFoodLevel(Math.min(20, player.getFoodData().getFoodLevel() + HUNGER_RESTORE_AMOUNT));
        }
    }

    private static boolean isCrop(BlockState state) {
        return state.is(BlockTags.CROPS);
    }
}