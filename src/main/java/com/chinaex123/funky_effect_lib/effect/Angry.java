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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 愤怒：增加攻击伤害和攻击速度，同时额外消耗饥饿值
 * <p>
 * 机制：
 * <ol>
 *   <li>基础攻击伤害增加15%，每级增加10%</li>
 *   <li>基础攻击速度增加15%，每级增加10%</li>
 *   <li>每2.5秒（50刻）额外消耗1点饥饿值</li>
 *   <li>属性修改随等级变化自动更新</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Angry extends MobEffect {

    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("a231c78c-928a-4d18-b74c-68993ca90835");
    private static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("b6522240-1224-47b4-8ca4-398e9fa43afc");
    private static final String ATTACK_DAMAGE_MODIFIER_STRING = UUID.nameUUIDFromBytes("angry_damage".getBytes()).toString();
    private static final String ATTACK_SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("angry_speed".getBytes()).toString();

    /** 基础攻击伤害增加 **/
    private static final float BASE_DAMAGE_INCREASE = 0.15f;
    /** 每级额外增加的攻击伤害 **/
    private static final float ADDITIONAL_DAMAGE_PER_LEVEL = 0.10f;
    /** 基础攻击速度增加 **/
    private static final float BASE_SPEED_INCREASE = 0.15f;
    /** 每级额外增加的攻击速度 **/
    private static final float ADDITIONAL_SPEED_PER_LEVEL = 0.10f;

    /** 消耗饥饿值的间隔 **/
    private static final int HUNGER_COST_INTERVAL = 50;

    /** 缓存每个玩家上次应用的附魔等级 **/
    private static final Map<UUID, Integer> lastAmplifierMap = new HashMap<>();
    /** 缓存每个玩家的Tick计数器 **/
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();

    public Angry(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家Tick事件处理
     * 管理愤怒效果的属性修改和饥饿值消耗
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide()) {
            return;
        }

        UUID entityId = player.getUUID();
        MobEffectInstance effect = player.getEffect(FELEffects.ANGRY.get());

        if (effect == null) {
            // 效果已消失，清除所有修改
            clearAttributes(player);
            lastAmplifierMap.remove(entityId);
            tickCounterMap.remove(entityId);
            return;
        }

        int amplifier = effect.getAmplifier();
        Integer lastAmplifier = lastAmplifierMap.get(entityId);

        // 如果等级发生变化，重新计算属性修改
        if (lastAmplifier == null || lastAmplifier != amplifier) {
            float damageIncrease = BASE_DAMAGE_INCREASE + (amplifier * ADDITIONAL_DAMAGE_PER_LEVEL);
            float speedIncrease = BASE_SPEED_INCREASE + (amplifier * ADDITIONAL_SPEED_PER_LEVEL);
            applyAttributes(player, damageIncrease, speedIncrease);
            lastAmplifierMap.put(entityId, amplifier);
        }

        // 饥饿值消耗计时
        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        if (tickCounter >= HUNGER_COST_INTERVAL) {
            // 减少1点饥饿值，最低为0
            player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - 1));
            tickCounter = 0;
        }

        tickCounterMap.put(entityId, tickCounter);
    }

    /**
     * 应用属性修改
     *
     * @param player 玩家对象
     * @param damageIncrease 攻击伤害增加量
     * @param speedIncrease 攻击速度增加量
     */
    private static void applyAttributes(Player player, float damageIncrease, float speedIncrease) {
        // 修改攻击伤害
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

        // 修改攻击速度
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_UUID);
            attackSpeed.addTransientModifier(new AttributeModifier(
                    ATTACK_SPEED_MODIFIER_UUID,
                    ATTACK_SPEED_MODIFIER_STRING,
                    speedIncrease,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    /**
     * 清除所有属性修改
     *
     * @param player 玩家对象
     */
    private static void clearAttributes(Player player) {
        // 清除攻击伤害修改
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
        }

        // 清除攻击速度修改
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_UUID);
        }
    }
}