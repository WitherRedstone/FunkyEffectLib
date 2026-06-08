package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 战意沸腾：每次攻击提升攻击力，若3秒未攻击则重置效果 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class BattleFrenzy extends MobEffect {

    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("bc5096c6-4a31-4f0c-9356-66a0174ac362");
    private static final String ATTACK_DAMAGE_MODIFIER_STRING = UUID.nameUUIDFromBytes("battle_frenzy_damage".getBytes()).toString();

    private static final float DAMAGE_INCREASE_PER_ATTACK = 0.01f; // 每次攻击提升的成倍攻击力的比例
    private static final float MAX_DAMAGE_INCREASE = 2.0f; // 最大攻击力加成
    private static final int RESET_TICKS = 60; // 间隔重置的时间

    private static final Map<UUID, Integer> attackCountMap = new HashMap<>(); // 的攻击次数
    private static final Map<UUID, Long> lastAttackTimeMap = new HashMap<>(); // 上次攻击的时间
    private static final Map<UUID, Float> currentDamageMap = new HashMap<>(); // 当前的成倍攻击力

    public BattleFrenzy(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (!player.hasEffect(FELEffects.BATTLE_FRENZY.get())) {
            return;
        }

        UUID playerId = player.getUUID();
        long currentTime = player.level().getGameTime();

        int attackCount = attackCountMap.getOrDefault(playerId, 0) + 1;
        attackCountMap.put(playerId, attackCount);
        lastAttackTimeMap.put(playerId, currentTime);

        float damageIncrease = Math.min(attackCount * DAMAGE_INCREASE_PER_ATTACK, MAX_DAMAGE_INCREASE);
        currentDamageMap.put(playerId, damageIncrease);
        applyAttributes(player, damageIncrease);
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

        MobEffectInstance effect = player.getEffect(FELEffects.BATTLE_FRENZY.get());

        if (effect == null) {
            UUID playerId = player.getUUID();
            clearAttributes(player);
            attackCountMap.remove(playerId);
            lastAttackTimeMap.remove(playerId);
            currentDamageMap.remove(playerId);
            return;
        }

        UUID playerId = player.getUUID();
        Long lastAttackTime = lastAttackTimeMap.get(playerId);

        if (lastAttackTime == null) {
            return;
        }

        long currentTime = player.level().getGameTime();

        if (currentTime - lastAttackTime >= RESET_TICKS) {
            attackCountMap.put(playerId, 0);
            currentDamageMap.put(playerId, 0.0f);
            clearAttributes(player);
        }
    }

    private static void applyAttributes(Player player, float damageIncrease) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
            attackDamage.addTransientModifier(new AttributeModifier(
                    ATTACK_DAMAGE_MODIFIER_UUID,
                    ATTACK_DAMAGE_MODIFIER_STRING,
                    damageIncrease,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    private static void clearAttributes(Player player) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
        }
    }
}