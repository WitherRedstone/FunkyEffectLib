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

/** 增幅：冲刺一段时间后提高移动速度 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Amplified extends MobEffect {

    private static final ResourceLocation SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "amplified_speed");

    private static final float BASE_SPEED_BOOST_PER_TICK = 0.002f; // 基础每 tick 增加的速度加成
    private static final float BASE_MAX_SPEED_BOOST = 0.30f; // 基础最高30%速度加成
    private static final float BOOST_PER_LEVEL = 0.10f; // 每级增加10%

    private static final Map<UUID, Integer> sprintTicksMap = new HashMap<>();  // 每个玩家的冲刺计时
    private static final Map<UUID, Float> currentBoostMap = new HashMap<>();  // 每个玩家的当前速度加成

    public Amplified(int color) {
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
     * 监听玩家冲刺状态
     */
    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        // 检查是否有 增幅 效果
        MobEffectInstance effect = player.getEffect(FELEffects.AMPLIFIED);
        if (effect == null) {
            clearSpeedBoost(player);
            return;
        }

        int level = effect.getAmplifier();

        // 根据等级计算每 tick 速度加成和最高速度加成
        float speedBoostPerTick = BASE_SPEED_BOOST_PER_TICK * (1 + (level * BOOST_PER_LEVEL / BASE_MAX_SPEED_BOOST));
        float maxSpeedBoost = BASE_MAX_SPEED_BOOST * (1 + level);

        UUID playerId = player.getUUID();

        // 判断是否在冲刺
        if (player.isSprinting()) {
            // 冲刺中：增加速度加成
            int sprintTicks = sprintTicksMap.getOrDefault(playerId, 0);
            int currentBoostTicks = sprintTicks + 1;
            sprintTicksMap.put(playerId, currentBoostTicks);

            // 计算速度加成 (根据等级有不同上限)
            float speedBoost = Math.min(currentBoostTicks * speedBoostPerTick, maxSpeedBoost);
            currentBoostMap.put(playerId, speedBoost);

            // 应用速度加成
            applySpeedBoost(player, speedBoost);
        } else {
            // 停止冲刺：重置计时并移除速度加成
            if (sprintTicksMap.containsKey(playerId)) {
                sprintTicksMap.remove(playerId);
                currentBoostMap.remove(playerId);
                clearSpeedBoost(player);
            }
        }
    }

    /**
     * 应用速度加成
     */
    private static void applySpeedBoost(Player player, float boost) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(SPEED_MODIFIER);
            movementSpeed.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER,
                    boost,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    /**
     * 清除速度加成
     */
    private static void clearSpeedBoost(Player player) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(SPEED_MODIFIER);
        }
    }

    /**
     * 效果结束时清理所有数据
     */
    @SubscribeEvent
    public static void onEffectEnd(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            if (!player.hasEffect(FELEffects.AMPLIFIED)) {
                sprintTicksMap.remove(player.getUUID());
                currentBoostMap.remove(player.getUUID());
                clearSpeedBoost(player);
            }
        }
    }
}