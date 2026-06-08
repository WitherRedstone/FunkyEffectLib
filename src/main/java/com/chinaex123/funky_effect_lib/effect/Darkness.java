package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 黑暗之重：每2.5秒降低移动速度 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Darkness extends MobEffect {

    private static final ResourceLocation DARKNESS_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "darkness_slowdown");

    private static final float BASE_SPEED_REDUCTION = -0.05f; // 基础移动速度减少
    private static final int TICKS_PER_INTERVAL = 50; // 降低速度的时间间隔tick
    private static final int MAX_LEVEL = 10; // 最大等级

    private static final Map<UUID, Integer> entityTickMap = new HashMap<>(); // 记录每个实体的当前等级

    public Darkness(int color) {
        super(MobEffectCategory.HARMFUL, color);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                DARKNESS_MODIFIER,
                BASE_SPEED_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide()) {
            return;
        }

        MobEffectInstance darknessEffect = entity.getEffect(FELEffects.DARKNESS);

        if (darknessEffect == null) {
            entityTickMap.remove(entity.getUUID());
            return;
        }

        UUID entityId = entity.getUUID();
        int ticks = entityTickMap.getOrDefault(entityId, 0) + 1;

        if (ticks >= TICKS_PER_INTERVAL) {
            entityTickMap.put(entityId, 0);
            int currentLevel = darknessEffect.getAmplifier();

            if (currentLevel < MAX_LEVEL - 1) {
                entity.addEffect(new MobEffectInstance(
                        darknessEffect.getEffect(),
                        darknessEffect.getDuration(),
                        currentLevel + 1,
                        darknessEffect.isAmbient(),
                        darknessEffect.isVisible(),
                        darknessEffect.showIcon()
                ));
            }
        } else {
            entityTickMap.put(entityId, ticks);
        }
    }
}