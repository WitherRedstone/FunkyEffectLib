package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 借贷：受到致命伤害时扣除持续时间而非死亡，效果结束后立即死亡
 * <p>
 * 机制：
 * <ol>
 *   <li>当玩家受到致命伤害时，阻止死亡并扣除200刻（10秒）效果持续时间</li>
 *   <li>持续时间不足200刻时，剩余时间至少为1刻</li>
 *   <li>扣除持续时间后，重新应用效果</li>
 *   <li>效果自然过期时，立即杀死玩家</li>
 *   <li>使用pendingDeathMap标记等待死亡的实体</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class BorrowedTime extends MobEffect {

    /** 每次死亡扣除的持续时间 **/
    private static final int DURATION_PENALTY_PER_DEATH = 200;

    /** 缓存等待死亡的实体UUID **/
    private static final Map<UUID, Boolean> pendingDeathMap = new ConcurrentHashMap<>();

    public BorrowedTime(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 实体受伤事件处理
     * 当受到致命伤害时，扣除持续时间替代死亡
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        UUID entityId = entity.getUUID();

        // 检查是否拥有借贷效果
        MobEffectInstance effect = entity.getEffect(FELEffects.BORROWED_TIME.get());
        if (effect == null) {
            return;
        }

        float originalDamage = event.getAmount();
        float currentHealth = entity.getHealth();

        // 检查是否为致命伤害
        if (originalDamage >= currentHealth) {
            // 取消本次伤害
            event.setAmount(0.0f);

            // 计算新的持续时间（扣除200刻，至少保留1刻）
            int currentDuration = effect.getDuration();
            int newDuration = Math.max(1, currentDuration - DURATION_PENALTY_PER_DEATH);

            // 标记等待死亡
            pendingDeathMap.put(entityId, true);

            // 移除旧效果并重新添加（更新持续时间）
            entity.removeEffect(FELEffects.BORROWED_TIME.get());
            MobEffectInstance newEffect = new MobEffectInstance(FELEffects.BORROWED_TIME.get(), newDuration, effect.getAmplifier());
            entity.addEffect(newEffect);
        }
    }

    /**
     * 效果过期事件处理
     * 效果自然过期时，如果标记了等待死亡，则杀死实体
     *
     * @param event 效果过期事件
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.BORROWED_TIME.get()) {
            LivingEntity entity = event.getEntity();
            UUID entityId = entity.getUUID();
            // 检查是否标记了等待死亡
            if (pendingDeathMap.containsKey(entityId)) {
                pendingDeathMap.remove(entityId);
                killEntity(entity);
            }
        }
    }

    /**
     * 杀死实体
     *
     * @param entity 目标实体
     */
    private static void killEntity(LivingEntity entity) {
        entity.setHealth(0.0f);
    }

    /**
     * 清除等待死亡标记
     *
     * @param entityId 实体UUID
     */
    public static void clearPendingDeath(UUID entityId) {
        pendingDeathMap.remove(entityId);
    }
}