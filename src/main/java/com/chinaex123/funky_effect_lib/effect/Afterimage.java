package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.entity.AfterimageClone;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

/**
 * 残影：受到伤害后有概率生成一个分身吸引敌人
 * <p>
 * 机制：
 * <ol>
 *   <li>基础生成概率25%，每级增加5%</li>
 *   <li>基础持续时间200刻（10秒），每级增加100刻（5秒）</li>
 *   <li>分身会吸引附近敌人的仇恨，保护玩家</li>
 *   <li>效果触发后自动移除残影效果</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Afterimage extends MobEffect {

    /** 基础生成概率 **/
    private static final float BASE_CHANCE = 0.25f;
    /** 每级增加的生成概率 **/
    private static final float CHANCE_PER_LEVEL = 0.05f;
    /** 基础持续时间 **/
    private static final int BASE_DURATION = 200;
    /** 每级增加的持续时间 **/
    private static final int DURATION_PER_LEVEL = 100;

    public Afterimage(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 获取分身持续时间
     *
     * @param amplifier 效果等级
     * @return 持续时间（刻）
     */
    public static int getCloneDuration(int amplifier) {
        return BASE_DURATION + (amplifier * DURATION_PER_LEVEL);
    }

    /**
     * 获取分身生成概率
     *
     * @param amplifier 效果等级
     * @return 生成概率（0.0 ~ 1.0）
     */
    public static float getCloneChance(int amplifier) {
        return Math.min(1.0f, BASE_CHANCE + (amplifier * CHANCE_PER_LEVEL));
    }

    /**
     * 实体受伤事件处理
     * 当拥有残影效果的玩家受到伤害时，有概率生成分身
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        // 仅在服务端执行
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        // 检查玩家是否拥有残影效果
        MobEffectInstance effect = player.getEffect(FELEffects.AFTERIMAGE.get());
        if (effect == null) {
            return;
        }

        // 根据等级计算生成概率
        float chance = getCloneChance(effect.getAmplifier());

        // 判定是否生成分身
        if (player.getRandom().nextFloat() < chance) {
            // 获取分身持续时间
            int duration = getCloneDuration(effect.getAmplifier());
            // 创建分身
            AfterimageClone.createClone(player, duration);
            // 触发后移除残影效果
            player.removeEffect(FELEffects.AFTERIMAGE.get());
        }
    }
}