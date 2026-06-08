package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/** 高兴：增加移动速度，攻击或受伤时移除 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Happy extends MobEffect {

    // 移动速度配置
    private static final UUID HAPPY_SPEED_MODIFIER_UUID = UUID.fromString("d236af5e-286e-4340-91f9-aecc1fed0a06");
    private static final String HAPPY_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("happy_speed".getBytes()).toString();

    private static final float BASE_SPEED_BONUS = 0.15f; // 基础速度加成
    private static final float EXTRA_SPEED_PER_LEVEL = 0.15f; // 每级额外速度加成

    public Happy(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            AttributeInstance instance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (instance != null) {
                instance.removeModifier(HAPPY_SPEED_MODIFIER_UUID);
                float speedBonus = BASE_SPEED_BONUS + (amplifier * EXTRA_SPEED_PER_LEVEL);
                instance.addTransientModifier(new AttributeModifier(
                        HAPPY_SPEED_MODIFIER_UUID,
                        HAPPY_SPEED_MODIFIER_STRING,
                        speedBonus,
                        AttributeModifier.Operation.MULTIPLY_BASE
                ));
            }
        }
    }

    private static void removeBonus(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null) {
            instance.removeModifier(HAPPY_SPEED_MODIFIER_UUID);
        }
    }

    /**
     * 监听受到伤害事件 - 移除高兴效果
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(FELEffects.HAPPY.get())) {
            removeBonus(entity);
            entity.removeEffect(FELEffects.HAPPY.get());
        }
    }

    /**
     * 监听攻击事件 - 移除高兴效果
     */
    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker.hasEffect(FELEffects.HAPPY.get())) {
                removeBonus(attacker);
                attacker.removeEffect(FELEffects.HAPPY.get());
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}