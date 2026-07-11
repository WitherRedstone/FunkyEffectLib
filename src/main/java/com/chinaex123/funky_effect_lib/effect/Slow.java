package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.api.SlowAPI;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.NetworkHandler;
import com.chinaex123.funky_effect_lib.network.effect.SlowSyncPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 减速：降低移动速度，叠至100层时触发冻结效果 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Slow extends MobEffect {

    private static final String SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("slow_speed".getBytes()).toString();
    private static final float SPEED_REDUCTION = -0.5f; // 每层降低50%移动速度
    private static final int STACK_INTERVAL = 30; // 每1.5秒（30tick）叠加一次
    private static final int STACKS_PER_INTERVAL = 5; // 每次叠加5层
    private static final int FREEZE_DURATION = 600; // 冻结效果持续时间（30秒）

    /** 缓存每个实体的计时器，用于控制叠加频率 **/
    private static final Map<UUID, Integer> tickMap = new HashMap<>();

    public Slow(int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_STRING,
                SPEED_REDUCTION,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    /**
     * 效果持续期间每tick执行一次
     * 检查层数是否达到最大值，达到则触发冻结
     **/
    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        int stacks = SlowAPI.getStacks(entity);
        if (stacks >= SlowAPI.MAX_STACKS) {
            triggerFreeze(entity);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /** 服务端tick事件：为拥有减速效果的实体定时叠加层数 **/
    @SubscribeEvent
    public static void onEntityTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class,
                AABB.ofSize(new net.minecraft.core.BlockPos(0, 0, 0).getCenter(), 100000, 100000, 100000))) {
            MobEffectInstance effect = entity.getEffect(FELEffects.SLOW.get());
            if (effect == null) {
                continue;
            }

            UUID entityId = entity.getUUID();
            int ticks = tickMap.getOrDefault(entityId, 0) + 1;
            tickMap.put(entityId, ticks);

            if (ticks >= STACK_INTERVAL) {
                tickMap.put(entityId, 0);
                int stacks = SlowAPI.getStacks(entity);
                if (stacks < SlowAPI.MAX_STACKS) {
                    stacks = Math.min(stacks + STACKS_PER_INTERVAL, SlowAPI.MAX_STACKS);
                    SlowAPI.setStacksInternal(entity, stacks);
                    syncStacks(entity, stacks);
                    
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                            8, 0.4, 0.4, 0.4, 0.02);

                    if (stacks >= SlowAPI.MAX_STACKS) {
                        triggerFreeze(entity);
                    }
                }
            }
        }
    }

    /** 触发冻结效果：移除减速效果并施加冻结效果 **/
    public static void triggerFreeze(LivingEntity entity) {
        SlowAPI.clearStacks(entity);
        syncStacks(entity, 0);
        entity.removeEffect(FELEffects.SLOW.get());
        entity.addEffect(new MobEffectInstance(FELEffects.FREEZE.get(), FREEZE_DURATION, 0));
    }

    /**
     * 将减速层数同步到客户端
     * @param entity 目标实体
     * @param stacks 减速层数
     **/
    public static void syncStacks(LivingEntity entity, int stacks) {
        if (entity instanceof ServerPlayer player) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new SlowSyncPacket(entity.getUUID(), stacks));
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        SlowAPI.clearStacks(entity);
        tickMap.remove(entity.getUUID());
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.SLOW.get()) {
            SlowAPI.clearStacks(event.getEntity());
            tickMap.remove(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.SLOW.get()) {
            SlowAPI.clearStacks(event.getEntity());
            tickMap.remove(event.getEntity().getUUID());
        }
    }
}