package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 隐身：完全隐身状态并消除生物对你的仇恨 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Invisibility extends MobEffect {

    private static final double DETECTION_RANGE = 16.0; // 半径范围，用于检测怪物是否对玩家有仇恨

    private static final Map<UUID, Boolean> hasCleared = new HashMap<>(); // 记录每个实体是否已清除仇恨

    public Invisibility(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            entity.setInvisible(true);

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
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 持续阻止怪物将隐形玩家设为目标
     */
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewTarget();

        if (newTarget != null && newTarget.hasEffect(FELEffects.INVISIBILITY.get())) {
            event.setCanceled(true);
        }
    }

    /**
     * 效果结束时恢复可见
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (!entity.hasEffect(FELEffects.INVISIBILITY.get()) && entity.isInvisible()) {
            entity.setInvisible(false);
            hasCleared.remove(entity.getUUID());
        }
    }
}