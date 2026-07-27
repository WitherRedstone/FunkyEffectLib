package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 易怒：效果生效期间受伤获得愤怒效果
 * <p>
 * 机制：
 * <ol>
 *   <li>每受伤5次获得1级愤怒效果</li>
 *   <li>愤怒效果基础持续100刻（5秒），每级增加100刻</li>
 *   <li>受伤冷却时间为50刻（2.5秒）</li>
 *   <li>若200刻（10秒）未受伤，移除易怒效果</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Irritable extends MobEffect {

    /** 愤怒效果基础持续时间 **/
    private static final int BASE_DURATION = 100;
    /** 每级愤怒效果增加的持续时间 **/
    private static final int DURATION_INCREASE_PER_STACK = 100;
    /** 升级所需受伤次数 **/
    private static final int HITS_PER_STACK = 5;
    /** 受伤冷却时间 **/
    private static final int COOLDOWN_TICKS = 50;
    /** 未受伤移除效果的间隔 **/
    private static final int REMOVE_EFFECT_TICKS = 200;

    /** 缓存每个玩家的受伤次数 **/
    private static final Map<UUID, Integer> hitCountMap = new HashMap<>();
    /** 缓存每个玩家的上次受伤时间 **/
    private static final Map<UUID, Long> lastHitTimeMap = new HashMap<>();

    public Irritable(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    /**
     * 实体受伤事件处理
     * 受伤时累积次数，达到阈值时获得愤怒效果
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();

        // 仅对玩家有效
        if (!(entity instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // 检查是否拥有易怒效果
        if (!player.hasEffect(FELEffects.IRRITABLE)) {
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

        // 计算愤怒效果等级和持续时间
        int angryLevel = hitCount / HITS_PER_STACK;
        int angryDuration = BASE_DURATION + (angryLevel * DURATION_INCREASE_PER_STACK);

        // 添加愤怒效果
        MobEffectInstance angryEffect = new MobEffectInstance(
                FELEffects.ANGRY,
                angryDuration,
                angryLevel,
                false,
                true
        );
        player.addEffect(angryEffect);
    }

    /**
     * 实体Tick事件处理
     * 检查是否长时间未受伤，若是则移除易怒效果
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        // 仅处理玩家实体
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // 检查是否拥有易怒效果
        if (!player.hasEffect(FELEffects.IRRITABLE)) {
            return;
        }

        UUID playerId = player.getUUID();
        Long lastHitTime = lastHitTimeMap.get(playerId);

        if (lastHitTime == null) {
            return;
        }

        long currentTime = player.level().getGameTime();

        // 如果超过10秒未受伤，移除易怒效果
        if (currentTime - lastHitTime >= REMOVE_EFFECT_TICKS) {
            player.removeEffect(FELEffects.IRRITABLE);
            hitCountMap.remove(playerId);
            lastHitTimeMap.remove(playerId);
        }
    }
}