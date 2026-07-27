package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.NetworkHandler;
import com.chinaex123.funky_effect_lib.network.effect.CreepingDarknessSyncPacket;
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
 * 蔓延黑暗：每2.5秒增加1层，叠至10层时死亡
 * <p>
 * 机制：
 * <ol>
 *   <li>每2.5秒（50刻）自动增加1层</li>
 *   <li>最大层数为10层</li>
 *   <li>每层减少5%移动速度</li>
 *   <li>达到最大层数后，延迟5秒（100刻）后死亡</li>
 *   <li>支持服务端与客户端的数据同步</li>
 *   <li>效果移除或过期时自动清除所有数据</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class CreepingDarkness extends MobEffect {

    private static final UUID DARKNESS_MODIFIER_UUID = UUID.fromString("616b9f04-fd34-47b0-89b0-38f02a76975d");
    private static final String DARKNESS_MODIFIER_STRING = UUID.nameUUIDFromBytes("creeping_darkness_damage".getBytes()).toString();

    /** 层数在持久化数据中的存储键 **/
    private static final String STACK_TAG = "creeping_darkness_stack";

    /** 叠层间隔 **/
    private static final int TICKS_PER_INTERVAL = 50;
    /** 最大层数 **/
    private static final int MAX_STACK = 10;
    /** 死亡延迟时间 **/
    private static final int DEATH_DELAY_TICKS = 100;
    /** 基础移动速度减少量 **/
    private static final float BASE_SPEED_REDUCTION = -0.05f;

    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> entityTickMap = new HashMap<>();
    /** 缓存每个实体的死亡计时器 **/
    private static final Map<UUID, Integer> deathTimerMap = new HashMap<>();
    /** 缓存每个实体的上次层数 **/
    private static final Map<UUID, Integer> lastStackMap = new HashMap<>();

    public CreepingDarkness(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 清除实体的所有蔓延黑暗相关数据
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
            float reduction = BASE_SPEED_REDUCTION * newStack;
            attribute.addTransientModifier(new AttributeModifier(
                    DARKNESS_MODIFIER_UUID,
                    DARKNESS_MODIFIER_STRING,
                    reduction,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
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
            entity.removeEffect(FELEffects.CREEPING_DARKNESS.get());
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
     * 同步层数到客户端
     *
     * @param entity 目标实体
     */
    private static void syncToClient(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new CreepingDarknessSyncPacket(serverPlayer.getUUID(), getStack(serverPlayer)));
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
        if (event.getEffectInstance().getEffect() == FELEffects.CREEPING_DARKNESS.get()) {
            LivingEntity entity = event.getEntity();
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
                        new CreepingDarknessSyncPacket(player.getUUID(), stack));
                updateSpeedModifier(player, stack);
            }
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

        // 如果玩家没有蔓延黑暗效果，清除所有数据
        if (!entity.hasEffect(FELEffects.CREEPING_DARKNESS.get())) {
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
                // 清除数据并杀死实体
                clearAllData(entity);
                entity.hurt(entity.damageSources().magic(), Float.MAX_VALUE);
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
        if (event.getEffect() == FELEffects.CREEPING_DARKNESS.get()) {
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
        if (instance != null && instance.getEffect() == FELEffects.CREEPING_DARKNESS.get()) {
            clearAllData(event.getEntity());
        }
    }
}