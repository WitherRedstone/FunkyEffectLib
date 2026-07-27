package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 晶化护盾：受到伤害时，有概率将部分伤害转化为经验值消耗
 * <p>
 * 机制：
 * <ol>
 *   <li>基础转换概率50%，每级增加5%</li>
 *   <li>基础转换比例50%，每级增加5%</li>
 *   <li>每点伤害消耗10点经验值</li>
 *   <li>经验值不足时不触发转换</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class CrystalShield extends MobEffect {

    /** 基础转换概率 **/
    private static final float CONVERSION_CHANCE = 0.5f;
    /** 基础转换比例 **/
    private static final float CONVERSION_RATIO = 0.5f;
    /** 每级增加的转换概率 **/
    private static final float CHANCE_PER_LEVEL = 0.05f;
    /** 每级增加的转换比例 **/
    private static final float RATIO_PER_LEVEL = 0.05f;
    /** 每点伤害消耗的经验值 **/
    private static final int XP_PER_DAMAGE = 10;

    public CrystalShield(int color) {
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
     * 实体受伤事件处理
     * 有概率将部分伤害转化为经验值消耗
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();

        // 仅对玩家有效
        if (!(entity instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        var effect = player.getEffect(FELEffects.CRYSTAL_SHIELD);
        if (effect == null) {
            return;
        }

        int amplifier = effect.getAmplifier();
        // 计算转换概率：基础 + 等级 × 每级加成
        float conversionChance = CONVERSION_CHANCE + (amplifier * CHANCE_PER_LEVEL);
        // 计算转换比例：基础 + 等级 × 每级加成（上限90%）
        float conversionRatio = CONVERSION_RATIO + (amplifier * RATIO_PER_LEVEL);
        conversionRatio = Math.min(conversionRatio, 0.9f);

        // 判定是否触发转换
        if (player.getRandom().nextFloat() < conversionChance) {
            float originalDamage = event.getOriginalDamage();
            float convertedDamage = originalDamage * conversionRatio;  // 被转换的伤害
            float remainingDamage = originalDamage - convertedDamage;  // 实际承受的伤害

            // 计算需要消耗的经验值
            int xpCost = (int) Math.ceil(convertedDamage * XP_PER_DAMAGE);
            int currentXp = getPlayerTotalExperience(player);

            // 经验值充足时进行转换
            if (currentXp >= xpCost) {
                giveExperience(player, -xpCost);
                event.setNewDamage(remainingDamage);
            }
        }
    }

    /**
     * 获取玩家总经验值
     *
     * @param player 玩家对象
     * @return 总经验值
     */
    private static int getPlayerTotalExperience(Player player) {
        return player.totalExperience;
    }

    /**
     * 给予或扣除玩家经验值
     *
     * @param player 玩家对象
     * @param amount 经验值数量（正数增加，负数扣除）
     */
    private static void giveExperience(Player player, int amount) {
        player.giveExperiencePoints(amount);
    }
}