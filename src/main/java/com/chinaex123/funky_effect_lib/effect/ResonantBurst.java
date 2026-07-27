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
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class ResonantBurst extends MobEffect {

    private static final UUID RESONANT_BURST_MODIFIER_UUID = UUID.fromString("945c4ba4-58c6-49f8-8760-330ba07c5d5e");
    private static final String RESONANT_BURST_MODIFIER_NAME = UUID.nameUUIDFromBytes("resonant_burst_damage".getBytes()).toString();

    /** 搜索半径 **/
    private static final int SEARCH_RADIUS = 8;
    /** 每个共鸣生物增加的伤害（10%） **/
    private static final float DAMAGE_BONUS_PER_ENTITY = 0.1f;
    /** 最大共鸣数量限制（5个） **/
    private static final int MAX_RESONANCE_COUNT = 5;

    public ResonantBurst(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        updateDamageModifier(entity);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每5刻更新一次，减少性能开销
        return duration % 5 == 0;
    }

    /**
     * 玩家Tick事件处理
     * 管理共鸣爆发效果的攻击力加成
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;
        MobEffectInstance effect = entity.getEffect(FELEffects.RESONANT_BURST.get());

        if (effect != null) {
            // 更新攻击力加成
            updateDamageModifier(entity);
        } else {
            // 如果没有效果，确保移除加成
            removeDamageModifier(entity);
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

        AttributeInstance attribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute == null) return;

        // 检查当前修饰符的值是否与计算值相同，避免不必要的更新
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

    /**
     * 移除攻击力加成
     *
     * @param entity 目标实体
     */
    private static void removeDamageModifier(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute != null) {
            attribute.removeModifier(RESONANT_BURST_MODIFIER_UUID);
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

        // 搜索范围内拥有共鸣爆发效果的实体
        return center.level().getEntitiesOfClass(LivingEntity.class, searchArea, entity -> {
            if (entity == center) return false;
            if (entity.isRemoved()) return false;
            // 检查是否有共鸣爆发效果（任意等级）
            MobEffectInstance effect = entity.getEffect(FELEffects.RESONANT_BURST.get());
            return effect != null;
        }).size();
    }
}