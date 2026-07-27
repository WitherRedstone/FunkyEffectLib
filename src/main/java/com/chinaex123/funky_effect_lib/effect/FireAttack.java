package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

/**
 * 火焰攻击：攻击时使敌人燃烧并造成额外火焰伤害
 * <p>
 * 机制：
 * <ol>
 *   <li>攻击时使目标燃烧：基础15秒，每级增加5秒</li>
 *   <li>立即造成额外火焰伤害：基础2点，每级增加1点</li>
 *   <li>如果目标免疫火焰，则只生成火焰粒子效果</li>
 *   <li>粒子数量随等级增加</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class FireAttack extends MobEffect {

    /** 基础燃烧时间 **/
    private static final int BASE_BURN_TICKS = 300;
    /** 每级额外燃烧时间 **/
    private static final int EXTRA_BURN_TICKS_PER_LEVEL = 100;

    /** 基础火焰伤害 **/
    private static final float BASE_FIRE_DAMAGE = 2.0f;
    /** 每级额外火焰伤害 **/
    private static final float EXTRA_FIRE_DAMAGE_PER_LEVEL = 1.0f;

    public FireAttack(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 实体受伤事件处理
     * 当攻击者拥有火焰攻击效果时，使目标燃烧并造成额外伤害
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        // 检查攻击者是否为LivingEntity
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        if (attacker.level().isClientSide()) {
            return;
        }

        // 检查攻击者是否拥有火焰攻击效果
        MobEffectInstance effectInstance = attacker.getEffect(FELEffects.FIRE_ATTACK.get());
        if (effectInstance == null) {
            return;
        }

        LivingEntity target = event.getEntity();
        int amplifier = effectInstance.getAmplifier();

        // 如果目标免疫火焰，只生成粒子效果
        if (target.fireImmune()) {
            // 生成火焰粒子
            int particleCount = 20 + (amplifier * 10);
            for (int i = 0; i < particleCount; ++i) {
                double px = target.getX() + (target.level().getRandom().nextFloat() * target.getBbWidth() * 2.0F) - target.getBbWidth();
                double py = target.getY() + (target.level().getRandom().nextFloat() * target.getBbHeight());
                double pz = target.getZ() + (target.level().getRandom().nextFloat() * target.getBbWidth() * 2.0F) - target.getBbWidth();
                target.level().addParticle(ParticleTypes.FLAME, px, py, pz, 0.02D, 0.02D, 0.02D);
            }
        } else {
            // 使目标燃烧：基础15秒，每级增加5秒
            int burnTicks = BASE_BURN_TICKS + (amplifier * EXTRA_BURN_TICKS_PER_LEVEL);
            target.setRemainingFireTicks(burnTicks);

            // 立即造成额外的火焰伤害
            float fireDamage = BASE_FIRE_DAMAGE + (amplifier * EXTRA_FIRE_DAMAGE_PER_LEVEL);
            target.hurt(target.damageSources().onFire(), fireDamage);
        }
    }
}