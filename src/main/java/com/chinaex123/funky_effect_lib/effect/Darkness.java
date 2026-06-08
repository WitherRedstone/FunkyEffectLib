package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 黑暗之重：每2.5秒降低移动速度 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Darkness extends MobEffect {

    private static final UUID DARKNESS_MODIFIER_UUID = UUID.fromString("9642376f-b90b-4a05-b153-92a7322c62aa");
    private static final String DARKNESS_MODIFIER_STRING = UUID.nameUUIDFromBytes("darkness_slowdown".getBytes()).toString();

    private static final float BASE_SPEED_REDUCTION = -0.05f; // 每级基础移动速度减少
    private static final int TICKS_PER_INTERVAL = 50; // 降低速度的时间间隔tick
    private static final int MAX_LEVEL = 10; // 最大等级

    private static final Map<UUID, Integer> entityTickMap = new HashMap<>(); // 记录每个实体的当前等级
    private static final Map<UUID, Integer> lastLevelMap = new HashMap<>(); // 记录每个实体上一次的等级

    public Darkness(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    // ==================== 减速效果管理 ====================
    private static void updateSpeedModifier(LivingEntity entity, int level) {
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        UUID entityId = entity.getUUID();
        Integer lastLevel = lastLevelMap.get(entityId);

        if (lastLevel != null && lastLevel == level) {
            return;
        }

        movementSpeed.removeModifier(DARKNESS_MODIFIER_UUID);

        if (level > 0) {
            float reduction = BASE_SPEED_REDUCTION * level;
            movementSpeed.addTransientModifier(new AttributeModifier(
                    DARKNESS_MODIFIER_UUID,
                    DARKNESS_MODIFIER_STRING,
                    reduction,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }

        lastLevelMap.put(entityId, level);
    }

    private static void removeSpeedModifier(LivingEntity entity) {
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(DARKNESS_MODIFIER_UUID);
        }
        lastLevelMap.remove(entity.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (entity.level().isClientSide()) {
            return;
        }

        MobEffectInstance darknessEffect = entity.getEffect(FELEffects.DARKNESS.get());

        if (darknessEffect == null) {
            // 效果消失时清除数据
            if (entityTickMap.containsKey(entity.getUUID())) {
                entityTickMap.remove(entity.getUUID());
                removeSpeedModifier(entity);
            }
            return;
        }

        UUID entityId = entity.getUUID();
        int currentLevel = darknessEffect.getAmplifier();

        // 更新速度减益（根据当前等级）
        updateSpeedModifier(entity, currentLevel + 1);

        // 如果已达到最大等级，不再继续升级
        if (currentLevel >= MAX_LEVEL - 1) {
            entityTickMap.remove(entityId);
            return;
        }

        int ticks = entityTickMap.getOrDefault(entityId, 0) + 1;

        if (ticks >= TICKS_PER_INTERVAL) {
            entityTickMap.put(entityId, 0);

            // 升级效果
            entity.addEffect(new MobEffectInstance(
                    darknessEffect.getEffect(),
                    darknessEffect.getDuration(),
                    currentLevel + 1,
                    darknessEffect.isAmbient(),
                    darknessEffect.isVisible(),
                    darknessEffect.showIcon()
            ));
        } else {
            entityTickMap.put(entityId, ticks);
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, AttributeMap attributes, int amplifier) {
        // 移除速度修饰符
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(DARKNESS_MODIFIER_UUID);
        }

        // 清理静态 Map 中的数据
        UUID entityId = entity.getUUID();
        entityTickMap.remove(entityId);
        lastLevelMap.remove(entityId);

        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}