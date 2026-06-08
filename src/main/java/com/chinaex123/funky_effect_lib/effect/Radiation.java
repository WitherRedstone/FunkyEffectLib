package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELDamageTypes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 辐射：每秒造成伤害并快速降低饱食度和护甲值 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Radiation extends MobEffect {

    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("F6A7B8C9-0D1E-4F2A-3B4C-5D6E7F8A9B0C");
    private static final String ARMOR_MODIFIER_STRING = UUID.nameUUIDFromBytes("radiation_armor".getBytes()).toString();

    private static final float BASE_DAMAGE = 1.0f; // 基础伤害
    private static final float ADDITIONAL_DAMAGE_PER_LEVEL = 1.0f; // 每级额外伤害
    private static final int DAMAGE_INTERVAL = 70; // 伤害间隔

    private static final float BASE_ARMOR_REDUCTION = -0.20f; // 基础护甲值减少
    private static final float ADDITIONAL_ARMOR_REDUCTION_PER_LEVEL = -0.10f; // 每级额外护甲值减少

    private static final float BASE_HUNGER_DEPLETION = 1.0f; // 基础饱食度减少
    private static final float ADDITIONAL_HUNGER_DEPLETION_PER_LEVEL = 1.0f; // 每级额外饱食度减少
    private static final int HUNGER_INTERVAL = 50; // 饱食度扣除间隔

    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>(); // 上一次应用的放大等级
    private static final Map<UUID, Integer> damageTickCounterMap = new HashMap<>(); // 伤害间隔计数器
    private static final Map<UUID, Integer> hungerTickCounterMap = new HashMap<>(); // 饱食度间隔计数器

    public Radiation(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

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
        MobEffectInstance effect = entity.getEffect(FELEffects.RADIATION.get());

        if (effect == null) {
            clearAttributes(entity);
            lastAmplifierMap.remove(entityId);
            damageTickCounterMap.remove(entityId);
            hungerTickCounterMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        if (lastAmplifier == null || lastAmplifier != amplifier) {
            float armorReduction = BASE_ARMOR_REDUCTION + (amplifier * ADDITIONAL_ARMOR_REDUCTION_PER_LEVEL);
            applyAttributes(entity, armorReduction);
            lastAmplifierMap.put(entityId, amplifier);
        }

        // ========== 伤害逻辑 ==========
        int damageTick = damageTickCounterMap.getOrDefault(entityId, 0);
        damageTick++;
        if (damageTick >= DAMAGE_INTERVAL) {
            float damage = BASE_DAMAGE + (amplifier * ADDITIONAL_DAMAGE_PER_LEVEL);
            DamageSource radiationDamage = new DamageSource(
                    entity.level().registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(FELDamageTypes.RADIATION)
            );
            entity.hurt(radiationDamage, damage);
            damageTick = 0;
        }
        damageTickCounterMap.put(entityId, damageTick);

        // ========== 饱食度逻辑 ==========
        if (entity instanceof Player player) {
            int hungerTick = hungerTickCounterMap.getOrDefault(entityId, 0);
            hungerTick++;
            if (hungerTick >= HUNGER_INTERVAL) {
                float hungerDepletionFloat = BASE_HUNGER_DEPLETION + (amplifier * ADDITIONAL_HUNGER_DEPLETION_PER_LEVEL);
                int hungerDepletion = Math.round(hungerDepletionFloat);
                int newFoodLevel = Math.max(0, player.getFoodData().getFoodLevel() - hungerDepletion);
                player.getFoodData().setFoodLevel(newFoodLevel);
                hungerTick = 0;
            }
            hungerTickCounterMap.put(entityId, hungerTick);
        }
    }

    private static void applyAttributes(LivingEntity entity, float armorReduction) {
        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR_MODIFIER_UUID);
            armor.addTransientModifier(new AttributeModifier(
                    ARMOR_MODIFIER_UUID,
                    ARMOR_MODIFIER_STRING,
                    armorReduction,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    private static void clearAttributes(LivingEntity entity) {
        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR_MODIFIER_UUID);
        }
    }
}