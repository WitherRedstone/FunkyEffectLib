package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELDamageTypes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 魔力灼烧：每秒造成魔法伤害
 * <p>
 * 机制：
 * <ol>
 *   <li>基础伤害为2点，每级增加1.5点</li>
 *   <li>每1秒（20刻）造成一次伤害</li>
 *   <li>伤害类型为自定义的MANA_BURN（魔力灼烧）</li>
 *   <li>效果消失时停止伤害</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class ManaBurn extends MobEffect {

    /** 基础伤害 **/
    private static final float BASE_DAMAGE = 2.0f;
    /** 每级额外伤害 **/
    private static final float ADDITIONAL_DAMAGE_PER_LEVEL = 1.5f;
    /** 伤害间隔 **/
    private static final int DAMAGE_INTERVAL = 20;

    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();

    public ManaBurn(int color) {
        super(MobEffectCategory.HARMFUL, color);
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
     * 实体Tick事件处理
     * 管理魔力灼烧的计时和伤害
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        // 仅处理LivingEntity
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide()) {
            return;
        }

        UUID entityId = entity.getUUID();
        MobEffectInstance effect = entity.getEffect(FELEffects.MANA_BURN);

        if (effect == null) {
            // 效果消失，清除计时器
            tickCounterMap.remove(entityId);
            return;
        }

        // 计时器递增
        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        // 达到伤害间隔时造成伤害
        if (tickCounter >= DAMAGE_INTERVAL) {
            int amplifier = effect.getAmplifier();
            // 计算伤害：基础 + 等级 × 每级加成
            float damage = BASE_DAMAGE + (amplifier * ADDITIONAL_DAMAGE_PER_LEVEL);

            // 创建魔力灼烧伤害源
            DamageSource manaBurnDamage = entity.level().damageSources().source(FELDamageTypes.MANA_BURN);
            // 对实体造成伤害
            entity.hurt(manaBurnDamage, damage);
            // 重置计时器
            tickCounter = 0;
        }

        tickCounterMap.put(entityId, tickCounter);
    }
}