package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 自然祝福：周围植物生长加速
 * <p>
 * 机制：
 * <ol>
 *   <li>基础间隔100刻（5秒），每级减少10刻</li>
 *   <li>影响半径2格</li>
 *   <li>对范围内可催熟的植物施加骨粉效果</li>
 *   <li>生成幸福村民粒子效果</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class NaturesBlessing extends MobEffect {

    /** 基础间隔 **/
    private static final int BASE_INTERVAL_TICKS = 100;
    /** 每级减少的间隔 **/
    private static final int INTERVAL_REDUCTION_PER_LEVEL = 10;
    /** 影响范围半径 **/
    private static final int EFFECT_RADIUS = 2;

    /** 缓存每个玩家的Tick计数器 **/
    private static final Map<UUID, Integer> playerTickMap = new HashMap<>();

    public NaturesBlessing(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    /**
     * 玩家Tick事件处理
     * 管理植物生长加速效果
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        // 检查是否拥有自然祝福效果
        var effect = entity.getEffect(FELEffects.NATURES_BLESSING);
        if (effect == null) {
            playerTickMap.remove(entity.getUUID());
            return;
        }

        int amplifier = effect.getAmplifier();
        // 计算间隔：基础 - (等级+1) × 每级减少量
        int intervalTicks = BASE_INTERVAL_TICKS - (amplifier + 1) * INTERVAL_REDUCTION_PER_LEVEL;
        // 确保最小间隔为10刻
        intervalTicks = Math.max(intervalTicks, 10);

        UUID playerId = entity.getUUID();
        int ticks = playerTickMap.getOrDefault(playerId, 0) + 1;

        // 未达到间隔，继续计时
        if (ticks < intervalTicks) {
            playerTickMap.put(playerId, ticks);
            return;
        }

        // 重置计时器
        playerTickMap.put(playerId, 0);

        ServerLevel level = (ServerLevel) entity.level();
        // 获取玩家周围的影响区域
        BlockPos centerPos = entity.blockPosition();
        AABB area = new AABB(centerPos).inflate(EFFECT_RADIUS);

        // 遍历区域内的所有方块
        BlockPos.betweenClosedStream(area).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            // 检查是否为可催熟的植物
            if (state.getBlock() instanceof BonemealableBlock bonemealable) {
                // 检查是否可以催熟
                if (bonemealable.isValidBonemealTarget(level, pos, state)) {
                    // 执行催熟
                    bonemealable.performBonemeal(level, level.random, pos, state);

                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            5, 0.3, 0.3, 0.3, 0.02);
                }
            }
        });
    }
}