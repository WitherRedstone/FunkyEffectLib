package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 命运骰子：效果激活期间，每隔30秒随机获得一个正面或负面效果
 * <p>
 * 机制：
 * <ol>
 *   <li>每600刻（30秒）重新随机一次</li>
 *   <li>随机效果持续400刻（20秒）</li>
 *   <li>效果等级在0~3之间随机</li>
 *   <li>正面/负面效果各50%概率</li>
 *   <li>黑名单效果不会被选中（瞬间治疗、瞬间伤害、饱和等）</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class DiceOfFate extends MobEffect {

    /** 重新随机间隔 **/
    private static final int REROLL_INTERVAL = 600;
    /** 随机效果的持续时间 **/
    private static final int RANDOM_EFFECT_DURATION = 400;
    /** 最小效果等级 **/
    private static final int MIN_EFFECT_AMPLIFIER = 0;
    /** 最大效果等级 **/
    private static final int MAX_EFFECT_AMPLIFIER = 3;

    /** 黑名单效果（不会随机到的效果） **/
    private static final Set<MobEffect> BLACKLIST = Set.of(
            MobEffects.HEAL, // 瞬间治疗
            MobEffects.HARM, // 瞬间伤害
            MobEffects.SATURATION, // 饱和
            MobEffects.BAD_OMEN, // 不祥之兆
            MobEffects.HERO_OF_THE_VILLAGE  // 村庄英雄
    );

    /** 缓存每个玩家的Tick计数器 **/
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();
    /** 正面效果列表 **/
    private static List<MobEffect> positiveEffects;
    /** 负面效果列表 **/
    private static List<MobEffect> negativeEffects;

    public DiceOfFate(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 检查效果是否在黑名单中
     *
     * @param effect 要检查的效果
     * @return true表示在黑名单中
     */
    private static boolean isBlacklisted(MobEffect effect) {
        return BLACKLIST.contains(effect);
    }

    /**
     * 获取随机效果等级
     *
     * @param random 随机源
     * @return 0~3之间的随机等级
     */
    private static int getRandomAmplifier(RandomSource random) {
        return random.nextInt(MAX_EFFECT_AMPLIFIER - MIN_EFFECT_AMPLIFIER + 1) + MIN_EFFECT_AMPLIFIER;
    }

    /**
     * 玩家Tick事件处理
     * 管理随机效果的触发
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (entity.level().isClientSide()) {
            return;
        }

        UUID entityId = entity.getUUID();
        MobEffectInstance effect = entity.getEffect(FELEffects.DICE_OF_FATE.get());

        // 如果效果消失，清除计数器
        if (effect == null) {
            tickCounterMap.remove(entityId);
            return;
        }

        // 初始化效果列表（懒加载）
        if (positiveEffects == null) {
            initializeEffectLists();
        }

        // 计时器递增
        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        // 达到间隔时间，触发随机效果
        if (tickCounter >= REROLL_INTERVAL) {
            tickCounter = 0;
            tickCounterMap.put(entityId, tickCounter);
            applyRandomEffect(entity);
        } else {
            tickCounterMap.put(entityId, tickCounter);
        }
    }

    /**
     * 应用随机效果
     *
     * @param entity 目标实体
     */
    private static void applyRandomEffect(LivingEntity entity) {
        MobEffect randomEffect = getRandomEffect(entity);
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

    /**
     * 初始化效果列表
     * 将注册的所有效果分为正面和负面两组
     */
    private static void initializeEffectLists() {
        positiveEffects = new ArrayList<>();
        negativeEffects = new ArrayList<>();

        ForgeRegistries.MOB_EFFECTS.forEach(effect -> {
            // 跳过黑名单效果
            if (isBlacklisted(effect)) return;

            // 根据效果类别分类
            if (effect.getCategory() == MobEffectCategory.BENEFICIAL) {
                positiveEffects.add(effect);
            } else if (effect.getCategory() == MobEffectCategory.HARMFUL) {
                negativeEffects.add(effect);
            }
        });
    }

    /**
     * 获取随机效果
     * 50%概率选择正面效果，50%概率选择负面效果
     *
     * @param entity 目标实体（用于随机源）
     * @return 随机选中的效果
     */
    private static MobEffect getRandomEffect(LivingEntity entity) {
        boolean isPositive = entity.getRandom().nextBoolean();
        List<MobEffect> effects = isPositive ? positiveEffects : negativeEffects;

        if (effects.isEmpty()) {
            return null;
        }

        return effects.get(entity.getRandom().nextInt(effects.size()));
    }
}