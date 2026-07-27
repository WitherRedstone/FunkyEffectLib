package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
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
 * 嗜血：攻击时，恢复相当于造成伤害一定比例的生命值
 * <p>
 * 机制：
 * <ol>
 *   <li>基础吸血比例为15%，每级增加10%</li>
 *   <li>造成的伤害越高，恢复的生命值越多</li>
 *   <li>适用于任何造成伤害的攻击（近战、远程、魔法等）</li>
 *   <li>恢复量 = 造成伤害 × 吸血比例</li>
 *   <li>使用事件后阶段获取实际造成的伤害值</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Bloodthirsty extends MobEffect {

    /** 基础吸血比例 **/
    private static final float BASE_HEAL_RATIO = 0.15F;
    /** 每级额外吸血比例 **/
    private static final float EXTRA_HEAL_PER_LEVEL = 0.10F;

    public Bloodthirsty(int color) {
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
     * 当玩家造成伤害时，根据伤害量恢复生命值
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        // 检查伤害来源是否为LivingEntity
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        // 仅在服务端执行
        if (attacker.level().isClientSide()) {
            return;
        }

        // 检查攻击者是否拥有嗜血效果
        MobEffectInstance effect = attacker.getEffect(FELEffects.BLOODTHIRSTY.get());
        if (effect == null) {
            return;
        }

        int amplifier = effect.getAmplifier();

        // 计算吸血比例：基础 + 等级 × 每级加成
        float healRatio = BASE_HEAL_RATIO + (EXTRA_HEAL_PER_LEVEL * amplifier);
        // 计算恢复量：造成伤害 × 吸血比例
        float healAmount = event.getAmount() * healRatio;

        // 治疗攻击者
        attacker.heal(healAmount);
    }
}