package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 钢铁意志：免疫虚弱和挖掘疲劳，提升击退抗性
 * <p>
 * 机制：
 * <ol>
 *   <li>基础击退抗性增加25%</li>
 *   <li>每Tick自动移除虚弱效果</li>
 *   <li>每Tick自动移除挖掘疲劳效果</li>
 *   <li>效果持续期间持续生效</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class IronWill extends MobEffect {

    private static final ResourceLocation KNOCKBACK_RESISTANCE_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "iron_will_knockback_resistance");

    /** 基础击退抗性 **/
    private static final float BASE_KNOCKBACK_RESISTANCE = 0.25f;

    public IronWill(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        // 添加击退抗性修改器
        this.addAttributeModifier(
                Attributes.KNOCKBACK_RESISTANCE,
                KNOCKBACK_RESISTANCE_MODIFIER,
                BASE_KNOCKBACK_RESISTANCE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
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
     * 自动移除虚弱和挖掘疲劳效果
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

        // 检查是否拥有钢铁意志效果
        var effect = entity.getEffect(FELEffects.IRON_WILL);
        if (effect == null) {
            return;
        }

        // 移除负面效果
        removeNegativeEffects(entity);
    }

    /**
     * 移除虚弱和挖掘疲劳效果
     *
     * @param entity 目标实体
     */
    private static void removeNegativeEffects(LivingEntity entity) {
        // 移除虚弱效果
        if (entity.hasEffect(MobEffects.WEAKNESS)) {
            entity.removeEffect(MobEffects.WEAKNESS);
        }
        // 移除挖掘疲劳效果
        if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            entity.removeEffect(MobEffects.DIG_SLOWDOWN);
        }
    }
}