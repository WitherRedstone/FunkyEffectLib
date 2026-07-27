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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 瓦解：大幅降低攻击力
 * <p>
 * 机制：
 * <ol>
 *   <li>基础攻击力降低35%</li>
 *   <li>每级额外降低10%攻击力</li>
 *   <li>效果等级变化时自动更新攻击力降低</li>
 *   <li>效果消失时恢复攻击力</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Sever extends MobEffect {

    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("F8A9B0C1-2D3E-4F4A-5B6C-7D8E9F0A1B2C");
    private static final String ATTACK_DAMAGE_MODIFIER_STRING = UUID.nameUUIDFromBytes("sever_damage".getBytes()).toString();

    /** 基础攻击力降低 **/
    private static final float BASE_ATTACK_DAMAGE_REDUCTION = -0.35f;
    /** 每级额外降低攻击力 **/
    private static final float ADDITIONAL_REDUCTION_PER_LEVEL = -0.10f;

    /** 缓存每个实体上次应用的等级 **/
    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>();

    public Sever(int color) {
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
     * 管理瓦解效果的攻击力降低
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
        MobEffectInstance effect = entity.getEffect(FELEffects.SEVER.get());

        // 如果效果消失，清除攻击力降低
        if (effect == null) {
            clearAttackDamageReduction(entity);
            lastAmplifierMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        // 如果等级发生变化，重新计算攻击力降低
        if (lastAmplifier == null || lastAmplifier != amplifier) {
            // 计算攻击力降低：基础 + 等级 × 每级加成
            float reduction = BASE_ATTACK_DAMAGE_REDUCTION + (amplifier * ADDITIONAL_REDUCTION_PER_LEVEL);
            applyAttackDamageReduction(entity, reduction);
            lastAmplifierMap.put(entityId, amplifier);
        }
    }

    /**
     * 应用攻击力降低
     *
     * @param entity 目标实体
     * @param reduction 攻击力降低量（负值）
     */
    private static void applyAttackDamageReduction(LivingEntity entity, float reduction) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            // 移除旧的修改器，避免重复叠加
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
            // 添加新的修改器
            attackDamage.addTransientModifier(new AttributeModifier(
                    ATTACK_DAMAGE_MODIFIER_UUID,
                    ATTACK_DAMAGE_MODIFIER_STRING,
                    reduction,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    /**
     * 清除攻击力降低
     *
     * @param entity 目标实体
     */
    private static void clearAttackDamageReduction(LivingEntity entity) {
        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
        }
    }
}