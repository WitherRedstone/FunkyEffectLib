package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 血肉重生：消耗饥饿值恢复生命值，饥饿值低于3格时停止
 * <p>
 * 机制：
 * <ol>
 *   <li>每2.5秒（50刻）触发一次治疗效果</li>
 *   <li>基础消耗1点饥饿值，每级增加1点</li>
 *   <li>基础恢复2点生命值，每级增加2点</li>
 *   <li>饥饿值低于6点（3格）时停止恢复</li>
 *   <li>生命值满时停止恢复</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class FleshRegrowth extends MobEffect {

    /** 最低饥饿值阈值 **/
    private static final int MIN_HUNGER_LEVEL = 6;
    /** 基础每次治疗消耗的饥饿值 **/
    private static final int BASE_HUNGER_COST = 1;
    /** 每级额外消耗的饥饿值 **/
    private static final int HUNGER_COST_PER_LEVEL = 1;
    /** 基础每次治疗恢复的生命值 **/
    private static final float BASE_HEAL_AMOUNT = 2.0f;
    /** 每级额外恢复的生命值 **/
    private static final float HEAL_AMOUNT_PER_LEVEL = 2.0f;
    /** 治疗间隔 **/
    private static final int TICK_INTERVAL = 50;

    /** 缓存每个玩家的Tick计数器 **/
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();

    public FleshRegrowth(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家Tick事件处理
     * 管理治疗效果触发
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide()) {
            return;
        }

        UUID entityId = player.getUUID();
        MobEffectInstance effect = player.getEffect(FELEffects.FLESH_REGROWTH.get());

        // 如果效果消失，清除计时器
        if (effect == null) {
            tickCounterMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();

        // 根据等级计算消耗和恢复量
        int hungerCost = BASE_HUNGER_COST + (amplifier * HUNGER_COST_PER_LEVEL);
        float healAmount = BASE_HEAL_AMOUNT + (amplifier * HEAL_AMOUNT_PER_LEVEL);

        // 计时器递增
        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        // 达到间隔时间，触发治疗
        if (tickCounter >= TICK_INTERVAL) {
            tickCounter = 0;
            tickCounterMap.put(entityId, tickCounter);

            FoodData foodData = player.getFoodData();
            int currentHunger = foodData.getFoodLevel();

            // 检查条件：饥饿值大于阈值、饥饿值足够消耗、生命值未满
            if (currentHunger > MIN_HUNGER_LEVEL && currentHunger >= hungerCost && player.getHealth() < player.getMaxHealth()) {
                // 消耗饥饿值
                foodData.setFoodLevel(currentHunger - hungerCost);
                // 恢复生命值
                player.heal(healAmount);
            }
        } else {
            tickCounterMap.put(entityId, tickCounter);
        }
    }
}