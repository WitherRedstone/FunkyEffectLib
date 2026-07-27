package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * 高兴：增加移动速度，攻击或受伤时移除
 * <p>
 * 机制：
 * <ol>
 *   <li>基础速度加成15%，每级增加15%</li>
 *   <li>受到伤害时效果被移除</li>
 *   <li>攻击时效果被移除</li>
 *   <li>效果被移除时自动清除速度加成</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Happy extends MobEffect {

    private static final ResourceLocation HAPPY_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "happy_speed");

    /** 基础速度加成 **/
    private static final float BASE_SPEED_BONUS = 0.15f;
    /** 每级额外速度加成 **/
    private static final float EXTRA_SPEED_PER_LEVEL = 0.15f;

    public Happy(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 仅在服务端执行
        if (!entity.level().isClientSide()) {
            var instance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (instance != null) {
                // 移除旧的修改器
                instance.removeModifier(HAPPY_SPEED_MODIFIER);
                // 计算速度加成：基础 + 等级 × 每级加成
                float speedBonus = BASE_SPEED_BONUS + (amplifier * EXTRA_SPEED_PER_LEVEL);
                // 添加新的修改器
                instance.addTransientModifier(new AttributeModifier(
                        HAPPY_SPEED_MODIFIER,
                        speedBonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 移除属性修改器
     *
     * @param entity 目标实体
     * @param attribute 属性类型
     * @param id 修改器ID
     */
    private static void removeBonus(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    /**
     * 受到伤害事件处理
     * 受到伤害时移除高兴效果
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(FELEffects.HAPPY)) {
            // 移除速度加成
            removeBonus(entity, Attributes.MOVEMENT_SPEED, HAPPY_SPEED_MODIFIER);
            // 移除效果
            entity.removeEffect(FELEffects.HAPPY);
        }
    }

    /**
     * 攻击事件处理
     * 攻击时移除高兴效果
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent.Post event) {
        // 检查攻击者是否为LivingEntity
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker.hasEffect(FELEffects.HAPPY)) {
                // 移除速度加成
                removeBonus(attacker, Attributes.MOVEMENT_SPEED, HAPPY_SPEED_MODIFIER);
                // 移除效果
                attacker.removeEffect(FELEffects.HAPPY);
            }
        }
    }
}