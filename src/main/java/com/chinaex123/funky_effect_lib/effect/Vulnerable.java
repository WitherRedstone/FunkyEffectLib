package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;

/** 虚弱：生物将承受额外伤害并减慢移动速度 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Vulnerable extends MobEffect {

    // 移动速度配置
    private static final ResourceLocation SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "vulnerable_speed");

    private static final float BASE_SPEED_REDUCTION = -0.05f; // 基础减速
    private static final float BASE_EXTRA_DAMAGE = 0.35f; // 基础额外伤害
    private static final float EXTRA_DAMAGE_PER_LEVEL = 0.20f; // 每级额外增加

    public Vulnerable(int color) {
        super(MobEffectCategory.HARMFUL, color);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER,
                BASE_SPEED_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
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
     * 监听伤害事件，增加受到的伤害
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();

        // 检查是否有 Vulnerable 效果
        MobEffectInstance effect = entity.getEffect(FELEffects.VULNERABLE);
        if (effect == null) {
            return;
        }

        int amplifier = effect.getAmplifier();
        // 计算额外伤害倍率：基础1.0 + (基础额外伤害 + 每级额外伤害 × 等级)
        float damageMultiplier = 1.0f + (BASE_EXTRA_DAMAGE + (EXTRA_DAMAGE_PER_LEVEL * amplifier));

        // 修改即将受到的伤害
        float originalDamage = event.getNewDamage();
        float newDamage = originalDamage * damageMultiplier;
        event.setNewDamage(newDamage);
    }
}