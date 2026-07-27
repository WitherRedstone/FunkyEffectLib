package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.api.event.Scorch.ScorchAPI;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.NetworkHandler;
import com.chinaex123.funky_effect_lib.network.effect.ScorchSyncPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 灼烧：每秒叠加一层并造成伤害，每满10层触发一次点燃
 * <p>
 * 机制：
 * <ol>
 *   <li>每秒（20刻）叠加1层灼烧层数</li>
 *   <li>每10层触发一次点燃效果</li>
 *   <li>最高100层，达到后每5秒触发一次最高级点燃</li>
 *   <li>持续使目标燃烧</li>
 *   <li>使用ScorchAPI管理层数和点燃效果</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Scorch extends MobEffect {

    /** 叠层间隔 **/
    private static final int TICKS_PER_STACK = 20;
    /** 灼烧最高层数 **/
    private static final int MAX_SCORCH_STACKS = 100;
    /** 100层后触发间隔 **/
    private static final int POST_MAX_TICKS = 100;
    /** 保持燃烧状态的时间 **/
    private static final int FIRE_TICKS = 60;

    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();
    /** 缓存达到100层后每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> postMaxTickMap = new HashMap<>();
    /** 默认点燃配置 **/
    private static final ScorchAPI.IgniteConfig DEFAULT_CONFIG = ScorchAPI.IgniteConfig.createDefault();

    public Scorch(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 仅在服务端执行
        if (!entity.level().isClientSide()) {
            UUID entityId = entity.getUUID();

            // 让生物持续着火（每Tick刷新燃烧时间）
            if (entity.getRemainingFireTicks() < 40) {
                entity.setRemainingFireTicks(FIRE_TICKS);
            }

            // 获取当前层数
            int currentStacks = ScorchAPI.getScorchStacks(entity);

            // 如果已经达到最高层数，进入特殊模式
            if (currentStacks >= MAX_SCORCH_STACKS) {
                handlePostMaxMode(entity, entityId);
                return;
            }

            // 未达到最高层数，继续叠加逻辑
            int ticks = tickCounterMap.getOrDefault(entityId, 0) + 1;

            if (ticks >= TICKS_PER_STACK) {
                tickCounterMap.put(entityId, 0);

                // 新层数 = 当前层数 + 1
                int newStacks = currentStacks + 1;

                // 使用API添加层数，会自动触发点燃
                ScorchAPI.addScorchStacks(entity, 1, DEFAULT_CONFIG);
                syncToClient(entity, newStacks);

                // 检查是否刚刚达到100层
                if (newStacks == MAX_SCORCH_STACKS) {
                    onReachMaxStacks(entity);
                }
            } else {
                tickCounterMap.put(entityId, ticks);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 处理100层后的模式
     * 每5秒触发一次最高级点燃
     *
     * @param entity 目标实体
     * @param entityId 实体UUID
     */
    private static void handlePostMaxMode(LivingEntity entity, UUID entityId) {
        int postTicks = postMaxTickMap.getOrDefault(entityId, 0) + 1;

        if (postTicks >= POST_MAX_TICKS) {
            postMaxTickMap.put(entityId, 0);
            // 触发最高级点燃
            ScorchAPI.triggerIgnite(entity, ScorchAPI.MAX_IGNITE_LEVEL - 1, DEFAULT_CONFIG);
        } else {
            postMaxTickMap.put(entityId, postTicks);
        }
    }

    /**
     * 达到100层时的特效
     * 爆炸粒子和火焰粒子效果，播放凋灵生成音效
     *
     * @param entity 目标实体
     */
    private static void onReachMaxStacks(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    3, 0.5, 0.5, 0.5, 0.2);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    50, 1.0, 1.0, 1.0, 0.2);
        }

        // 播放凋灵生成音效
        entity.level().playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.WITHER_SPAWN,
                SoundSource.HOSTILE,
                1.0f, 1.0f
        );
    }

    /**
     * 同步灼烧层数到客户端
     *
     * @param entity 目标实体
     * @param stacks 当前灼烧层数
     */
    public static void syncToClient(LivingEntity entity, int stacks) {
        if (entity instanceof ServerPlayer serverPlayer) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new ScorchSyncPacket(entity.getUUID(), stacks));
        }
    }

    /**
     * 玩家Tick事件处理
     * 清理缓存数据
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        // 如果玩家没有灼烧效果，清除所有数据
        if (!entity.hasEffect(FELEffects.SCORCH.get())) {
            tickCounterMap.remove(entity.getUUID());
            postMaxTickMap.remove(entity.getUUID());
            ScorchAPI.clearScorchStacks(entity);
            syncToClient(entity, 0);
        }
    }
}