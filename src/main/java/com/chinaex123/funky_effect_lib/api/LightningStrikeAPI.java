package com.chinaex123.funky_effect_lib.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 闪电打击公共 API 类
 * <p>
 * 提供统一的闪电召唤接口，支持自定义伤害、音效和火焰效果
 */
public class LightningStrikeAPI {

    /** 默认闪电造成的伤害值（5点） **/
    public static final float DEFAULT_DAMAGE = 5.0f;

    /**
     * 在目标实体位置召唤一道闪电（使用默认伤害）
     *
     * @param target 闪电击中的目标实体
     * @param attacker 闪电的施法者（用于伤害来源判定）
     * @return 生成的闪电实体，客户端调用返回null
     */
    public static LightningBolt strike(LivingEntity target, LivingEntity attacker) {
        return strike(target, attacker, DEFAULT_DAMAGE, true, false);
    }

    /**
     * 在目标实体位置召唤一道闪电（自定义伤害）
     *
     * @param target 闪电击中的目标实体
     * @param attacker 闪电的施法者
     * @param damage 闪电造成的伤害值
     * @return 生成的闪电实体，客户端调用返回null
     */
    public static LightningBolt strike(LivingEntity target, LivingEntity attacker, float damage) {
        return strike(target, attacker, damage, true, false);
    }

    /**
     * 在目标实体位置召唤一道闪电（自定义伤害和是否播放声音）
     *
     * @param target 闪电击中的目标实体
     * @param attacker 闪电的施法者
     * @param damage 闪电造成的伤害值
     * @param playSound 是否播放雷声和冲击音效
     * @return 生成的闪电实体，客户端调用返回null
     */
    public static LightningBolt strike(LivingEntity target, LivingEntity attacker, float damage, boolean playSound) {
        return strike(target, attacker, damage, playSound, false);
    }

    /**
     * 在目标实体位置召唤一道闪电（全参数版本）
     *
     * @param target 闪电击中的目标实体
     * @param attacker 闪电的施法者
     * @param damage 闪电造成的伤害值
     * @param playSound 是否播放雷声和冲击音效
     * @param hasFire 是否在闪电击中处产生火焰（视觉效果）
     * @return 生成的闪电实体，客户端调用返回null
     */
    public static LightningBolt strike(LivingEntity target, LivingEntity attacker, float damage, boolean playSound, boolean hasFire) {
        // 仅在服务端执行，客户端不生成闪电
        if (target.level().isClientSide()) {
            return null;
        }

        BlockPos pos = target.blockPosition();
        EntityType<LightningBolt> lightningType = EntityType.LIGHTNING_BOLT;
        LightningBolt lightning = lightningType.create(target.level());

        if (lightning != null) {
            // 设置闪电位置
            lightning.moveTo(Vec3.atBottomCenterOf(pos));
            // setVisualOnly(!hasFire)：如果hasFire为false，闪电仅为视觉效果不产生火焰
            lightning.setVisualOnly(!hasFire);
            // 设置施法者（用于伤害来源判定）
            if (attacker instanceof ServerPlayer) {
                lightning.setCause((ServerPlayer) attacker);
            }
            // 将闪电添加到世界
            target.level().addFreshEntity(lightning);

            // 对目标造成闪电伤害
            target.hurt(target.level().damageSources().lightningBolt(), damage);

            // 播放音效
            if (playSound) {
                target.level().playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.MASTER, 1.0f, 1.0f);
                target.level().playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT,
                        SoundSource.MASTER, 1.0f, 1.0f);
            }
        }

        return lightning;
    }

    /**
     * 在指定位置召唤一道闪电（使用默认伤害）
     *
     * @param level 世界对象
     * @param pos 闪电位置
     * @param attacker 闪电的施法者
     * @return 生成的闪电实体，客户端调用返回null
     */
    public static LightningBolt strikeAtPosition(Level level, BlockPos pos, LivingEntity attacker) {
        return strikeAtPosition(level, pos, attacker, DEFAULT_DAMAGE, true, false);
    }

    /**
     * 在指定位置召唤一道闪电（自定义伤害）
     *
     * @param level 世界对象
     * @param pos 闪电位置
     * @param attacker 闪电的施法者
     * @param damage 闪电造成的伤害值
     * @return 生成的闪电实体，客户端调用返回null
     */
    public static LightningBolt strikeAtPosition(Level level, BlockPos pos, LivingEntity attacker, float damage) {
        return strikeAtPosition(level, pos, attacker, damage, true, false);
    }

    /**
     * 在指定位置召唤一道闪电（自定义伤害和是否播放声音）
     *
     * @param level 世界对象
     * @param pos 闪电位置
     * @param attacker 闪电的施法者
     * @param damage 闪电造成的伤害值
     * @param playSound 是否播放雷声和冲击音效
     * @return 生成的闪电实体，客户端调用返回null
     */
    public static LightningBolt strikeAtPosition(Level level, BlockPos pos, LivingEntity attacker, float damage, boolean playSound) {
        return strikeAtPosition(level, pos, attacker, damage, playSound, false);
    }

    /**
     * 在指定位置召唤一道闪电（全参数版本）
     * 与 strike 方法的区别在于：strikeAtPosition 不自动对目标造成伤害
     *
     * @param level 世界对象
     * @param pos 闪电位置
     * @param attacker 闪电的施法者
     * @param damage 闪电造成的伤害值（此版本暂不应用，保留参数供扩展）
     * @param playSound 是否播放雷声和冲击音效
     * @param hasFire 是否在闪电击中处产生火焰（视觉效果）
     * @return 生成的闪电实体，客户端调用返回null
     */
    public static LightningBolt strikeAtPosition(Level level, BlockPos pos, LivingEntity attacker, float damage, boolean playSound, boolean hasFire) {
        if (level.isClientSide) {
            return null;
        }

        EntityType<LightningBolt> lightningType = EntityType.LIGHTNING_BOLT;
        LightningBolt lightning = lightningType.create(level);

        if (lightning != null) {
            lightning.moveTo(Vec3.atBottomCenterOf(pos));
            lightning.setVisualOnly(!hasFire);
            if (attacker instanceof ServerPlayer) {
                lightning.setCause((ServerPlayer) attacker);
            }
            level.addFreshEntity(lightning);
        }

        if (playSound) {
            level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER,
                    SoundSource.MASTER, 1.0f, 1.0f);
            level.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT,
                    SoundSource.MASTER, 1.0f, 1.0f);
        }

        return lightning;
    }
}