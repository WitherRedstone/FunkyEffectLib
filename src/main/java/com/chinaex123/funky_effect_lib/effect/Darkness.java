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

/**
 * 黑暗之重：每2.5秒降低移动速度，最多叠加10层
 * <p>
 * 机制：
 * <ol>
 *   <li>每2.5秒（50刻）增加1级减速</li>
 *   <li>每级减少5%移动速度</li>
 *   <li>最大减速等级为10层（累计50%减速）</li>
 *   <li>效果移除时自动清除所有减速效果</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Darkness extends MobEffect {

    private static final UUID DARKNESS_MODIFIER_UUID = UUID.fromString("9642376f-b90b-4a05-b153-92a7322c62aa");
    private static final String DARKNESS_MODIFIER_STRING = UUID.nameUUIDFromBytes("darkness_slowdown".getBytes()).toString();

    /** 每级基础移动速度减少 **/
    private static final float BASE_SPEED_REDUCTION = -0.05f;
    /** 减速等级提升间隔 **/
    private static final int TICKS_PER_INTERVAL = 50;
    /** 最大减速等级 **/
    private static final int MAX_LEVEL = 10;

    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> entityTickMap = new HashMap<>();
    /** 缓存每个实体上一次的减速等级 **/
    private static final Map<UUID, Integer> lastLevelMap = new HashMap<>();

    public Darkness(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 更新移动速度减速效果
     *
     * @param entity 目标实体
     * @param level 当前减速等级
     */
    private static void updateSpeedModifier(LivingEntity entity, int level) {
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        UUID entityId = entity.getUUID();
        Integer lastLevel = lastLevelMap.get(entityId);

        // 如果等级未变化，跳过更新
        if (lastLevel != null && lastLevel == level) {
            return;
        }

        // 移除旧的修改器
        movementSpeed.removeModifier(DARKNESS_MODIFIER_UUID);

        // 如果等级大于0，添加新的减速效果
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

    /**
     * 移除移动速度减速效果
     *
     * @param entity 目标实体
     */
    private static void removeSpeedModifier(LivingEntity entity) {
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(DARKNESS_MODIFIER_UUID);
        }
        lastLevelMap.remove(entity.getUUID());
    }

    /**
     * 玩家Tick事件处理
     * 管理减速等级的叠加
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

        MobEffectInstance darknessEffect = entity.getEffect(FELEffects.DARKNESS.get());

        // 如果效果消失，清除数据
        if (darknessEffect == null) {
            if (entityTickMap.containsKey(entity.getUUID())) {
                entityTickMap.remove(entity.getUUID());
                removeSpeedModifier(entity);
            }
            return;
        }

        UUID entityId = entity.getUUID();
        int currentLevel = darknessEffect.getAmplifier();

        // 更新速度减速效果
        updateSpeedModifier(entity, currentLevel + 1);

        // 如果已达到最大等级，不再继续升级
        if (currentLevel >= MAX_LEVEL - 1) {
            entityTickMap.remove(entityId);
            return;
        }

        // 计时器递增
        int ticks = entityTickMap.getOrDefault(entityId, 0) + 1;

        // 达到间隔时间，提升等级
        if (ticks >= TICKS_PER_INTERVAL) {
            entityTickMap.put(entityId, 0);
            // 重新应用效果，提升等级
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

        // 清理静态缓存数据
        UUID entityId = entity.getUUID();
        entityTickMap.remove(entityId);
        lastLevelMap.remove(entityId);

        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}