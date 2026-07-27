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
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Happy extends MobEffect {

    private static final UUID HAPPY_SPEED_MODIFIER_UUID = UUID.fromString("d236af5e-286e-4340-91f9-aecc1fed0a06");
    private static final String HAPPY_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("happy_speed".getBytes()).toString();

    /** 基础速度加成 **/
    private static final float BASE_SPEED_BONUS = 0.15f;
    /** 每级额外速度加成 **/
    private static final float EXTRA_SPEED_PER_LEVEL = 0.15f;

    public Happy(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 仅在服务端执行
        if (!entity.level().isClientSide()) {
            AttributeInstance instance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (instance != null) {
                // 移除旧的修改器
                instance.removeModifier(HAPPY_SPEED_MODIFIER_UUID);
                // 计算速度加成：基础 + 等级 × 每级加成
                float speedBonus = BASE_SPEED_BONUS + (amplifier * EXTRA_SPEED_PER_LEVEL);
                // 添加新的修改器
                instance.addTransientModifier(new AttributeModifier(
                        HAPPY_SPEED_MODIFIER_UUID,
                        HAPPY_SPEED_MODIFIER_STRING,
                        speedBonus,
                        AttributeModifier.Operation.MULTIPLY_BASE
                ));
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 移除速度加成
     *
     * @param entity 目标实体
     */
    private static void removeBonus(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null) {
            instance.removeModifier(HAPPY_SPEED_MODIFIER_UUID);
        }
    }

    /**
     * 受到伤害事件处理
     * 受到伤害时移除高兴效果
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(FELEffects.HAPPY.get())) {
            // 移除速度加成
            removeBonus(entity);
            // 移除效果
            entity.removeEffect(FELEffects.HAPPY.get());
        }
    }

    /**
     * 攻击事件处理
     * 攻击时移除高兴效果
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // 检查攻击者是否为LivingEntity
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (attacker.hasEffect(FELEffects.HAPPY.get())) {
                // 移除速度加成
                removeBonus(attacker);
                // 移除效果
                attacker.removeEffect(FELEffects.HAPPY.get());
            }
        }
    }
}