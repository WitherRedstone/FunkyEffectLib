package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class IronWill extends MobEffect {

    private static final String KNOCKBACK_RESISTANCE_MODIFIER_STRING = UUID.nameUUIDFromBytes("iron_will_knockback_resistance".getBytes()).toString();

    /** 基础击退抗性 **/
    private static final float BASE_KNOCKBACK_RESISTANCE = 0.25f;

    public IronWill(int color) {
        super(MobEffectCategory.BENEFICIAL, color);

        // 添加击退抗性修改器
        this.addAttributeModifier(
                Attributes.KNOCKBACK_RESISTANCE,
                KNOCKBACK_RESISTANCE_MODIFIER_STRING,
                BASE_KNOCKBACK_RESISTANCE,
                AttributeModifier.Operation.MULTIPLY_BASE
        );
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家Tick事件处理
     * 自动移除虚弱和挖掘疲劳效果
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (entity.level().isClientSide()) {
            return;
        }

        // 检查是否拥有钢铁意志效果
        MobEffectInstance effect = entity.getEffect(FELEffects.IRON_WILL.get());
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