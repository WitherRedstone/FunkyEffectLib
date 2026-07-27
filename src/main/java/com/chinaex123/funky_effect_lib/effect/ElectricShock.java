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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 触电：降低移动速度并持续造成伤害
 * <p>
 * 机制：
 * <ol>
 *   <li>基础移动速度减少15%，每级额外减少10%</li>
 *   <li>基础伤害为2点，每级增加2点</li>
 *   <li>每1秒（20刻）造成一次伤害</li>
 *   <li>伤害类型为自定义的ELECTRIC_SHOCK（电击）</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class ElectricShock extends MobEffect {

    private static final UUID MOVEMENT_SPEED_MODIFIER_UUID = UUID.fromString("225975ec-ebd2-4f92-aa35-159e7ea1532e");
    private static final String MOVEMENT_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("electric_shock_speed".getBytes()).toString();

    /** 基础移动速度减少 **/
    private static final float BASE_SPEED_REDUCTION = -0.15f;
    /** 每级额外移动速度减少 **/
    private static final float ADDITIONAL_REDUCTION_PER_LEVEL = -0.1f;
    /** 基础伤害 **/
    private static final float BASE_DAMAGE = 2.0f;
    /** 每级额外伤害 **/
    private static final float ADDITIONAL_DAMAGE_PER_LEVEL = 2.0f;
    /** 伤害间隔 **/
    private static final int DAMAGE_INTERVAL = 20;

    /** 缓存每个实体上次应用的等级 **/
    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>();
    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();

    public ElectricShock(int color) {
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
     * 管理减速效果和周期性伤害
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onLivingTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (entity.level().isClientSide()) {
            return;
        }

        UUID entityId = entity.getUUID();
        MobEffectInstance effect = entity.getEffect(FELEffects.ELECTRIC_SHOCK.get());

        // 如果效果消失，清除所有数据
        if (effect == null) {
            clearAttributes(entity);
            lastAmplifierMap.remove(entityId);
            tickCounterMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        // 如果等级发生变化，重新应用减速效果
        if (lastAmplifier == null || lastAmplifier != amplifier) {
            float speedReduction = BASE_SPEED_REDUCTION + (amplifier * ADDITIONAL_REDUCTION_PER_LEVEL);
            applyAttributes(entity, speedReduction);
            lastAmplifierMap.put(entityId, amplifier);
        }

        // 伤害计时器
        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        // 达到间隔时间，造成伤害
        if (tickCounter >= DAMAGE_INTERVAL) {
            // 计算伤害：基础 + 等级 × 每级加成
            float damage = BASE_DAMAGE + (amplifier * ADDITIONAL_DAMAGE_PER_LEVEL);
            // 创建电击伤害源
            DamageSource electricDamage = new DamageSource(
                    entity.level().registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(FELDamageTypes.ELECTRIC_SHOCK)
            );
            entity.hurt(electricDamage, damage);
            tickCounter = 0;
        }

        tickCounterMap.put(entityId, tickCounter);
    }

    /**
     * 应用移动速度减速效果
     *
     * @param entity 目标实体
     * @param speedReduction 速度减少量（负值）
     */
    private static void applyAttributes(LivingEntity entity, float speedReduction) {
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER_UUID);
            movementSpeed.addTransientModifier(new AttributeModifier(
                    MOVEMENT_SPEED_MODIFIER_UUID,
                    MOVEMENT_SPEED_MODIFIER_STRING,
                    speedReduction,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    /**
     * 清除移动速度减速效果
     *
     * @param entity 目标实体
     */
    private static void clearAttributes(LivingEntity entity) {
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(MOVEMENT_SPEED_MODIFIER_UUID);
        }
    }
}