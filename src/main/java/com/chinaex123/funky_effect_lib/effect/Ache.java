package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 疼痛：大幅降低移动速度并每秒减少饥饿值
 * <p>
 * 机制：
 * <ol>
 *   <li>基础移动速度减少35%，每级额外减少5%</li>
 *   <li>每2.5秒（50刻）减少1点饥饿值</li>
 *   <li>与镇静（Calm）效果互斥，拥有镇静时自动移除疼痛</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Ache extends MobEffect {

    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "ache_speed");

    /** 基础移动速度减少 */
    private static final float BASE_SPEED_REDUCTION = -0.35f;
    /** 每级额外移动速度减少 */
    private static final float ADDITIONAL_REDUCTION_PER_LEVEL = -0.05f;
    /** 减少饥饿值间隔 */
    private static final int HUNGER_COST_INTERVAL = 50;

    /** 上一次应用的等级 */
    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>();
    /** 当前应用的等级计数器 */
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();

    public Ache(int color) {
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
     * 玩家Tick事件处理
     * 管理疼痛效果的属性修改和饥饿值消耗
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // 如果玩家有 镇静 效果，跳过 疼痛 的所有逻辑并清除已有的 疼痛
        if (player.hasEffect(FELEffects.CALM) && player.hasEffect(FELEffects.ACHE)) {
            player.removeEffect(FELEffects.ACHE);
            clearAttributes(player);
            lastAmplifierMap.remove(player.getUUID());
            tickCounterMap.remove(player.getUUID());
            return;
        }

        MobEffectInstance effect = player.getEffect(FELEffects.ACHE);

        if (effect == null) {
            // 效果已消失，清除所有修改
            clearAttributes(player);
            lastAmplifierMap.remove(player.getUUID());
            tickCounterMap.remove(player.getUUID());
            return;
        }

        int amplifier = effect.getAmplifier();
        UUID entityId = player.getUUID();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        // 如果等级发生变化，重新计算属性修改
        if (lastAmplifier == null || lastAmplifier != amplifier) {
            float speedReduction = BASE_SPEED_REDUCTION + (amplifier * ADDITIONAL_REDUCTION_PER_LEVEL);
            applyAttributes(player, speedReduction);
            lastAmplifierMap.put(entityId, amplifier);
        }

        // 饥饿值消耗计时
        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        if (tickCounter >= HUNGER_COST_INTERVAL) {
            player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - 1));
            tickCounter = 0;
        }

        tickCounterMap.put(entityId, tickCounter);
    }

    /**
     * 应用移动速度修改
     *
     * @param player 玩家对象
     * @param boost 速度修改量（负值表示减速）
     */
    private static void applyAttributes(Player player, float boost) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            // 移除旧的修改器，避免重复叠加
            movementSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER);
            // 添加新的修改器
            movementSpeed.addTransientModifier(new AttributeModifier(
                    MOVEMENT_SPEED_MODIFIER,
                    boost,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    /**
     * 清除移动速度修改
     *
     * @param player 玩家对象
     */
    private static void clearAttributes(Player player) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER);
        }
    }
}