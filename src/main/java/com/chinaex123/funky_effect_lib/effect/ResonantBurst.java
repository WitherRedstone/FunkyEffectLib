package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

/** 共鸣爆发：周围每存在一个与你有相同效果的生物时提升攻击力 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class ResonantBurst extends MobEffect {

    private static final ResourceLocation RESONANT_BURST_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "resonant_burst");

    private static final int SEARCH_RADIUS = 8; // 搜索半径
    private static final float DAMAGE_BONUS_PER_ENTITY = 0.1f; // 每个共鸣生物增加的伤害
    private static final int MAX_RESONANCE_COUNT = 5; // 最大共鸣数量限制

    public ResonantBurst(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        updateDamageModifier(entity);
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

        var effect = entity.getEffect(FELEffects.RESONANT_BURST);
        if (effect != null) {
            updateDamageModifier(entity);
        }
    }

    private static void updateDamageModifier(LivingEntity entity) {
        int resonanceCount = countResonantEntities(entity);
        int actualResonanceCount = Math.min(resonanceCount, MAX_RESONANCE_COUNT);
        float damageBonus = actualResonanceCount * DAMAGE_BONUS_PER_ENTITY;

        var attribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute == null) return;

        attribute.removeModifier(RESONANT_BURST_MODIFIER);

        if (damageBonus > 0) {
            attribute.addTransientModifier(new AttributeModifier(
                    RESONANT_BURST_MODIFIER,
                    damageBonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    private static int countResonantEntities(LivingEntity center) {
        AABB searchArea = center.getBoundingBox().inflate(SEARCH_RADIUS);
        int count = 0;

        for (LivingEntity entity : center.level().getEntitiesOfClass(LivingEntity.class, searchArea, entity -> {
            if (entity == center) return false;
            if (entity.isRemoved()) return false;
            return entity.hasEffect(FELEffects.RESONANT_BURST);
        })) {
            count++;
        }

        return count;
    }
}