package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.api.SlowAPI;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.effect.SlowSyncPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 减速：降低移动速度，叠至100层时触发冻结效果
 * <p>
 * 机制：
 * <ol>
 *   <li>每1.5秒（30刻）叠加5层减速</li>
 *   <li>每层降低50%移动速度</li>
 *   <li>达到100层时触发冻结效果（持续30秒）</li>
 *   <li>生成雪花粒子效果</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Slow extends MobEffect {

    private static final ResourceLocation SPEED_MODIFIER_STRING = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "slow_speed");

    /** 每层降低移动速度 **/
    private static final float SPEED_REDUCTION = -0.5f;
    /** 叠层间隔 **/
    private static final int STACK_INTERVAL = 30;
    /** 每次叠加层数 **/
    private static final int STACKS_PER_INTERVAL = 5;
    /** 冻结效果持续时间 **/
    private static final int FREEZE_DURATION = 600;

    /** 缓存每个实体的计时器，用于控制叠加频率 **/
    private static final Map<UUID, Integer> tickMap = new HashMap<>();

    public Slow(int color) {
        super(MobEffectCategory.HARMFUL, color);
        // 添加移动速度修改器
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_STRING,
                SPEED_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    /**
     * 效果持续期间每Tick执行一次
     * 检查层数是否达到最大值，达到则触发冻结
     **/
    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        int stacks = SlowAPI.getStacks(entity);
        if (stacks >= SlowAPI.MAX_STACKS) {
            triggerFreeze(entity);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 服务端Tick事件处理
     * 为拥有减速效果的实体定时叠加层数
     *
     * @param event 世界Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 遍历所有实体
        for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class,
                AABB.ofSize(new net.minecraft.core.BlockPos(0, 0, 0).getCenter(), 100000, 100000, 100000))) {
            // 检查是否有减速效果
            MobEffectInstance effect = entity.getEffect(FELEffects.SLOW);
            if (effect == null) {
                continue;
            }

            UUID entityId = entity.getUUID();
            int ticks = tickMap.getOrDefault(entityId, 0) + 1;
            tickMap.put(entityId, ticks);

            // 达到叠层间隔
            if (ticks >= STACK_INTERVAL) {
                tickMap.put(entityId, 0);
                int stacks = SlowAPI.getStacks(entity);
                if (stacks < SlowAPI.MAX_STACKS) {
                    // 叠加层数
                    stacks = Math.min(stacks + STACKS_PER_INTERVAL, SlowAPI.MAX_STACKS);
                    SlowAPI.setStacksInternal(entity, stacks);
                    syncStacks(entity, stacks);

                    // 生成雪花粒子
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                            8, 0.4, 0.4, 0.4, 0.02);

                    // 达到最大层数，触发冻结
                    if (stacks >= SlowAPI.MAX_STACKS) {
                        triggerFreeze(entity);
                    }
                }
            }
        }
    }

    /**
     * 触发冻结效果：移除减速效果并施加冻结效果
     *
     * @param entity 目标实体
     */
    public static void triggerFreeze(LivingEntity entity) {
        // 清除减速层数
        SlowAPI.clearStacks(entity);
        syncStacks(entity, 0);
        // 移除减速效果
        entity.removeEffect(FELEffects.SLOW);
        // 施加冻结效果
        entity.addEffect(new MobEffectInstance(FELEffects.FREEZE, FREEZE_DURATION, 0));
    }

    /**
     * 将减速层数同步到客户端
     *
     * @param entity 目标实体
     * @param stacks 减速层数
     */
    public static void syncStacks(LivingEntity entity, int stacks) {
        if (entity instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new SlowSyncPacket(entity.getUUID(), stacks));
        }
    }

    /**
     * 实体死亡事件处理
     * 清除减速数据
     *
     * @param event 实体死亡事件
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        SlowAPI.clearStacks(entity);
        tickMap.remove(entity.getUUID());
    }

    /**
     * 效果移除事件处理
     * 效果被手动移除时清除数据
     *
     * @param event 效果移除事件
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.SLOW) {
            SlowAPI.clearStacks(event.getEntity());
            tickMap.remove(event.getEntity().getUUID());
        }
    }

    /**
     * 效果过期事件处理
     * 效果自然过期时清除数据
     *
     * @param event 效果过期事件
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.SLOW) {
            SlowAPI.clearStacks(event.getEntity());
            tickMap.remove(event.getEntity().getUUID());
        }
    }
}