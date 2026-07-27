package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
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

/**
 * 焕光：大幅提高攻击力
 * <p>
 * 机制：
 * <ol>
 *   <li>基础攻击力增加35%</li>
 *   <li>每级额外增加10%攻击力</li>
 *   <li>效果等级变化时自动更新攻击力加成</li>
 *   <li>效果消失时移除攻击力加成</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Radiant extends MobEffect {

    private static final ResourceLocation ATTACK_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "radiant_attack");

    /** 基础攻击力增加 **/
    private static final float BASE_ATTACK_DAMAGE_INCREASE = 0.35f;
    /** 每级额外增加攻击力 **/
    private static final float ADDITIONAL_INCREASE_PER_LEVEL = 0.10f;

    /** 缓存每个实体上次应用的等级 **/
    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>();

    public Radiant(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 实体Tick事件处理
     * 管理焕光效果的攻击力加成
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        // 仅处理LivingEntity
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide()) {
            return;
        }

        UUID entityId = entity.getUUID();
        MobEffectInstance effect = entity.getEffect(FELEffects.RADIANT);

        // 如果效果消失，清除攻击力加成
        if (effect == null) {
            clearAttackDamageIncrease(entity);
            lastAmplifierMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        // 如果等级发生变化，重新计算攻击力加成
        if (lastAmplifier == null || lastAmplifier != amplifier) {
            // 计算攻击力加成：基础 + 等级 × 每级加成
            float increase = BASE_ATTACK_DAMAGE_INCREASE + (amplifier * ADDITIONAL_INCREASE_PER_LEVEL);
            applyAttackDamageIncrease(entity, increase);
            lastAmplifierMap.put(entityId, amplifier);
        }
    }

    /**
     * 应用攻击力加成
     *
     * @param entity 目标实体
     * @param increase 攻击力增加量
     */
    private static void applyAttackDamageIncrease(LivingEntity entity, float increase) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            // 移除旧的修改器，避免重复叠加
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER);
            // 添加新的修改器
            attackDamage.addTransientModifier(new AttributeModifier(
                    ATTACK_DAMAGE_MODIFIER,
                    increase,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
    }

    /**
     * 清除攻击力加成
     *
     * @param entity 目标实体
     */
    private static void clearAttackDamageIncrease(LivingEntity entity) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER);
        }
    }
}