package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/** 命运骰子：效果激活期间随机获得一个正面或负面效果 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class DiceOfFate extends MobEffect {

    private static final int REROLL_INTERVAL = 600; // 重新随机的间隔
    private static final int RANDOM_EFFECT_DURATION = 400; // 随机效果的持续时间
    private static final int MIN_EFFECT_AMPLIFIER = 1; // 最小效果等级
    private static final int MAX_EFFECT_AMPLIFIER = 3; // 最大效果等级

    // 黑名单
    private static final Set<MobEffect> BLACKLIST = Set.of(
            MobEffects.HEAL.value(), // 瞬间治疗
            MobEffects.HARM.value(), // 瞬间伤害
            MobEffects.SATURATION.value(), // 饱和
            MobEffects.BAD_OMEN.value(), // 不祥之兆
            MobEffects.HERO_OF_THE_VILLAGE.value()  // 村庄英雄
    );

    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();
    private static List<Holder<MobEffect>> positiveEffects;
    private static List<Holder<MobEffect>> negativeEffects;

    public DiceOfFate(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    private static boolean isBlacklisted(MobEffect effect) {
        return BLACKLIST.contains(effect);
    }

    private static int getRandomAmplifier(RandomSource random) {
        return random.nextInt(MAX_EFFECT_AMPLIFIER - MIN_EFFECT_AMPLIFIER + 1) + MIN_EFFECT_AMPLIFIER;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide()) {
            return;
        }

        UUID entityId = entity.getUUID();
        var effect = entity.getEffect(FELEffects.DICE_OF_FATE);

        if (effect == null) {
            tickCounterMap.remove(entityId);
            return;
        }

        if (positiveEffects == null) {
            initializeEffectLists();
        }

        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        if (tickCounter >= REROLL_INTERVAL) {
            tickCounter = 0;
            tickCounterMap.put(entityId, tickCounter);
            applyRandomEffect(entity);
        } else {
            tickCounterMap.put(entityId, tickCounter);
        }
    }

    private static void applyRandomEffect(LivingEntity entity) {
        Holder<MobEffect> randomEffect = getRandomEffect(entity);
        if (randomEffect != null) {
            int amplifier = getRandomAmplifier(entity.getRandom());
            entity.addEffect(new MobEffectInstance(
                    randomEffect,
                    RANDOM_EFFECT_DURATION,
                    amplifier,
                    false,
                    true
            ));
        }
    }

    private static void initializeEffectLists() {
        positiveEffects = new ArrayList<>();
        negativeEffects = new ArrayList<>();

        BuiltInRegistries.MOB_EFFECT.forEach(effect -> {
            if (isBlacklisted(effect)) return; // 跳过黑名单中的效果

            if (effect.getCategory() == MobEffectCategory.BENEFICIAL) {
                positiveEffects.add(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
            } else if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                negativeEffects.add(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
            }
        });
    }

    private static Holder<MobEffect> getRandomEffect(LivingEntity entity) {
        boolean isPositive = entity.getRandom().nextBoolean();
        List<Holder<MobEffect>> effects = isPositive ? positiveEffects : negativeEffects;

        if (effects.isEmpty()) {
            return null;
        }

        return effects.get(entity.getRandom().nextInt(effects.size()));
    }
}