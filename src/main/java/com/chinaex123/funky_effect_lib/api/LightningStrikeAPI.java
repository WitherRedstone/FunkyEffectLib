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

/** 闪电打击公共 API 类 **/
public class LightningStrikeAPI {

    /** 默认闪电造成的伤害值 **/
    public static final float DEFAULT_DAMAGE = 5.0f;

    /** 在目标实体位置召唤一道闪电（使用默认伤害） **/
    public static LightningBolt strike(LivingEntity target, LivingEntity attacker) {
        return strike(target, attacker, DEFAULT_DAMAGE, true, false);
    }

    /** 在目标实体位置召唤一道闪电（自定义伤害） **/
    public static LightningBolt strike(LivingEntity target, LivingEntity attacker, float damage) {
        return strike(target, attacker, damage, true, false);
    }

    /** 在目标实体位置召唤一道闪电（自定义伤害和是否播放声音） **/
    public static LightningBolt strike(LivingEntity target, LivingEntity attacker, float damage, boolean playSound) {
        return strike(target, attacker, damage, playSound, false);
    }

    /** 在目标实体位置召唤一道闪电（自定义伤害、是否播放声音、是否产生火焰） **/
    public static LightningBolt strike(LivingEntity target, LivingEntity attacker, float damage, boolean playSound, boolean hasFire) {
        if (target.level().isClientSide()) {
            return null;
        }

        BlockPos pos = target.blockPosition();
        EntityType<LightningBolt> lightningType = EntityType.LIGHTNING_BOLT;
        LightningBolt lightning = lightningType.create(target.level());

        if (lightning != null) {
            lightning.moveTo(Vec3.atBottomCenterOf(pos));
            lightning.setVisualOnly(!hasFire);
            if (attacker instanceof ServerPlayer) {
                lightning.setCause((ServerPlayer) attacker);
            }
            target.level().addFreshEntity(lightning);

            target.hurt(target.level().damageSources().lightningBolt(), damage);

            if (playSound) {
                target.level().playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.MASTER, 1.0f, 1.0f);
                target.level().playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT,
                        SoundSource.MASTER, 1.0f, 1.0f);
            }
        }

        return lightning;
    }

    /** 在指定位置召唤一道闪电（使用默认伤害） **/
    public static LightningBolt strikeAtPosition(Level level, BlockPos pos, LivingEntity attacker) {
        return strikeAtPosition(level, pos, attacker, DEFAULT_DAMAGE, true, false);
    }

    /** 在指定位置召唤一道闪电（自定义伤害） **/
    public static LightningBolt strikeAtPosition(Level level, BlockPos pos, LivingEntity attacker, float damage) {
        return strikeAtPosition(level, pos, attacker, damage, true, false);
    }

    /** 在指定位置召唤一道闪电（自定义伤害和是否播放声音） **/
    public static LightningBolt strikeAtPosition(Level level, BlockPos pos, LivingEntity attacker, float damage, boolean playSound) {
        return strikeAtPosition(level, pos, attacker, damage, playSound, false);
    }

    /** 在指定位置召唤一道闪电（自定义伤害、是否播放声音、是否产生火焰） **/
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