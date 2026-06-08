package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** 共鸣爆发：周围每存在一个与你有相同效果的生物时提升攻击力 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class ResonantBurst extends MobEffect {

    private static final UUID RESONANT_BURST_MODIFIER_UUID = UUID.fromString("945c4ba4-58c6-49f8-8760-330ba07c5d5e");
    private static final String RESONANT_BURST_MODIFIER_NAME = UUID.nameUUIDFromBytes("resonant_burst_damage".getBytes()).toString();

    private static final int SEARCH_RADIUS = 8; // 搜索半径
    private static final float DAMAGE_BONUS_PER_ENTITY = 0.1f; // 每个共鸣生物增加的伤害
    private static final int MAX_RESONANCE_COUNT = 5; // 最大共鸣数量限制

    // 缓存上次计算的共鸣数量，避免重复更新
    private static final UUID CACHE_KEY = RESONANT_BURST_MODIFIER_UUID;

    public ResonantBurst(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        updateDamageModifier(entity);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 5 == 0;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;
        MobEffectInstance effect = entity.getEffect(FELEffects.RESONANT_BURST.get());

        if (effect != null) {
            updateDamageModifier(entity);
        } else {
            // 如果没有效果，确保移除修饰符
            removeDamageModifier(entity);
        }
    }

    private static void updateDamageModifier(LivingEntity entity) {
        int resonanceCount = countResonantEntities(entity);
        int actualResonanceCount = Math.min(resonanceCount, MAX_RESONANCE_COUNT);
        float damageBonus = actualResonanceCount * DAMAGE_BONUS_PER_ENTITY;

        AttributeInstance attribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute == null) return;

        // 检查当前修饰符的值是否与计算值相同
        AttributeModifier existingModifier = attribute.getModifier(RESONANT_BURST_MODIFIER_UUID);

        if (existingModifier != null && Math.abs(existingModifier.getAmount() - damageBonus) < 0.001) {
            return;
        }

        // 移除旧修饰符
        attribute.removeModifier(RESONANT_BURST_MODIFIER_UUID);

        // 添加新修饰符
        if (damageBonus > 0) {
            AttributeModifier modifier = new AttributeModifier(
                    RESONANT_BURST_MODIFIER_UUID,
                    RESONANT_BURST_MODIFIER_NAME,
                    damageBonus,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            attribute.addTransientModifier(modifier);
        }
    }

    private static void removeDamageModifier(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute != null) {
            attribute.removeModifier(RESONANT_BURST_MODIFIER_UUID);
        }
    }

    private static int countResonantEntities(LivingEntity center) {
        AABB searchArea = center.getBoundingBox().inflate(SEARCH_RADIUS);

        return (int) center.level().getEntitiesOfClass(LivingEntity.class, searchArea, entity -> {
            if (entity == center) return false;
            if (entity.isRemoved()) return false;
            // 检查是否有相同效果（可以是任意等级）
            MobEffectInstance effect = entity.getEffect(FELEffects.RESONANT_BURST.get());
            return effect != null;
        }).size();
    }
}