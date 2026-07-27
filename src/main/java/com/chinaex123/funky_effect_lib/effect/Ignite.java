package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.init.FELSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 点燃：瞬间造成火焰内爆伤害
 * <p>
 * 机制：
 * <ol>
 *   <li>基础伤害10点，每级增加5点</li>
 *   <li>造成伤害后使目标燃烧100刻（5秒）</li>
 *   <li>播放爆炸音效和大量粒子效果</li>
 *   <li>效果触发后自动移除</li>
 *   <li>使用damagedMap防止重复触发伤害</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Ignite extends MobEffect {

    /** 基础伤害 **/
    private static final float BASE_DAMAGE = 10.0f;
    /** 每级额外伤害 **/
    private static final float DAMAGE_PER_LEVEL = 5.0f;
    /** 火焰持续时间 **/
    private static final int FIRE_DURATION = 100;

    /** 金色粒子效果 **/
    private static final DustParticleOptions GOLDEN_DUST = new DustParticleOptions(
            new Vector3f(1.0f, 0.8f, 0.0f), 1.5f
    );
    /** 亮金色粒子效果 **/
    private static final DustParticleOptions BRIGHT_GOLDEN_DUST = new DustParticleOptions(
            new Vector3f(1.0f, 0.9f, 0.2f), 2.0f
    );

    /** 缓存已受到伤害的实体UUID **/
    private static final Map<UUID, Boolean> damagedMap = new HashMap<>();

    public Ignite(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 仅在服务端执行
        if (!entity.level().isClientSide()) {
            UUID entityId = entity.getUUID();
            // 防止重复触发伤害
            if (!damagedMap.containsKey(entityId)) {
                // 造成伤害
                dealDamage(entity, amplifier);
                damagedMap.put(entityId, true);
                // 效果触发后移除自身
                entity.removeEffect(FELEffects.IGNITE);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 造成伤害并播放视觉效果
     *
     * @param entity 目标实体
     * @param amplifier 效果等级
     */
    private static void dealDamage(LivingEntity entity, int amplifier) {
        // 计算伤害：基础 + 等级 × 每级加成
        float damage = BASE_DAMAGE + (DAMAGE_PER_LEVEL * amplifier);

        // 造成火焰伤害
        entity.hurt(entity.damageSources().onFire(), damage);
        // 使目标燃烧
        entity.setRemainingFireTicks(FIRE_DURATION);

        // 播放爆炸音效
        entity.level().playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                FELSounds.IGNITE_EXPLODE.get(),
                SoundSource.HOSTILE,
                1.5f,
                1.0f
        );

        // 生成粒子效果
        if (entity.level() instanceof ServerLevel serverLevel) {
            // 中心金色粒子
            serverLevel.sendParticles(GOLDEN_DUST,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    5, 0, 0, 0, 0);

            // 爆炸粒子
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    1, 0, 0, 0, 0.1f);

            // 环形金色粒子
            for (int i = 0; i < 30; i++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = 0.8 + Math.random() * 0.5;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double offsetY = (Math.random() - 0.5);

                serverLevel.sendParticles(GOLDEN_DUST,
                        entity.getX() + offsetX,
                        entity.getY() + entity.getBbHeight() / 2 + offsetY,
                        entity.getZ() + offsetZ,
                        1, 0, 0, 0, 0);
            }

            // 火焰粒子
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    15, 0.5, 0.5, 0.5, 0.05);

            // 亮金色粒子
            serverLevel.sendParticles(BRIGHT_GOLDEN_DUST,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    10, 0.3, 0.3, 0.3, 0.02);
        }
    }

    /**
     * 实体Tick事件处理
     * 清理damagedMap中的记录
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide()) {
            // 如果实体不再拥有点燃效果，清理记录
            if (!entity.hasEffect(FELEffects.IGNITE)) {
                damagedMap.remove(entity.getUUID());
            }
        }
    }
}