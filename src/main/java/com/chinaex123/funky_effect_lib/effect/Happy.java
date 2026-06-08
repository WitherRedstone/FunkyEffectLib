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

/** 高兴：增加移动速度，攻击或受伤时移除 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Happy extends MobEffect {

    // 移动速度配置
    private static final ResourceLocation HAPPY_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "happy_speed");

    private static final float BASE_SPEED_BONUS = 0.15f; // 基础速度加成
    private static final float EXTRA_SPEED_PER_LEVEL = 0.15f; // 每级额外速度加成

    public Happy(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            var instance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (instance != null) {
                instance.removeModifier(HAPPY_SPEED_MODIFIER);
                float speedBonus = BASE_SPEED_BONUS + (amplifier * EXTRA_SPEED_PER_LEVEL);
                instance.addTransientModifier(new AttributeModifier(
                        HAPPY_SPEED_MODIFIER,
                        speedBonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
        }
        return true;
    }

    private static void removeBonus(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    /**
     * 监听受到伤害事件 - 移除高兴效果
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(FELEffects.HAPPY)) {
            removeBonus(entity, Attributes.MOVEMENT_SPEED, HAPPY_SPEED_MODIFIER);
            entity.removeEffect(FELEffects.HAPPY);
        }
    }

    /**
     * 监听攻击事件 - 移除高兴效果
     */
    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker.hasEffect(FELEffects.HAPPY)) {
                removeBonus(attacker, Attributes.MOVEMENT_SPEED, HAPPY_SPEED_MODIFIER);
                attacker.removeEffect(FELEffects.HAPPY);
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}