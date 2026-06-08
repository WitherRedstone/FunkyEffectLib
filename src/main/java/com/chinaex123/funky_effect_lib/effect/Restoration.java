package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 恢复：缓慢恢复生命值 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Restoration extends MobEffect {

    private static final int TICKS_PER_HEAL = 50; // 每次恢复的时间
    private static final float BASE_HEAL_AMOUNT = 2.0f; // 基础恢复生命值
    private static final float HEAL_PER_LEVEL = 2.0f; // 每级额外恢复生命值

    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>(); // 每个实体的 tick 计数

    public Restoration(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            UUID entityId = entity.getUUID();
            int ticks = tickCounterMap.getOrDefault(entityId, 0) + 1;

            if (ticks >= TICKS_PER_HEAL) {
                tickCounterMap.put(entityId, 0);

                float healAmount = BASE_HEAL_AMOUNT + (HEAL_PER_LEVEL * amplifier);
                entity.heal(healAmount);
            } else {
                tickCounterMap.put(entityId, ticks);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 效果结束时清理计数
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (!entity.hasEffect(FELEffects.RESTORATION.get())) {
            tickCounterMap.remove(entity.getUUID());
        }
    }
}