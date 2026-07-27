package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.init.FELEntityTypeTags;
import com.chinaex123.funky_effect_lib.network.NetworkHandler;
import com.chinaex123.funky_effect_lib.network.effect.PervadingDarknessSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 弥漫暗影：每2.5秒增加1层，叠至10层时死亡，通过击杀特定敌人消除1层
 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class PervadingDarkness extends MobEffect {

    private static final UUID DARKNESS_MODIFIER_UUID = UUID.fromString("5c449be8-c5d1-4b0c-bedf-94db3168ce32");
    private static final String DARKNESS_MODIFIER_STRING = UUID.nameUUIDFromBytes("pervading_darkness".getBytes()).toString();

    /** NBT 存储键：当前层数 **/
    private static final String STACK_TAG = "pervading_darkness_stack";

    /** 层数增加间隔（ticks） **/
    private static final int TICKS_PER_INTERVAL = 50;
    /** 最大层数（达到后开始死亡倒计时） **/
    private static final int MAX_STACK = 10;
    /** 死亡倒计时持续 tick 数 **/
    private static final int DEATH_DELAY_TICKS = 100;
    /** 每层降低的移动速度 **/
    private static final float SPEED_REDUCTION_PER_STACK = -0.05f;

    /** 玩家 tick 计数器：记录距离下一次叠层的 tick 数 **/
    private static final Map<UUID, Integer> entityTickMap = new HashMap<>();
    /** 死亡倒计时剩余 tick 数（仅当层数达到 MAX_STACK 时有效） **/
    private static final Map<UUID, Integer> deathTimerMap = new HashMap<>();
    /** 缓存上次同步的层数，用于减少不必要的更新 **/
    private static final Map<UUID, Integer> lastStackMap = new HashMap<>();

    public PervadingDarkness(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 清除实体的所有弥漫暗影相关数据
     *
     * @param entity 目标实体
     */
    private static void clearAllData(LivingEntity entity) {
        UUID entityId = entity.getUUID();
        CompoundTag data = entity.getPersistentData();
        data.remove(STACK_TAG);
        removeSpeedModifier(entity);
        entityTickMap.remove(entityId);
        deathTimerMap.remove(entityId);
        lastStackMap.remove(entityId);
        syncToClient(entity);
    }

    /**
     * 更新移动速度减速效果
     *
     * @param entity 目标实体
     * @param newStack 新的层数
     */
    private static void updateSpeedModifier(LivingEntity entity, int newStack) {
        AttributeInstance attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;

        UUID entityId = entity.getUUID();
        Integer lastStack = lastStackMap.get(entityId);

        // 如果层数未变化，跳过更新
        if (lastStack != null && lastStack == newStack) {
            return;
        }

        // 移除旧的修改器
        attribute.removeModifier(DARKNESS_MODIFIER_UUID);

        // 如果层数大于0，添加新的减速效果
        if (newStack > 0) {
            double reduction = SPEED_REDUCTION_PER_STACK * newStack;
            AttributeModifier modifier = new AttributeModifier(
                    DARKNESS_MODIFIER_UUID,
                    DARKNESS_MODIFIER_STRING,
                    reduction,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            attribute.addTransientModifier(modifier);
        }

        lastStackMap.put(entityId, newStack);
    }

    /**
     * 移除移动速度减速效果
     *
     * @param entity 目标实体
     */
    private static void removeSpeedModifier(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(DARKNESS_MODIFIER_UUID);
        }
    }

    /**
     * 获取实体的当前层数
     *
     * @param entity 目标实体
     * @return 当前层数
     */
    private static int getStack(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.getInt(STACK_TAG);
    }

    /**
     * 设置实体的层数
     *
     * @param entity 目标实体
     * @param stack 要设置的层数
     */
    private static void setStack(LivingEntity entity, int stack) {
        CompoundTag data = entity.getPersistentData();

        if (stack <= 0) {
            // 层数为0，清除所有数据并移除效果
            clearAllData(entity);
            entity.removeEffect(FELEffects.PERVADING_DARKNESS.get());
        } else {
            int newStack = Math.min(stack, MAX_STACK);
            data.putInt(STACK_TAG, newStack);
            updateSpeedModifier(entity, newStack);
            syncToClient(entity);
        }
    }

    /**
     * 增加层数
     *
     * @param entity 目标实体
     * @param amount 增加的层数
     */
    private static void addStack(LivingEntity entity, int amount) {
        int current = getStack(entity);
        setStack(entity, current + amount);
    }

    /**
     * 减少层数
     *
     * @param entity 目标实体
     * @param amount 减少的层数
     */
    private static void removeStack(LivingEntity entity, int amount) {
        int current = getStack(entity);
        int newStack = current - amount;
        setStack(entity, Math.max(newStack, 0));
    }

    /**
     * 同步层数到客户端
     *
     * @param entity 目标实体
     */
    private static void syncToClient(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new PervadingDarknessSyncPacket(serverPlayer.getUUID(), getStack(serverPlayer)));
        }
    }

    /**
     * 效果添加事件处理
     * 如果实体已有层数数据，先清除再重新开始
     *
     * @param event 效果添加事件
     */
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        event.getEffectInstance();
        if (event.getEffectInstance().getEffect() == FELEffects.PERVADING_DARKNESS.get()) {
            LivingEntity entity = event.getEntity();
            // 检查是否有旧的层数残留，如果有则清除
            if (getStack(entity) > 0) {
                clearAllData(entity);
            }
        }
    }

    /**
     * 玩家登录事件处理
     * 同步层数到客户端并恢复减速效果
     *
     * @param event 玩家登录事件
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            int stack = getStack(player);
            if (stack > 0) {
                NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new PervadingDarknessSyncPacket(player.getUUID(), stack));
                updateSpeedModifier(player, stack);
                if (stack >= MAX_STACK) {
                    deathTimerMap.put(player.getUUID(), 0);
                }
            }
        }
    }

    /**
     * 击杀事件处理
     * 击杀特定敌人时减少1层
     *
     * @param event 实体死亡事件
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();

        // 检查目标是否为特定类型敌人
        if (!target.getType().is(FELEntityTypeTags.PERVADING_DARKNESS_MOB)) {
            return;
        }

        // 检查击杀者是否为LivingEntity
        if (!(event.getSource().getEntity() instanceof LivingEntity killer)) {
            return;
        }

        // 检查击杀者是否拥有弥漫暗影效果
        if (!killer.hasEffect(FELEffects.PERVADING_DARKNESS.get()) || killer.level().isClientSide()) {
            return;
        }

        // 减少1层
        int currentStack = getStack(killer);
        if (currentStack > 0) {
            removeStack(killer, 1);
        }
    }

    /**
     * 玩家Tick事件处理
     * 管理层数叠加和死亡逻辑
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (entity.level().isClientSide()) {
            return;
        }

        // 如果玩家没有弥漫暗影效果，清除所有数据
        if (!entity.hasEffect(FELEffects.PERVADING_DARKNESS.get())) {
            // 没有效果时，确保清除所有数据
            if (getStack(entity) > 0) {
                clearAllData(entity);
            }
            return;
        }

        UUID entityId = entity.getUUID();
        int currentStack = getStack(entity);

        // 达到最大层数：死亡倒计时
        if (currentStack >= MAX_STACK) {
            int deathTicks = deathTimerMap.getOrDefault(entityId, 0) + 1;

            if (deathTicks >= DEATH_DELAY_TICKS) {
                // 先尝试伤害
                entity.hurt(entity.damageSources().magic(), Float.MAX_VALUE);

                // 如果还活着，强制杀死
                if (entity.isAlive()) {
                    entity.setHealth(0);
                }

                // 玩家死亡后再清除数据
                clearAllData(entity);
            } else {
                deathTimerMap.put(entityId, deathTicks);
            }
            return;
        }

        // 未达最大层数：叠加层数
        int ticks = entityTickMap.getOrDefault(entityId, 0) + 1;

        if (ticks >= TICKS_PER_INTERVAL) {
            // 达到间隔时间，增加1层
            entityTickMap.put(entityId, 0);
            addStack(entity, 1);
        } else {
            entityTickMap.put(entityId, ticks);
        }
    }

    /**
     * 效果移除事件处理
     * 效果被手动移除时清除所有数据
     *
     * @param event 效果移除事件
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.PERVADING_DARKNESS.get()) {
            clearAllData(event.getEntity());
        }
    }

    /**
     * 效果过期事件处理
     * 效果自然过期时清除所有数据
     *
     * @param event 效果过期事件
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.PERVADING_DARKNESS.get()) {
            clearAllData(event.getEntity());
        }
    }
}