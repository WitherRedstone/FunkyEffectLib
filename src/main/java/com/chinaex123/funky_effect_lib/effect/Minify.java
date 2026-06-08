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

/** 迷你化 - 缩小玩家体型 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Minify extends MobEffect {

    private static final ResourceLocation SCALE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "minify_scale");
    private static final ResourceLocation STEP_HEIGHT_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "minify_step_height");
    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "minify_attack_damage");
    private static final ResourceLocation MAX_HEALTH_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "minify_max_health");
    private static final ResourceLocation ENTITY_INTERACTION_RANGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "minify_entity_interaction_range");
    private static final ResourceLocation BLOCK_INTERACTION_RANGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "minify_block_interaction_range");
    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "minify_movement_speed");
    private static final ResourceLocation JUMP_STRENGTH_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "minify_jump_strength");
    private static final ResourceLocation SAFE_FALL_DISTANCE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "minify_safe_fall_distance");

    // 减益效果
    private static final double SCALE_REDUCTION_PER_LEVEL = -0.15; // 每级体型缩小的比例
    private static final double MIN_SCALE = 0.01; // 最小体型限制
    private static final double STEP_HEIGHT_REDUCTION_PER_LEVEL = -0.05; // 每级减少的上台阶高度
    private static final double INTERACTION_RANGE_REDUCTION_PER_LEVEL = -0.25; // 每级减少的交互范围
    private static final double MAX_INTERACTION_RANGE_REDUCTION = -0.75; // 最大减少的交互范围上限
    private static final double ATTACK_DAMAGE_REDUCTION_PER_LEVEL = -0.5; // 每级减少的攻击力
    private static final double MAX_ATTACK_DAMAGE_REDUCTION = -0.5; // 最大减少的攻击力上限
    private static final double MAX_HEALTH_REDUCTION_PER_LEVEL = -2.0; // 每级减少的最大生命值
    private static final double MIN_MAX_HEALTH = 2.0; // 最小最大生命值

    // 增益效果
    private static final double MOVEMENT_SPEED_BONUS_PER_LEVEL = 0.05; // 每级增加的移动速度
    private static final double JUMP_STRENGTH_BONUS_PER_LEVEL = 0.05; // 每级增加的跳跃强度
    private static final double SAFE_FALL_DISTANCE_BONUS_PER_LEVEL = 0.5; // 每级增加的最小安全坠落距离

    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>();

    public Minify(int color) {
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
        MobEffectInstance effect = entity.getEffect(FELEffects.MINIFY);

        if (effect == null) {
            removeAllBonuses(entity);
            lastAmplifierMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        if (lastAmplifier == null || lastAmplifier != amplifier) {
            applyMinifyEffects(entity, amplifier);
            lastAmplifierMap.put(entityId, amplifier);
        }
    }

    private static void applyMinifyEffects(LivingEntity entity, int amplifier) {
        int level = amplifier + 1;

        // 实体体型缩放
        double scale = Math.max(MIN_SCALE, 1.0 + level * SCALE_REDUCTION_PER_LEVEL);
        updateAttribute(entity, Attributes.SCALE, SCALE_MODIFIER, scale - 1.0, AttributeModifier.Operation.ADD_VALUE);

        // 自动上台阶高度
        updateAttribute(entity, Attributes.STEP_HEIGHT, STEP_HEIGHT_MODIFIER, level * STEP_HEIGHT_REDUCTION_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);

        // 攻击伤害
        double attackDamageReduction = Math.max(MAX_ATTACK_DAMAGE_REDUCTION, level * ATTACK_DAMAGE_REDUCTION_PER_LEVEL);
        updateAttribute(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER, attackDamageReduction, AttributeModifier.Operation.ADD_VALUE);

        // 最大生命值
        double maxHealthReduction = Math.max(-(entity.getAttributeBaseValue(Attributes.MAX_HEALTH) - MIN_MAX_HEALTH), level * MAX_HEALTH_REDUCTION_PER_LEVEL);
        updateAttribute(entity, Attributes.MAX_HEALTH, MAX_HEALTH_MODIFIER, maxHealthReduction, AttributeModifier.Operation.ADD_VALUE);

        // 实体交互范围
        double interactionReduction = Math.max(MAX_INTERACTION_RANGE_REDUCTION, level * INTERACTION_RANGE_REDUCTION_PER_LEVEL);
        updateAttribute(entity, Attributes.ENTITY_INTERACTION_RANGE, ENTITY_INTERACTION_RANGE_MODIFIER, interactionReduction, AttributeModifier.Operation.ADD_VALUE);

        // 方块交互范围
        updateAttribute(entity, Attributes.BLOCK_INTERACTION_RANGE, BLOCK_INTERACTION_RANGE_MODIFIER, interactionReduction, AttributeModifier.Operation.ADD_VALUE);

        // 移动速度
        updateAttribute(entity, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_MODIFIER, level * MOVEMENT_SPEED_BONUS_PER_LEVEL, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        // 跳跃力度
        updateAttribute(entity, Attributes.JUMP_STRENGTH, JUMP_STRENGTH_MODIFIER, level * JUMP_STRENGTH_BONUS_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);

        // 安全坠落距离
        updateAttribute(entity, Attributes.SAFE_FALL_DISTANCE, SAFE_FALL_DISTANCE_MODIFIER, level * SAFE_FALL_DISTANCE_BONUS_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);
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