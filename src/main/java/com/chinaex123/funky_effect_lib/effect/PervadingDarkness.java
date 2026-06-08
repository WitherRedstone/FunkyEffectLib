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

/** 弥漫暗影：每2.5秒增加1层，叠至10层时死亡，通过击杀特定敌人消除1层 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class PervadingDarkness extends MobEffect {

    private static final UUID DARKNESS_MODIFIER_UUID = UUID.fromString("5c449be8-c5d1-4b0c-bedf-94db3168ce32");
    private static final String DARKNESS_MODIFIER_STRING = UUID.nameUUIDFromBytes("pervading_darkness".getBytes()).toString();

    private static final String STACK_TAG = "pervading_darkness_stack";
    private static final int TICKS_PER_INTERVAL = 50; // 叠层的速度
    private static final int MAX_STACK = 10; // 最大层数
    private static final int DEATH_DELAY_TICKS = 100; // 死亡延迟时间
    private static final float SPEED_REDUCTION_PER_STACK = -0.05f; // 每层移动速度减少量（负数）

    private static final Map<UUID, Integer> entityTickMap = new HashMap<>();
    private static final Map<UUID, Integer> deathTimerMap = new HashMap<>();
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

    // ==================== 清除所有数据 ====================
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

    // ==================== 减速效果管理 ====================
    private static void updateSpeedModifier(LivingEntity entity, int newStack) {
        AttributeInstance attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) return;

        UUID entityId = entity.getUUID();
        Integer lastStack = lastStackMap.get(entityId);

        if (lastStack != null && lastStack == newStack) {
            return;
        }

        attribute.removeModifier(DARKNESS_MODIFIER_UUID);

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

    private static void removeSpeedModifier(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(DARKNESS_MODIFIER_UUID);
        }
    }

    // ==================== 层数操作 ====================
    private static int getStack(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.getInt(STACK_TAG);
    }

    private static void setStack(LivingEntity entity, int stack) {
        CompoundTag data = entity.getPersistentData();

        if (stack <= 0) {
            clearAllData(entity);
            entity.removeEffect(FELEffects.PERVADING_DARKNESS.get());
        } else {
            int newStack = Math.min(stack, MAX_STACK);
            data.putInt(STACK_TAG, newStack);
            updateSpeedModifier(entity, newStack);
            syncToClient(entity);
        }
    }

    private static void addStack(LivingEntity entity, int amount) {
        int current = getStack(entity);
        setStack(entity, current + amount);
    }

    private static void removeStack(LivingEntity entity, int amount) {
        int current = getStack(entity);
        int newStack = current - amount;
        setStack(entity, Math.max(newStack, 0));
    }

    // ==================== 客户端同步 ====================
    private static void syncToClient(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new PervadingDarknessSyncPacket(serverPlayer.getUUID(), getStack(serverPlayer)));
        }
    }

    // ==================== 效果添加时重置层数 ====================
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance() != null &&
                event.getEffectInstance().getEffect() == FELEffects.PERVADING_DARKNESS.get()) {
            LivingEntity entity = event.getEntity();
            // 检查是否有旧的层数残留，如果有则清除
            if (getStack(entity) > 0) {
                clearAllData(entity);
            }
        }
    }

    // ==================== 玩家登录时同步 ====================
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

    // ==================== 击杀特定敌人消除1层 ====================
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();

        if (!target.getType().is(FELEntityTypeTags.PERVADING_DARKNESS_MOB)) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof LivingEntity killer)) {
            return;
        }

        if (!killer.hasEffect(FELEffects.PERVADING_DARKNESS.get()) || killer.level().isClientSide()) {
            return;
        }

        int currentStack = getStack(killer);
        if (currentStack > 0) {
            removeStack(killer, 1);
        }
    }

    // ==================== 叠层逻辑 ====================
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (entity.level().isClientSide()) {
            return;
        }

        if (!entity.hasEffect(FELEffects.PERVADING_DARKNESS.get())) {
            // 没有效果时，确保清除所有数据
            if (getStack(entity) > 0) {
                clearAllData(entity);
            }
            return;
        }

        UUID entityId = entity.getUUID();
        int currentStack = getStack(entity);

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

        int ticks = entityTickMap.getOrDefault(entityId, 0) + 1;

        if (ticks >= TICKS_PER_INTERVAL) {
            entityTickMap.put(entityId, 0);
            addStack(entity, 1);
        } else {
            entityTickMap.put(entityId, ticks);
        }
    }

    // ==================== 效果移除时清除数据 ====================
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.PERVADING_DARKNESS.get()) {
            clearAllData(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.PERVADING_DARKNESS.get()) {
            clearAllData(event.getEntity());
        }
    }
}