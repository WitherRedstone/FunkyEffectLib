package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 贪婪：拾取经验球时短暂提升攻击力
 * <p>
 * 机制：
 * <ol>
 *   <li>拾取经验球时触发攻击力提升效果</li>
 *   <li>基础攻击力提升5%，每级增加5%</li>
 *   <li>效果持续240刻（12秒）</li>
 *   <li>拾取多个经验球会刷新持续时间</li>
 *   <li>效果消失后攻击力恢复</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Avarice extends MobEffect {

    private static final ResourceLocation ATTACK_DAMAGE_BOOST_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "avarice_attack_boost");

    /** 基础攻击力提升 **/
    private static final float BASE_ATTACK_BOOST = 0.05f;
    /** 每级额外攻击力提升 **/
    private static final float ADDITIONAL_BOOST_PER_LEVEL = 0.05f;
    /** 攻击力提升持续时间 **/
    private static final int BOOST_DURATION_TICKS = 240;

    /** 缓存每个玩家的攻击力提升计时器 **/
    private static final Map<UUID, Integer> boostTimerMap = new HashMap<>();

    public Avarice(int color) {
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

    /**
     * 实体Tick事件处理
     * 管理攻击力提升的计时和清除
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        // 仅处理玩家实体
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // 仅在服务端执行
        if (player.level().isClientSide()) {
            return;
        }

        UUID playerId = player.getUUID();
        var effect = player.getEffect(FELEffects.AVARICE);

        // 如果玩家没有贪婪效果，清除攻击力提升
        if (effect == null) {
            if (boostTimerMap.containsKey(playerId)) {
                clearAttackBoost(player);
                boostTimerMap.remove(playerId);
            }
            return;
        }

        // 更新计时器
        Integer boostTimer = boostTimerMap.get(playerId);
        if (boostTimer != null && boostTimer > 0) {
            boostTimer--;
            if (boostTimer <= 0) {
                // 计时结束，清除攻击力提升
                clearAttackBoost(player);
                boostTimerMap.remove(playerId);
            } else {
                boostTimerMap.put(playerId, boostTimer);
            }
        }
    }

    /**
     * 经验球拾取事件处理
     * 拾取经验球时触发攻击力提升
     *
     * @param event 经验球拾取事件
     */
    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();

        // 检查玩家是否拥有贪婪效果
        var effect = player.getEffect(FELEffects.AVARICE);
        if (effect == null) {
            return;
        }

        // 根据等级计算攻击力提升量
        int amplifier = effect.getAmplifier();
        float boostAmount = BASE_ATTACK_BOOST + (amplifier * ADDITIONAL_BOOST_PER_LEVEL);

        // 重置计时器（刷新持续时间）
        boostTimerMap.put(playerId, BOOST_DURATION_TICKS);
        // 应用攻击力提升
        applyAttackBoost(player, boostAmount);
    }

    /**
     * 应用攻击力提升
     *
     * @param player 玩家对象
     * @param boostAmount 攻击力提升量
     */
    private static void applyAttackBoost(Player player, float boostAmount) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            // 移除旧的修改器，避免重复叠加
            attackDamage.removeModifier(ATTACK_DAMAGE_BOOST_MODIFIER);
            AttributeModifier modifier = new AttributeModifier(
                    ATTACK_DAMAGE_BOOST_MODIFIER,
                    boostAmount,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );
            attackDamage.addTransientModifier(modifier);
        }
    }

    /**
     * 清除攻击力提升
     *
     * @param player 玩家对象
     */
    private static void clearAttackBoost(Player player) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(ATTACK_DAMAGE_BOOST_MODIFIER);
        }
    }
}