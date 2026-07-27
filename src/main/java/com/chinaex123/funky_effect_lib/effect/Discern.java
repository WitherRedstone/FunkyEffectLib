package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 洞察：攻击同类型生物时伤害递增，目标死亡时重置
 * <p>
 * 机制：
 * <ol>
 *   <li>每次攻击同类型生物，伤害倍数增加0.1</li>
 *   <li>基础伤害倍数为1.0，最高3.0</li>
 *   <li>攻击不同目标时独立计数</li>
 *   <li>目标死亡后重置对应的计数</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Discern extends MobEffect {

    /** 基础伤害倍数 **/
    private static final float BASE_DAMAGE_MULTIPLIER = 1.0f;
    /** 每次攻击增加的伤害倍数 **/
    private static final float DAMAGE_INCREASE_PER_HIT = 0.1f;
    /** 最大伤害倍数 **/
    private static final float MAX_MULTIPLIER = 3.0f;

    /**
     * 存储每个攻击者的计数数据
     * UUID → (EntityType → 攻击次数)
     */
    private static final Map<UUID, Map<EntityType<?>, Integer>> attackCountMap = new HashMap<>();

    public Discern(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 实体受伤事件处理
     * 攻击同类型生物时递增伤害
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        // 检查攻击者是否为LivingEntity
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        if (attacker.level().isClientSide()) {
            return;
        }

        // 检查攻击者是否拥有洞察效果
        MobEffectInstance effect = attacker.getEffect(FELEffects.DISCERN.get());
        if (effect == null) {
            return;
        }

        LivingEntity target = event.getEntity();

        UUID attackerId = attacker.getUUID();
        EntityType<?> targetType = target.getType();

        // 获取或创建该攻击者的计数映射
        Map<EntityType<?>, Integer> typeCountMap = attackCountMap.computeIfAbsent(attackerId, k -> new HashMap<>());
        // 获取当前攻击次数
        int hitCount = typeCountMap.getOrDefault(targetType, 0);

        // 计算伤害倍数（基础 + 次数 × 每次增量，上限为最大值）
        float multiplier = BASE_DAMAGE_MULTIPLIER + (hitCount * DAMAGE_INCREASE_PER_HIT);
        multiplier = Math.min(multiplier, MAX_MULTIPLIER);

        // 应用伤害倍数
        float originalDamage = event.getAmount();
        float newDamage = originalDamage * multiplier;
        event.setAmount(newDamage);

        // 增加攻击次数
        hitCount++;
        typeCountMap.put(targetType, hitCount);
    }

    /**
     * 实体死亡事件处理
     * 目标死亡后重置对应的计数
     *
     * @param event 实体死亡事件
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();
        EntityType<?> targetType = target.getType();

        // 遍历所有攻击者，移除对应目标类型的计数
        attackCountMap.entrySet().removeIf(entry -> {
            Map<EntityType<?>, Integer> typeCountMap = entry.getValue();
            typeCountMap.remove(targetType);
            // 如果该攻击者的计数映射为空，一并移除
            return typeCountMap.isEmpty();
        });
    }

    /**
     * 获取指定攻击者对特定实体类型的攻击次数
     *
     * @param attackerId 攻击者UUID
     * @param targetType 目标实体类型
     * @return 攻击次数
     */
    public static int getHitCount(UUID attackerId, EntityType<?> targetType) {
        Map<EntityType<?>, Integer> typeCountMap = attackCountMap.get(attackerId);
        if (typeCountMap == null) {
            return 0;
        }
        return typeCountMap.getOrDefault(targetType, 0);
    }

    /**
     * 清除指定攻击者的所有计数数据
     *
     * @param attackerId 攻击者UUID
     */
    public static void clearAttackerData(UUID attackerId) {
        attackCountMap.remove(attackerId);
    }
}