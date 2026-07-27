package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 增幅：冲刺一段时间后逐渐提高移动速度
 * <p>
 * 机制：
 * <ol>
 *   <li>玩家冲刺时，每Tick增加速度加成</li>
 *   <li>速度加成随着冲刺时间逐渐增加，直至达到上限</li>
 *   <li>基础最高速度加成为30%，每级增加10%</li>
 *   <li>停止冲刺时，速度加成立即移除</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Amplified extends MobEffect {

    private static final UUID MOVEMENT_SPEED_MODIFIER_UUID = UUID.fromString("39024c2b-55b0-451c-9d12-ade56ae6abf4");
    private static final String MOVEMENT_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("amplified_speed".getBytes()).toString();

    /** 基础每Tick增加的速度加成 **/
    private static final float BASE_SPEED_BOOST_PER_TICK = 0.002f;
    /** 基础最高速度加成 **/
    private static final float BASE_MAX_SPEED_BOOST = 0.30f;
    /** 每级增加的最高速度加成 **/
    private static final float BOOST_PER_LEVEL = 0.10f;

    /** 缓存每个玩家的冲刺计时 **/
    private static final Map<UUID, Integer> sprintTicksMap = new HashMap<>();
    /** 缓存每个玩家的当前速度加成 **/
    private static final Map<UUID, Float> currentBoostMap = new HashMap<>();

    public Amplified(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) { }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家Tick事件处理
     * 管理增幅效果的冲刺计时和速度加成
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player == null || player.level().isClientSide()) {
            return;
        }

        // 检查玩家是否拥有增幅效果
        MobEffectInstance effect = player.getEffect(FELEffects.AMPLIFIED.get());
        if (effect == null) {
            clearSpeedBoost(player);
            return;
        }

        int level = effect.getAmplifier();

        // 根据等级计算每Tick速度加成和最高速度加成
        float speedBoostPerTick = BASE_SPEED_BOOST_PER_TICK * (1 + (level * BOOST_PER_LEVEL / BASE_MAX_SPEED_BOOST));
        float maxSpeedBoost = BASE_MAX_SPEED_BOOST * (1 + level);

        UUID playerId = player.getUUID();

        // 判断玩家是否在冲刺
        if (player.isSprinting()) {
            // 冲刺中：增加速度加成计时
            int sprintTicks = sprintTicksMap.getOrDefault(playerId, 0);
            int currentBoostTicks = sprintTicks + 1;
            sprintTicksMap.put(playerId, currentBoostTicks);

            // 计算速度加成（根据等级有不同上限）
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
     *
     * @param player 玩家对象
     * @param boost 速度加成量（正数）
     */
    private static void applySpeedBoost(Player player, float boost) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            // 移除旧的修改器，避免重复叠加
            movementSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER_UUID);
            // 添加新的修改器
            movementSpeed.addTransientModifier(new AttributeModifier(
                    MOVEMENT_SPEED_MODIFIER_UUID,
                    MOVEMENT_SPEED_MODIFIER_STRING,
                    boost,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    /**
     * 清除速度加成
     *
     * @param player 玩家对象
     */
    private static void clearSpeedBoost(Player player) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER_UUID);
        }
    }

    /**
     * 玩家Tick事件处理
     * 当玩家失去增幅效果时，清理所有缓存数据
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onEffectEnd(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (player != null && !player.level().isClientSide()) {
            // 如果玩家没有增幅效果，清理所有相关数据
            if (!player.hasEffect(FELEffects.AMPLIFIED.get())) {
                sprintTicksMap.remove(player.getUUID());
                currentBoostMap.remove(player.getUUID());
                clearSpeedBoost(player);
            }
        }
    }
}