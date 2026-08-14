package com.chinaex123.funky_effect_lib.entity;

import com.chinaex123.funky_effect_lib.api.ThreadlingAPI;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 线虫实体类
 * <p>
 * 一种小型的线状生物实体，由玩家召唤用于攻击目标。
 * 具有延迟攻击机制，会根据生成距离决定是否立即攻击或等待。
 * 攻击一次后自动消失，免疫火焰伤害。
 */
public class Threadling extends Monster {

    /** 召唤者（玩家） */
    private Player owner;
    /** 攻击目标 */
    private LivingEntity target;
    /** 是否已造成伤害（用于防止多次攻击） */
    private boolean hasDealtDamage = false;
    /** 生成时与目标的距离 */
    private double spawnDistance = 0.0;
    /** 攻击延迟刻数（当生成距离大于阈值时等待的刻数） */
    private static final int SPAWN_DELAY = 20;
    /** 最小延迟距离阈值，超过此距离需要延迟攻击 */
    private static final double MIN_DELAY_DISTANCE = 3.0;
    /** 是否应用sever效果 **/
    private boolean applySeverEffect = false;
    /** sever效果持续时间 **/
    private int severDuration = 0;
    /** sever效果等级 **/
    private int severAmplifier = 0;

    /** 伤害配置 **/
    private boolean useRandomDamage = true;
    private float fixedDamage = 7.5F;
    private float minRandomDamage = 5.0F;
    private float maxRandomDamage = 10.0F;

    /**
     * 构造线虫实体
     *
     * @param entityType 实体类型
     * @param level 世界对象
     */
    public Threadling(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        // 设置最大上坡高度，使实体能够爬上低矮方块
        this.setMaxUpStep(0.6F);
        // 击杀后不掉落经验值
        this.xpReward = 0;
    }

    /**
     * 设置sever效果
     *
     * @param duration 持续时间（tick）
     * @param amplifier 效果等级
     */
    public void setSeverEffect(int duration, int amplifier) {
        this.applySeverEffect = true;
        this.severDuration = duration;
        this.severAmplifier = amplifier;
    }

    /**
     * 注册实体的AI目标
     * <p>
     * 设置实体的行为优先级：
     * 1. 近战攻击（最高优先级）
     * 2. 游泳
     * 3. 随机环顾四周
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    /**
     * 设置固定伤害
     *
     * @param damage 固定伤害值
     */
    public void setFixedDamage(float damage) {
        this.useRandomDamage = false;
        this.fixedDamage = damage;
    }

    /**
     * 设置随机伤害范围
     *
     * @param minDamage 最小伤害值
     * @param maxDamage 最大伤害值
     */
    public void setRandomDamage(float minDamage, float maxDamage) {
        this.useRandomDamage = true;
        this.minRandomDamage = minDamage;
        this.maxRandomDamage = maxDamage;
    }

    /**
     * 设置伤害配置
     *
     * @param config 线虫配置
     */
    public void setDamageConfig(ThreadlingAPI.ThreadlingConfig config) {
        if (config.useRandomDamage) {
            setRandomDamage(config.minRandomDamage, config.maxRandomDamage);
        } else {
            setFixedDamage(config.fixedDamage);
        }
    }

    /**
     * 创建实体的属性
     * <p>
     * 设置生命值、移动速度、攻击力、追踪范围和护甲值
     *
     * @return 属性构建器
     */
    public static AttributeSupplier createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)        // 最大生命值（极低，一击必杀）
                .add(Attributes.MOVEMENT_SPEED, 0.4D)    // 移动速度
                .add(Attributes.ATTACK_DAMAGE, 8.0D)     // 攻击力
                .add(Attributes.FOLLOW_RANGE, 32.0D)     // 追踪范围
                .add(Attributes.ARMOR, 0.0D)             // 护甲值
                .build();
    }

    /**
     * 执行伤害目标操作
     * <p>
     * 对目标造成随机伤害（5-10点），每个实体只能造成一次伤害
     *
     * @param entity 被攻击的目标实体
     * @return true表示成功造成伤害，false表示未造成伤害
     */
    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        // 如果已经造成过伤害，不再重复攻击
        if (hasDealtDamage) {
            return false;
        }

        // 不能攻击玩家
        if (entity instanceof Player) {
            return false;
        }

        // 对LivingEntity造成伤害
        if (entity instanceof LivingEntity livingTarget) {
            // 计算伤害值
            float damage;
            if (useRandomDamage) {
                // 随机伤害
                damage = minRandomDamage + (this.getRandom().nextFloat() * (maxRandomDamage - minRandomDamage));
            } else {
                // 固定伤害
                damage = fixedDamage;
            }

            // 创建伤害源（由本实体发起的近战攻击）
            DamageSource damageSource = this.damageSources().mobAttack(this);

            // 尝试造成伤害
            if (livingTarget.hurt(damageSource, damage)) {
                // 应用sever效果
                if (applySeverEffect) {
                    livingTarget.addEffect(new MobEffectInstance(
                            FELEffects.SEVER.get(),
                            severDuration,
                            severAmplifier
                    ));
                }

                hasDealtDamage = true;  // 标记已造成伤害
                this.discard();          // 造成伤害后实体消失
                return true;
            }
        }
        return false;
    }

    /**
     * 实体的每帧更新逻辑
     * <p>
     * 处理实体的追踪、移动和攻击逻辑：
     * 1. 检查目标是否有效
     * 2. 判断是否需要延迟攻击（近距离立即攻击，远距离等待后攻击）
     * 3. 向目标移动
     * 4. 到达攻击范围后执行攻击
     */
    @Override
    public void tick() {
        super.tick();

        // 如果没有目标，不执行任何操作
        if (target == null) {
            return;
        }

        // 如果目标已死亡，实体消失
        if (!target.isAlive()) {
            this.discard();
            return;
        }

        // 如果已造成伤害，实体消失（防止残留）
        if (hasDealtDamage) {
            this.discard();
            return;
        }

        // 如果距离目标超过32格（1024平方单位），实体消失
        if (this.distanceToSqr(target) > 1024.0) {
            this.discard();
            return;
        }

        // 判断是否需要延迟攻击（生成时距离目标较远）
        boolean needsDelay = this.spawnDistance > MIN_DELAY_DISTANCE;
        /* 生成时的游戏刻数，用于计算攻击延迟 */
        int spawnTime = 0;
        if (needsDelay && this.tickCount - spawnTime < SPAWN_DELAY) {
            // 延迟期间：快速向目标移动
            double dx = target.getX() - this.getX();
            double dy = target.getY() - this.getY();
            double dz = target.getZ() - this.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // 如果距离大于1格，向目标移动（速度0.5）
            if (distance > 1.0) {
                double speed = 0.5D;
                this.setDeltaMovement(dx / distance * speed, dy / distance * speed, dz / distance * speed);
            }

            // 面向目标
            this.lookAt(target, 10.0F, 10.0F);
            return;
        }

        // 正常追踪阶段：计算与目标的方向和距离
        double dx = target.getX() - this.getX();
        double dy = target.getY() - this.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // 如果距离大于1.5格，向目标移动（速度0.4）
        if (distance > 1.5) {
            double speed = 0.4D;
            this.setDeltaMovement(dx / distance * speed, this.getDeltaMovement().y, dz / distance * speed);
        }

        // 面向目标
        this.lookAt(target, 10.0F, 10.0F);

        // 如果距离小于2格，执行攻击
        if (distance < 2.0) {
            this.doHurtTarget(target);
        }
    }

    /**
     * 设置实体拥有者（召唤者）
     *
     * @param owner 召唤实体的玩家
     */
    public void setOwner(Player owner) {
        this.owner = owner;
    }

    /**
     * 设置攻击目标
     * <p>
     * 同时计算并存储生成时与目标的距离，用于判断是否需要攻击延迟
     *
     * @param target 要攻击的目标
     */
    public void setTarget(LivingEntity target) {
        this.target = target;
        // 计算生成时与目标的距离
        if (target != null) {
            double dx = target.getX() - this.getX();
            double dy = target.getY() - this.getY();
            double dz = target.getZ() - this.getZ();
            this.spawnDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }

    /**
     * 获取目标的UUID
     *
     * @return 目标实体的UUID，如果目标为空则返回null
     */
    private UUID getTargetUUID() {
        return target != null ? target.getUUID() : null;
    }

    /**
     * 获取环境音效
     *
     * @return 末影螨的环境音效
     */
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMITE_AMBIENT;
    }

    /**
     * 获取受伤音效
     *
     * @param damageSource 伤害源
     * @return 末影螨的受伤音效
     */
    @Override
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.ENDERMITE_HURT;
    }

    /**
     * 获取死亡音效
     *
     * @return 末影螨的死亡音效
     */
    @Override
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.ENDERMITE_DEATH;
    }

    /**
     * 获取音效音量
     *
     * @return 音量大小（0.1倍）
     */
    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    /**
     * 判断是否可被视作敌人
     *
     * @return 始终返回false，表示不被视作敌人
     */
    @Override
    public boolean canBeSeenAsEnemy() {
        return false;
    }

    /**
     * 判断是否可被攻击
     *
     * @return 始终返回true，表示可被攻击
     */
    @Override
    public boolean isAttackable() {
        return true;
    }

    /**
     * 判断是否可被推动
     *
     * @return 始终返回false，表示不可被推动
     */
    @Override
    public boolean isPushable() {
        return false;
    }

    /**
     * 判断是否对特定伤害源免疫
     *
     * @param source 伤害源
     * @return 对火焰伤害免疫，返回true
     */
    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE);
    }

    /**
     * 处理实体受伤逻辑
     * <p>
     * 对火焰伤害不进行处理（免疫），其他伤害正常处理
     *
     * @param source 伤害源
     * @param amount 伤害量
     * @return true表示成功处理伤害，false表示免疫伤害
     */
    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        // 免疫火焰伤害
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public Player getOwner() {
        return owner;
    }
}