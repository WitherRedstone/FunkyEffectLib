package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.api.ThreadlingAPI;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

/**
 * 线虫：生成线虫攻击敌人
 * <p>
 * 该效果为增益效果，当玩家攻击实体时，
 * 根据效果等级召唤对应数量的线虫实体攻击目标。
 * <p>
 * 效果等级与召唤数量的映射关系：
 * - 等级 0 → 1 个 线虫
 * - 等级 1 → 2 个 线虫
 * - 等级 2 → 3 个 线虫
 * - 等级 3 → 4 个 线虫
 * - 等级 4+ → 5 个 线虫
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Threadling extends MobEffect {

    /** 最大有效等级，超过此等级按此等级计算 */
    private static final int MAX_LEVEL = 5;

    /**
     * 构造线虫状态效果
     *
     * @param color 效果颜色值（用于效果图标和粒子颜色）
     */
    public Threadling(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    /**
     * 应用效果刻更新
     * <p>
     * 该效果不需要每刻执行操作，因此留空实现
     *
     * @param entity 拥有该效果的实体
     * @param amplifier 效果等级（从0开始）
     */
    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    /**
     * 判断效果是否应在每刻执行更新
     * <p>
     * 始终返回true，使效果持续生效
     *
     * @param duration 剩余持续时间（刻）
     * @param amplifier 效果等级
     * @return 始终返回true
     */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家攻击事件处理器
     * <p>
     * 当玩家攻击实体时触发，如果玩家拥有线虫效果，
     * 则根据效果等级在目标周围生成对应数量的线虫实体进行攻击
     *
     * @param event 实体攻击事件
     */
    @SubscribeEvent
    public static void onPlayerAttack(LivingAttackEvent event) {
        // 检查攻击者是否为玩家
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // 仅在服务端执行逻辑
        if (player.level().isClientSide()) {
            return;
        }

        // 检查玩家是否拥有线虫效果
        MobEffectInstance effect = player.getEffect(FELEffects.THREADLING.get());
        if (effect == null) {
            return;
        }

        // 获取被攻击的目标实体
        LivingEntity target = event.getEntity();

        // 获取效果等级并限制最大等级
        int amplifier = effect.getAmplifier();
        int effectiveLevel = Math.min(amplifier, MAX_LEVEL);
        // 计算召唤数量
        int threadlingCount = effectiveLevel + 1;

        // 命中后给予目标效果
        ThreadlingAPI.ThreadlingConfig config = ThreadlingAPI.ThreadlingConfig.createFullConfig(
                true, 60, 0,
                true, 8.0F, 15.0F // 随机伤害8-15点
        );
        // 召唤线虫
        ThreadlingAPI.spawnThreadlings(player, target, threadlingCount, true, 60, config);
    }
}