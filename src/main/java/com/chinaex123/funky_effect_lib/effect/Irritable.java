package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 易怒：效果生效期间受伤获得愤怒 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Irritable extends MobEffect {

    private static final int BASE_DURATION = 100; // 基础持续时间
    private static final int DURATION_INCREASE_PER_STACK = 100; // 每级增加的时间
    private static final int HITS_PER_STACK = 5; // 受伤次数增加一级
    private static final int COOLDOWN_TICKS = 50; // 受伤冷却
    private static final int REMOVE_EFFECT_TICKS = 200; // 无受伤移除效果的间隔

    private static final Map<UUID, Integer> hitCountMap = new HashMap<>();  // 每个玩家的受伤次数
    private static final Map<UUID, Long> lastHitTimeMap = new HashMap<>();  // 每个玩家的上次受伤时间

    public Irritable(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // 检查是否有易怒效果
        if (!player.hasEffect(FELEffects.IRRITABLE.get())) {
            UUID playerId = player.getUUID();
            hitCountMap.remove(playerId);
            lastHitTimeMap.remove(playerId);
            return;
        }

        UUID playerId = player.getUUID();
        long currentTime = player.level().getGameTime();

        // 检查冷却时间
        Long lastHitTime = lastHitTimeMap.get(playerId);
        if (lastHitTime != null && currentTime - lastHitTime < COOLDOWN_TICKS) {
            return;
        }

        // 更新上次受伤时间
        lastHitTimeMap.put(playerId, currentTime);

        // 增加受伤次数
        int hitCount = hitCountMap.getOrDefault(playerId, 0) + 1;
        hitCountMap.put(playerId, hitCount);

        // 计算 愤怒 效果的等级和持续时间
        int angryLevel = hitCount / HITS_PER_STACK;
        int angryDuration = BASE_DURATION + (angryLevel * DURATION_INCREASE_PER_STACK);

        MobEffectInstance angryEffect = new MobEffectInstance(
                FELEffects.ANGRY.get(),
                angryDuration,
                angryLevel,
                false,
                true
        );
        player.addEffect(angryEffect);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide()) {
            return;
        }

        if (!player.hasEffect(FELEffects.IRRITABLE.get())) {
            return;
        }

        UUID playerId = player.getUUID();
        Long lastHitTime = lastHitTimeMap.get(playerId);

        if (lastHitTime == null) {
            return;
        }

        long currentTime = player.level().getGameTime();

        // 检查未受伤的时间
        if (currentTime - lastHitTime >= REMOVE_EFFECT_TICKS) {
            player.removeEffect(FELEffects.IRRITABLE.get());
            hitCountMap.remove(playerId);
            lastHitTimeMap.remove(playerId);
        }
    }
}