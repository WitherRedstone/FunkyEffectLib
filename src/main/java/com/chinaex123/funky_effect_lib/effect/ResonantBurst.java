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

/**
 * 共鸣爆发：周围每存在一个与你有相同效果的生物时提升攻击力
 * <p>
 * 机制：
 * <ol>
 *   <li>搜索半径8格内拥有共鸣爆发效果的生物</li>
 *   <li>每个共鸣生物增加10%攻击力</li>
 *   <li>最多计算5个共鸣生物（最大50%加成）</li>
 *   <li>效果消失时自动移除攻击力加成</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class ResonantBurst extends MobEffect {

    private static final ResourceLocation RESONANT_BURST_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "resonant_burst");

    /** 搜索半径 **/
    private static final int SEARCH_RADIUS = 8;
    /** 每个共鸣生物增加的伤害 **/
    private static final float DAMAGE_BONUS_PER_ENTITY = 0.1f;
    /** 最大共鸣数量限制 **/
    private static final int MAX_RESONANCE_COUNT = 5;

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

    /**
     * 实体Tick事件处理
     * 管理共鸣爆发效果的攻击力加成
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        // 仅处理LivingEntity
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        // 如果拥有共鸣爆发效果，更新攻击力加成
        var effect = entity.getEffect(FELEffects.RESONANT_BURST);
        if (effect != null) {
            updateDamageModifier(entity);
        }
    }

    /**
     * 更新攻击力加成
     *
     * @param entity 目标实体
     */
    private static void updateDamageModifier(LivingEntity entity) {
        // 计算共鸣生物数量
        int resonanceCount = countResonantEntities(entity);
        // 限制最大数量
        int actualResonanceCount = Math.min(resonanceCount, MAX_RESONANCE_COUNT);
        // 计算伤害加成
        float damageBonus = actualResonanceCount * DAMAGE_BONUS_PER_ENTITY;

        var attribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute == null) return;

        // 移除旧的修改器
        attribute.removeModifier(RESONANT_BURST_MODIFIER);

        // 如果有加成，添加新的修改器
        if (damageBonus > 0) {
            attribute.addTransientModifier(new AttributeModifier(
                    RESONANT_BURST_MODIFIER,
                    damageBonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    /**
     * 计算周围拥有共鸣爆发效果的实体数量
     *
     * @param center 中心实体
     * @return 共鸣实体数量
     */
    private static int countResonantEntities(LivingEntity center) {
        // 创建搜索区域
        AABB searchArea = center.getBoundingBox().inflate(SEARCH_RADIUS);
        int count = 0;

        // 搜索范围内拥有共鸣爆发效果的实体
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