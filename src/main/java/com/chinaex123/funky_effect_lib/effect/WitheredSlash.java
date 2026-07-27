package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

/**
 * 枯斩：攻击敌人可以虚弱敌人并对其造成凋零
 * <p>
 * 机制：
 * <ol>
 *   <li>攻击时为目标施加凋零效果：基础60刻（3秒），每级增加20刻（1秒）</li>
 *   <li>攻击时为目标施加虚弱效果：持续100刻（5秒）</li>
 *   <li>两个效果的等级与枯斩效果等级相同</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class WitheredSlash extends MobEffect {

    /** 凋零效果基础持续时间 **/
    private static final int WITHER_BASE_DURATION_TICKS = 60;
    /** 每级增加的凋零持续时间 **/
    private static final int WITHER_EXTRA_DURATION_PER_LEVEL = 20;
    /** 虚弱效果持续时间 **/
    private static final int WEAKNESS_BASE_DURATION_TICKS = 100;

    public WitheredSlash(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 实体受伤事件处理
     * 攻击时为目标施加凋零和虚弱效果
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent event) {
        // 检查攻击者是否为玩家
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (attacker.level().isClientSide()) {
            return;
        }

        // 检查玩家是否拥有枯斩效果
        MobEffectInstance effectInstance = attacker.getEffect(FELEffects.WITHERED_SLASH.get());
        if (effectInstance != null) {
            LivingEntity target = event.getEntity();
            int amplifier = effectInstance.getAmplifier();

            // 计算凋零持续时间：基础 + 等级 × 每级加成
            int witherDuration = WITHER_BASE_DURATION_TICKS + (amplifier * WITHER_EXTRA_DURATION_PER_LEVEL);
            // 为目标添加凋零效果
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, witherDuration, amplifier, false, false));
            // 为目标添加虚弱效果
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_BASE_DURATION_TICKS, amplifier, false, false));
        }
    }
}