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

/**
 * 辐射：每秒造成伤害并快速降低饱食度和护甲值
 * <p>
 * 机制：
 * <ol>
 *   <li>基础伤害1点，每级增加1点，每3.5秒（70刻）造成一次伤害</li>
 *   <li>基础护甲减少20%，每级额外减少10%</li>
 *   <li>基础饱食度减少1点，每级增加1点，每2.5秒（50刻）扣除一次</li>
 *   <li>效果消失时恢复护甲值</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Radiation extends MobEffect {

    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("F6A7B8C9-0D1E-4F2A-3B4C-5D6E7F8A9B0C");
    private static final String ARMOR_MODIFIER_STRING = UUID.nameUUIDFromBytes("radiation_armor".getBytes()).toString();

    /** 基础伤害 **/
    private static final float BASE_DAMAGE = 1.0f;
    /** 每级额外伤害 **/
    private static final float ADDITIONAL_DAMAGE_PER_LEVEL = 1.0f;
    /** 伤害间隔 **/
    private static final int DAMAGE_INTERVAL = 70;

    /** 基础护甲值减少 **/
    private static final float BASE_ARMOR_REDUCTION = -0.20f;
    /** 每级额外护甲值减少 **/
    private static final float ADDITIONAL_ARMOR_REDUCTION_PER_LEVEL = -0.10f;

    /** 基础饱食度减少 **/
    private static final float BASE_HUNGER_DEPLETION = 1.0f;
    /** 每级额外饱食度减少 **/
    private static final float ADDITIONAL_HUNGER_DEPLETION_PER_LEVEL = 1.0f;
    /** 饱食度扣除间隔 **/
    private static final int HUNGER_INTERVAL = 50;

    /** 缓存每个实体上次应用的等级 **/
    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>();
    /** 缓存每个实体的伤害间隔计数器 **/
    private static final Map<UUID, Integer> damageTickCounterMap = new HashMap<>();
    /** 缓存每个实体的饱食度间隔计数器 **/
    private static final Map<UUID, Integer> hungerTickCounterMap = new HashMap<>();

    public Radiation(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家Tick事件处理
     * 管理辐射效果的伤害、护甲减少和饱食度消耗
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
        MobEffectInstance effect = entity.getEffect(FELEffects.RADIATION.get());

        // 如果效果消失，清除所有数据
        if (effect == null) {
            clearAttributes(entity);
            lastAmplifierMap.remove(entityId);
            damageTickCounterMap.remove(entityId);
            hungerTickCounterMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        // 如果等级发生变化，重新应用护甲减少效果
        if (lastAmplifier == null || lastAmplifier != amplifier) {
            float armorReduction = BASE_ARMOR_REDUCTION + (amplifier * ADDITIONAL_ARMOR_REDUCTION_PER_LEVEL);
            applyAttributes(entity, armorReduction);
            lastAmplifierMap.put(entityId, amplifier);
        }

        // 伤害逻辑
        int damageTick = damageTickCounterMap.getOrDefault(entityId, 0);
        damageTick++;
        if (damageTick >= DAMAGE_INTERVAL) {
            // 计算伤害：基础 + 等级 × 每级加成
            float damage = BASE_DAMAGE + (amplifier * ADDITIONAL_DAMAGE_PER_LEVEL);
            // 创建辐射伤害源
            DamageSource radiationDamage = new DamageSource(
                    entity.level().registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(FELDamageTypes.RADIATION)
            );
            entity.hurt(radiationDamage, damage);
            damageTick = 0;
        }
        damageTickCounterMap.put(entityId, damageTick);

        // 饱食度逻辑
        if (entity instanceof Player player) {
            int hungerTick = hungerTickCounterMap.getOrDefault(entityId, 0);
            hungerTick++;
            if (hungerTick >= HUNGER_INTERVAL) {
                // 计算饱食度消耗量：基础 + 等级 × 每级加成（四舍五入）
                float hungerDepletionFloat = BASE_HUNGER_DEPLETION + (amplifier * ADDITIONAL_HUNGER_DEPLETION_PER_LEVEL);
                int hungerDepletion = Math.round(hungerDepletionFloat);
                // 减少饱食度，最低为0
                int newFoodLevel = Math.max(0, player.getFoodData().getFoodLevel() - hungerDepletion);
                player.getFoodData().setFoodLevel(newFoodLevel);
                hungerTick = 0;
            }
            hungerTickCounterMap.put(entityId, hungerTick);
        }
    }

    /**
     * 应用护甲减少效果
     *
     * @param entity 目标实体
     * @param armorReduction 护甲减少量（负值）
     */
    private static void applyAttributes(LivingEntity entity, float armorReduction) {
        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            // 移除旧的修改器
            armor.removeModifier(ARMOR_MODIFIER_UUID);
            // 添加新的修改器
            armor.addTransientModifier(new AttributeModifier(
                    ARMOR_MODIFIER_UUID,
                    ARMOR_MODIFIER_STRING,
                    armorReduction,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    /**
     * 清除护甲减少效果
     *
     * @param entity 目标实体
     */
    private static void clearAttributes(LivingEntity entity) {
        AttributeInstance armor = entity.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR_MODIFIER_UUID);
        }
    }
}