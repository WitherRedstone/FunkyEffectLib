package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

/** 后发制人：抵消一次伤害，下次攻击伤害大幅提升，攻击后效果消失 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Payback extends MobEffect {

    private static final float DAMAGE_MULTIPLIER = 3.0F;  // 下次攻击伤害倍率

    private static final Set<Player> paybackPlayers = new HashSet<>(); // 记录已抵消伤害的玩家

    public Payback(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    /**
     * 处理受到伤害事件 - 抵消伤害
     */
    @SubscribeEvent
    public static void onLivingDamageReceived(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();

        if (target.level().isClientSide()) {
            return;
        }

        if (target instanceof Player player && player.hasEffect(FELEffects.PAYBACK.get())) {
            // 如果已经标记过，不再抵消
            if (paybackPlayers.contains(player)) {
                return;
            }

            // 抵消这次伤害
            event.setAmount(0.0F);

            // 标记该玩家下次攻击增伤
            paybackPlayers.add(player);
        }
    }

    /**
     * 处理造成伤害事件 - 应用增伤并移除效果
     */
    @SubscribeEvent
    public static void onLivingDamageDealt(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getSource().getEntity() instanceof Player player) {
            if (paybackPlayers.contains(player)) {
                float originalDamage = event.getAmount();
                float newDamage = originalDamage * DAMAGE_MULTIPLIER;

                // 设置倍率伤害
                event.setAmount(newDamage);

                // 移除状态效果
                player.removeEffect(FELEffects.PAYBACK.get());

                // 清除标记
                paybackPlayers.remove(player);
            }
        }
    }
}