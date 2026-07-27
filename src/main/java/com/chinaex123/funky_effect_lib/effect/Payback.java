package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * 后发制人：抵消一次伤害，下次攻击伤害大幅提升，攻击后效果消失
 * <p>
 * 机制：
 * <ol>
 *   <li>受到伤害时抵消本次伤害（仅一次）</li>
 *   <li>抵消后标记玩家，下次攻击伤害提升至3倍</li>
 *   <li>攻击后移除效果并清除标记</li>
 *   <li>使用Set防止重复标记</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Payback extends MobEffect {

    /** 下次攻击伤害倍率 **/
    private static final float DAMAGE_MULTIPLIER = 3.0F;

    /** 记录已抵消伤害的玩家 **/
    private static final Set<Player> paybackPlayers = new HashSet<>();

    public Payback(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    /**
     * 受到伤害事件处理
     * 抵消本次伤害并标记玩家
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamageReceived(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();

        // 检查是否为玩家且拥有后发制人效果
        if (target instanceof Player player && player.hasEffect(FELEffects.PAYBACK)) {
            // 如果已经标记过，不再重复抵消
            if (paybackPlayers.contains(player)) {
                return;
            }

            // 抵消本次伤害（设置为0）
            event.setNewDamage(0.0F);

            // 标记该玩家，下次攻击将增伤
            paybackPlayers.add(player);
        }
    }

    /**
     * 造成伤害事件处理
     * 应用增伤并移除效果和标记
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamageDealt(LivingDamageEvent.Pre event) {
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player player) {
            // 如果玩家已被标记，应用增伤
            if (paybackPlayers.contains(player)) {
                float originalDamage = event.getOriginalDamage();
                float newDamage = originalDamage * DAMAGE_MULTIPLIER;

                // 设置倍率伤害
                event.setNewDamage(newDamage);

                // 移除后发制人效果
                player.removeEffect(FELEffects.PAYBACK);

                // 清除标记
                paybackPlayers.remove(player);
            }
        }
    }
}