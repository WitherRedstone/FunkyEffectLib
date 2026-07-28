package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELAttributes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 硬化：按百分比减免所有来源的伤害
 * <p>
 * 机制：
 * <ol>
 *   <li>每级减免5%伤害</li>
 *   <li>最大减免50%伤害</li>
 *   <li>减免所有来源的伤害（近战、远程、魔法等）</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Hardened extends MobEffect {

    private static final ResourceLocation DAMAGE_REDUCTION_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "damage_reduction");

    /** 每级伤害减免 **/
    private static final double REDUCTION_PER_LEVEL = 0.05;
    /** 最大伤害减免 **/
    private static final double MAX_REDUCTION = 0.50;

    /** 上一次应用的等级 */
    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>();

    public Hardened(int color) {
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
     * 管理硬化效果的属性修改
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide()) {
            return;
        }

        MobEffectInstance effect = entity.getEffect(FELEffects.HARDENED);

        if (effect == null) {
            // 效果已消失，清除所有修改
            clearAttributes(entity);
            lastAmplifierMap.remove(entity.getUUID());
            return;
        }

        int amplifier = effect.getAmplifier();
        UUID entityId = entity.getUUID();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        // 如果等级发生变化，重新计算属性修改
        if (lastAmplifier == null || lastAmplifier != amplifier) {
            // 计算减伤百分比：等级 + 1 × 每级减免，最高50%
            double damageReduction = Math.min((amplifier + 1) * REDUCTION_PER_LEVEL, MAX_REDUCTION);
            applyAttributes(entity, damageReduction);
            lastAmplifierMap.put(entityId, amplifier);
        }
    }

    /**
     * 应用伤害减免属性修改
     *
     * @param entity 目标实体
     * @param damageReduction 伤害减免值
     */
    private static void applyAttributes(LivingEntity entity, double damageReduction) {
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION);
        if (attribute != null) {
            // 移除旧的修改器，避免重复叠加
            attribute.removeModifier(DAMAGE_REDUCTION_MODIFIER);
            // 添加新的修改器
            attribute.addTransientModifier(new AttributeModifier(
                    DAMAGE_REDUCTION_MODIFIER,
                    damageReduction,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    /**
     * 清除伤害减免属性修改
     *
     * @param entity 目标实体
     */
    private static void clearAttributes(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION);
        if (attribute != null) {
            attribute.removeModifier(DAMAGE_REDUCTION_MODIFIER);
        }
    }
}