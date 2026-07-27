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

/**
 * 战意沸腾：每次攻击提升攻击力，若3秒未攻击则重置效果
 * <p>
 * 机制：
 * <ol>
 *   <li>每次成功攻击（造成伤害）时，攻击力增加1%</li>
 *   <li>攻击力加成上限为200%（最多叠加200次）</li>
 *   <li>若3秒（60刻）内未造成任何伤害，攻击力加成重置为0</li>
 *   <li>效果消失时自动清除所有加成</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class BattleFrenzy extends MobEffect {

    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("bc5096c6-4a31-4f0c-9356-66a0174ac362");
    private static final String ATTACK_DAMAGE_MODIFIER_STRING = UUID.nameUUIDFromBytes("battle_frenzy_damage".getBytes()).toString();

    /** 每次攻击增加的攻击力比例 **/
    private static final float DAMAGE_INCREASE_PER_ATTACK = 0.01f;
    /** 最大攻击力加成 **/
    private static final float MAX_DAMAGE_INCREASE = 2.0f;
    /** 重置间隔 **/
    private static final int RESET_TICKS = 60;

    /** 缓存每个玩家的攻击次数 **/
    private static final Map<UUID, Integer> attackCountMap = new HashMap<>();
    /** 缓存每个玩家的上次攻击时间 **/
    private static final Map<UUID, Long> lastAttackTimeMap = new HashMap<>();
    /** 缓存每个玩家的当前攻击力加成 **/
    private static final Map<UUID, Float> currentDamageMap = new HashMap<>();

    public BattleFrenzy(int color) {
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
     * 当玩家造成伤害时，增加攻击力加成
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        // 检查伤害来源是否为玩家
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // 仅在服务端执行
        if (player.level().isClientSide()) {
            return;
        }

        // 检查玩家是否拥有战意沸腾效果
        if (!player.hasEffect(FELEffects.BATTLE_FRENZY.get())) {
            return;
        }

        UUID playerId = player.getUUID();
        long currentTime = player.level().getGameTime();

        // 增加攻击次数
        int attackCount = attackCountMap.getOrDefault(playerId, 0) + 1;
        attackCountMap.put(playerId, attackCount);
        lastAttackTimeMap.put(playerId, currentTime);

        // 计算攻击力加成
        float damageIncrease = Math.min(attackCount * DAMAGE_INCREASE_PER_ATTACK, MAX_DAMAGE_INCREASE);
        currentDamageMap.put(playerId, damageIncrease);

        // 应用攻击力加成
        applyAttributes(player, damageIncrease);
    }

    /**
     * 玩家Tick事件处理
     * 检查是否超过3秒未攻击，若是则重置加成
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

        MobEffectInstance effect = player.getEffect(FELEffects.BATTLE_FRENZY.get());

        // 如果玩家没有战意沸腾效果，清除所有数据
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

        // 如果超过3秒未攻击，重置加成
        if (currentTime - lastAttackTime >= RESET_TICKS) {
            attackCountMap.put(playerId, 0);
            currentDamageMap.put(playerId, 0.0f);
            clearAttributes(player);
        }
    }

    /**
     * 应用攻击力加成
     *
     * @param player 玩家对象
     * @param damageIncrease 攻击力加成量
     */
    private static void applyAttributes(Player player, float damageIncrease) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            // 移除旧的修改器，避免重复叠加
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
            // 添加新的修改器
            attackDamage.addTransientModifier(new AttributeModifier(
                    ATTACK_DAMAGE_MODIFIER_UUID,
                    ATTACK_DAMAGE_MODIFIER_STRING,
                    damageIncrease,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    /**
     * 清除攻击力加成
     *
     * @param player 玩家对象
     */
    private static void clearAttributes(Player player) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
        }
    }
}