package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELDamageTypes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.effect.SoulburnSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/** 魂燃：潜行时消耗一次经验值，使下一次攻击时附带无视护甲和减伤的真实伤害 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Soulburn extends MobEffect {

    private static final ResourceLocation SOULBURN_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "soulburn");
    private static final String SOULBURN_TAG = "soulburn_charged";
    private static final String SOULBURN_COOLDOWN_TAG = "soulburn_cooldown";

    private static final float DAMAGE_BASE = 4.0f; // 基础伤害
    private static final float DAMAGE_PER_LEVEL = 2.0f; // 每级伤害增加
    private static final int COST_EXP = 50; // 消耗的经验值

    public Soulburn(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    // 玩家登录时同步充能状态
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.hasEffect(FELEffects.SOULBURN)) {
                boolean hasCharge = hasCharge(player);
                PacketDistributor.sendToPlayer(player, new SoulburnSyncPacket(player.getUUID(), hasCharge));
            }
        }
    }

    // 效果移除时清理充能
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.SOULBURN && event.getEntity() instanceof Player player) {
            clearCharge(player);
        }
    }

    // 效果过期时清理充能
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null &&
                event.getEffectInstance().getEffect() == FELEffects.SOULBURN &&
                event.getEntity() instanceof Player player) {
            clearCharge(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        var effect = player.getEffect(FELEffects.SOULBURN);
        if (effect == null) {
            // 没有效果时，确保充能被清理
            if (hasCharge(player)) {
                clearCharge(player);
            }
            return;
        }

        var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute == null) return;

        if (player.isShiftKeyDown()) {
            if (isCooldown(player)) {
                return;
            }

            if (!hasCharge(player)) {
                if (player.totalExperience >= COST_EXP) {
                    player.giveExperiencePoints(-COST_EXP);
                    setCharge(player, true);

                    if (player instanceof ServerPlayer serverPlayer) {
                        PacketDistributor.sendToPlayer(serverPlayer, new SoulburnSyncPacket(player.getUUID(), true));
                    }

                    int amplifier = effect.getAmplifier();
                    float damageBonus = DAMAGE_BASE + (amplifier * DAMAGE_PER_LEVEL);
                    attribute.addTransientModifier(new AttributeModifier(
                            SOULBURN_MODIFIER,
                            damageBonus,
                            AttributeModifier.Operation.ADD_VALUE
                    ));

                    setCooldown(player, true);
                }
            }
        } else {
            setCooldown(player, false);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        var source = event.getSource();
        var attacker = source.getEntity();

        if (!(attacker instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        var effect = player.getEffect(FELEffects.SOULBURN);
        if (effect == null) {
            return;
        }

        if (!hasCharge(player)) {
            return;
        }

        // 计算真实伤害
        int amplifier = effect.getAmplifier();
        float realDamage = DAMAGE_BASE + (amplifier * DAMAGE_PER_LEVEL);

        // 清空充能
        setCharge(player, false);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SoulburnSyncPacket(player.getUUID(), false));
        }

        var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute != null) {
            attribute.removeModifier(SOULBURN_MODIFIER);
        }

        // 在原伤害基础上增加真实伤害
        float originalDamage = event.getNewDamage();
        event.setNewDamage(originalDamage + realDamage);
    }

    private static void clearCharge(Player player) {
        setCharge(player, false);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SoulburnSyncPacket(player.getUUID(), false));
        }
        var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute != null) {
            attribute.removeModifier(SOULBURN_MODIFIER);
        }
        setCooldown(player, false);
    }

    private static boolean hasCharge(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(SOULBURN_TAG);
    }

    private static void setCharge(Player player, boolean charged) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(SOULBURN_TAG, charged);
    }

    private static boolean isCooldown(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getBoolean(SOULBURN_COOLDOWN_TAG);
    }

    private static void setCooldown(Player player, boolean cooldown) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(SOULBURN_COOLDOWN_TAG, cooldown);
    }
}