package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 恢复：缓慢恢复生命值
 * <p>
 * 机制：
 * <ol>
 *   <li>每2.5秒（50刻）恢复一次</li>
 *   <li>基础恢复2点生命值，每级增加2点</li>
 *   <li>效果消失时自动清理计时器</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Restoration extends MobEffect {

    /** 每次恢复间隔 **/
    private static final int TICKS_PER_HEAL = 50;
    /** 基础恢复生命值 **/
    private static final float BASE_HEAL_AMOUNT = 2.0f;
    /** 每级额外恢复生命值 **/
    private static final float HEAL_PER_LEVEL = 2.0f;

    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();

    public Restoration(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 仅在服务端执行
        if (!entity.level().isClientSide()) {
            UUID entityId = entity.getUUID();
            int ticks = tickCounterMap.getOrDefault(entityId, 0) + 1;

            // 达到间隔时间，触发治疗
            if (ticks >= TICKS_PER_HEAL) {
                tickCounterMap.put(entityId, 0);
                // 计算恢复量：基础 + 等级 × 每级加成
                float healAmount = BASE_HEAL_AMOUNT + (HEAL_PER_LEVEL * amplifier);
                // 恢复生命值
                entity.heal(healAmount);
            } else {
                tickCounterMap.put(entityId, ticks);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 实体Tick事件处理
     * 效果结束时清理计时器
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        // 仅处理LivingEntity，且仅在服务端执行
        if (event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide()) {
            // 如果实体没有恢复效果，清除计时器
            if (!entity.hasEffect(FELEffects.RESTORATION)) {
                tickCounterMap.remove(entity.getUUID());
            }
        }
    }
}