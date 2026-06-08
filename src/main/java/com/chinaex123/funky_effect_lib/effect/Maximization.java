package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 巨大化：增大玩家体型 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Maximization extends MobEffect {

    private static final ResourceLocation SCALE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "maximization_scale");
    private static final ResourceLocation STEP_HEIGHT_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "maximization_step_height");
    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "maximization_attack_damage");
    private static final ResourceLocation MAX_HEALTH_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "maximization_max_health");
    private static final ResourceLocation ENTITY_INTERACTION_RANGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "maximization_entity_interaction_range");
    private static final ResourceLocation BLOCK_INTERACTION_RANGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "maximization_block_interaction_range");
    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "maximization_movement_speed");
    private static final ResourceLocation JUMP_STRENGTH_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "maximization_jump_strength");
    private static final ResourceLocation SAFE_FALL_DISTANCE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "maximization_safe_fall_distance");

    // 增益效果
    private static final double SCALE_PER_LEVEL_LOW = 0.8; // 低等级每级体型增加
    private static final double SCALE_PER_LEVEL_HIGH = 1.0; // 高等级每级体型增加
    private static final double MAX_SCALE = 15.0; // 最大体型限制
    private static final double STEP_HEIGHT_PER_LEVEL = 0.5; // 每级增加的步进高度
    private static final double INTERACTION_RANGE_PER_LEVEL = 1.0; // 每级增加的交互范围
    private static final double ATTACK_DAMAGE_BASE = 1.5; // 基础增加的攻击力
    private static final double ATTACK_DAMAGE_PER_LEVEL = 0.5;  // 每级增加的攻击力
    private static final double MAX_ATTACK_DAMAGE = 100.0; // 最大增加的攻击力上限
    private static final double MAX_HEALTH_PER_LEVEL = 10.0; // 每级增加的最大生命值
    private static final double MAX_MAX_HEALTH = 200.0; // 最大生命值上限
    private static final double SAFE_FALL_DISTANCE_PER_LEVEL = 0.5; // 每级增加的安全坠落距离

    // 减益效果
    private static final double MOVEMENT_SPEED_REDUCTION_PER_LEVEL = -0.025; // 每级移动速度减少2.5%
    private static final double MAX_MOVEMENT_SPEED_REDUCTION = -0.5; // 最大移动速度减少上限50%
    private static final double JUMP_STRENGTH_REDUCTION_PER_LEVEL = -0.005; // 每级跳跃强度减少
    private static final double MAX_JUMP_STRENGTH_REDUCTION = -0.02; // 最大跳跃强度减少上限

    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>();

    public Maximization(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide()) {
            return;
        }

        UUID entityId = entity.getUUID();
        MobEffectInstance effect = entity.getEffect(FELEffects.MAXIMIZATION);

        if (effect == null) {
            removeAllBonuses(entity);
            lastAmplifierMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        if (lastAmplifier == null || lastAmplifier != amplifier) {
            applyMaximizationEffects(entity, amplifier);
            lastAmplifierMap.put(entityId, amplifier);
        }
    }

    private static void applyMaximizationEffects(LivingEntity entity, int amplifier) {
        int level = amplifier + 1;  // 实际等级（I级=1，II级=2，以此类推）

        // 实体体型缩放：等级<4时每级+0.8，>=4时每级+1.0，最大15
        double scale;
        if (amplifier < 4) {
            scale = level * SCALE_PER_LEVEL_LOW;
        } else {
            scale = 4.0 * SCALE_PER_LEVEL_LOW + (amplifier - 3) * SCALE_PER_LEVEL_HIGH;
        }
        scale = Math.min(scale, MAX_SCALE);
        updateAttribute(entity, Attributes.SCALE, SCALE_MODIFIER, scale, AttributeModifier.Operation.ADD_VALUE);

        // 自动上台阶高度
        updateAttribute(entity, Attributes.STEP_HEIGHT, STEP_HEIGHT_MODIFIER, level * STEP_HEIGHT_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);

        // 实体交互范围
        updateAttribute(entity, Attributes.ENTITY_INTERACTION_RANGE, ENTITY_INTERACTION_RANGE_MODIFIER, level * INTERACTION_RANGE_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);

        // 方块交互范围
        updateAttribute(entity, Attributes.BLOCK_INTERACTION_RANGE, BLOCK_INTERACTION_RANGE_MODIFIER, level, AttributeModifier.Operation.ADD_VALUE);

        // 攻击伤害
        double attackDamage = Math.min(ATTACK_DAMAGE_BASE + amplifier * ATTACK_DAMAGE_PER_LEVEL, MAX_ATTACK_DAMAGE);
        updateAttribute(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER, attackDamage, AttributeModifier.Operation.ADD_VALUE);

        // 最大生命值
        double maxHealth = Math.min(level * MAX_HEALTH_PER_LEVEL, MAX_MAX_HEALTH);
        updateAttribute(entity, Attributes.MAX_HEALTH, MAX_HEALTH_MODIFIER, maxHealth, AttributeModifier.Operation.ADD_VALUE);

        // 移动速度
        double movementSpeed = Math.max(MAX_MOVEMENT_SPEED_REDUCTION, level * MOVEMENT_SPEED_REDUCTION_PER_LEVEL);
        updateAttribute(entity, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER, movementSpeed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        // 跳跃力度
        double jumpStrength = Math.max(MAX_JUMP_STRENGTH_REDUCTION, level * JUMP_STRENGTH_REDUCTION_PER_LEVEL);
        updateAttribute(entity, Attributes.JUMP_STRENGTH, JUMP_STRENGTH_MODIFIER, jumpStrength, AttributeModifier.Operation.ADD_VALUE);

        // 安全坠落距离
        updateAttribute(entity, Attributes.SAFE_FALL_DISTANCE, SAFE_FALL_DISTANCE_MODIFIER, level * SAFE_FALL_DISTANCE_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void updateAttribute(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }

    public static void removeAllBonuses(LivingEntity entity) {
        removeBonus(entity, Attributes.SCALE, SCALE_MODIFIER);
        removeBonus(entity, Attributes.STEP_HEIGHT, STEP_HEIGHT_MODIFIER);
        removeBonus(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER);
        removeBonus(entity, Attributes.MAX_HEALTH, MAX_HEALTH_MODIFIER);
        removeBonus(entity, Attributes.ENTITY_INTERACTION_RANGE, ENTITY_INTERACTION_RANGE_MODIFIER);
        removeBonus(entity, Attributes.BLOCK_INTERACTION_RANGE, BLOCK_INTERACTION_RANGE_MODIFIER);
        removeBonus(entity, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER);
        removeBonus(entity, Attributes.JUMP_STRENGTH, JUMP_STRENGTH_MODIFIER);
        removeBonus(entity, Attributes.SAFE_FALL_DISTANCE, SAFE_FALL_DISTANCE_MODIFIER);
    }

    private static void removeBonus(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }
}