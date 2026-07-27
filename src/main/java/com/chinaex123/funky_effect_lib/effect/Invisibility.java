package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 隐身：完全隐身状态并消除生物对你的仇恨
 * <p>
 * 机制：
 * <ol>
 *   <li>将实体设置为完全不可见</li>
 *   <li>效果开始时清除16格内所有怪物的仇恨</li>
 *   <li>持续阻止怪物将隐身玩家设为目标</li>
 *   <li>效果结束时恢复可见</li>
 *   <li>使用hasCleared标记确保只清除一次仇恨</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Invisibility extends MobEffect {

    /** 仇恨清除范围 **/
    private static final double DETECTION_RANGE = 16.0;

    /** 记录每个实体是否已清除仇恨 **/
    private static final Map<UUID, Boolean> hasCleared = new HashMap<>();

    public Invisibility(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 仅在服务端执行
        if (!entity.level().isClientSide()) {
            // 设置实体为不可见
            entity.setInvisible(true);

            // 首次应用效果时清除周围怪物的仇恨
            UUID uuid = entity.getUUID();
            if (!hasCleared.getOrDefault(uuid, false)) {
                AABB area = entity.getBoundingBox().inflate(DETECTION_RANGE);
                entity.level().getEntitiesOfClass(Mob.class, area).forEach(mob -> {
                    if (mob.getTarget() == entity) {
                        mob.setTarget(null);
                    }
                });
                hasCleared.put(uuid, true);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 持续阻止怪物将隐身玩家设为目标
     *
     * @param event 实体变更目标事件
     */
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();

        // 如果新目标拥有隐身效果，取消本次目标变更
        if (newTarget != null && newTarget.hasEffect(FELEffects.INVISIBILITY)) {
            event.setCanceled(true);
        }
    }

    /**
     * 实体Tick事件处理
     * 效果结束时恢复可见
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide()) {
            // 如果实体没有隐身效果但仍然可见，恢复可见状态
            if (!entity.hasEffect(FELEffects.INVISIBILITY) && entity.isInvisible()) {
                entity.setInvisible(false);
            }
        }
    }
}