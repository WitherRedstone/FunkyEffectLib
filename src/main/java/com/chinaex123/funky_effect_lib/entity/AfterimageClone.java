package com.chinaex123.funky_effect_lib.entity;

import com.chinaex123.funky_effect_lib.init.FELEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.UUID;

/**
 * 残影分身实体类
 * <p>
 * 该实体是玩家召唤的分身，用于吸引敌对生物的注意力。
 * 分身拥有极高的生命值、极低的移动速度，会自动嘲讽范围内的敌对生物。
 * 存在时间可配置，到期后自动消失。
 * <p>
 * 注意：部分代码被注释，保留了伤害处理的可扩展性，
 * 当前分身处于无敌状态（注释代码所示），实际使用时可根据需求启用。
 */
public class AfterimageClone extends LivingEntity {

    /** 同步数据：拥有者名称 */
    private static final EntityDataAccessor<String> DATA_OWNER_NAME = SynchedEntityData.defineId(AfterimageClone.class, EntityDataSerializers.STRING);

    /** 拥有者的UUID */
    private UUID ownerUUID;
    /** 过期时间（游戏刻数），0表示永不过期 */
    private long expiryTime;

    /**
     * 创建分身的属性
     * <p>
     * 设置高生命值和极低移动速度，使其成为有效的嘲讽目标但不易移动
     *
     * @return 属性构建器
     */
    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)    // 高生命值，不易被击杀
                .add(Attributes.MOVEMENT_SPEED, 0.1)   // 极慢移动速度
                .build();
    }

    /**
     * 构造残影分身（用于实体类型注册）
     *
     * @param entityType 实体类型
     * @param level 世界对象
     */
    public AfterimageClone(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.expiryTime = 0;
    }

    /**
     * 构造残影分身（用于实际召唤）
     *
     * @param level 世界对象
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @param owner 拥有者（召唤者）
     */
    public AfterimageClone(Level level, double x, double y, double z, Player owner) {
        this(FELEntityTypes.AFTERIMAGE_CLONE.get(), level);
        this.setPos(x, y, z);
        this.ownerUUID = owner.getUUID();
        this.entityData.set(DATA_OWNER_NAME, owner.getName().getString());
        this.expiryTime = 0;
    }

    /**
     * 定义同步数据
     * <p>
     * 初始化需要网络同步的数据字段
     */
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER_NAME, "");
    }

    /**
     * 每帧更新逻辑
     * <p>
     * 1. 检查是否过期，如果过期则移除分身
     * 2. 嘲讽范围内的敌对生物
     */
    @Override
    public void tick() {
        super.tick();

        // 客户端不执行逻辑
        if (level().isClientSide()) {
            return;
        }

        // 检查过期时间（只有设置了过期时间才检查）
        if (expiryTime > 0 && level().getGameTime() >= expiryTime) {
            removeAfterimageClone();
            return;
        }

        // 嘲讽周围敌对生物
        attractHostileMobs();
    }

//    @Override
//    public boolean hurt(DamageSource source, float amount) {
//        // 分身不受任何伤害
//        return false;
//    }
//
//    @Override
//    public boolean isInvulnerableTo(DamageSource source) {
//        // 对所有伤害源无敌
//        return true;
//    }
//
//    @Override
//    public boolean isInvulnerable() {
//        // 免疫伤害
//        return true;
//    }
//
//    @Override
//    public boolean canBeAffected(MobEffectInstance effect) {
//        // 防止饥饿等效果造成伤害
//        return false;
//    }
//
//    @Override
//    public boolean isPushable() {
//        // 防止被击退
//        return false;
//    }
//
//    @Override
//    public void setHealth(float health) {
//        // 防止生命值被设置为0（除非是正常的伤害导致）
//        if (health <= 0 && this.getHealth() > 0 && !this.level().isClientSide()) {
//            return;
//        }
//        super.setHealth(health);
//    }

    /**
     * 实体死亡处理
     * <p>
     * 调用父类方法，但当前设计下分身不会死亡（无敌状态）
     *
     * @param damageSource 伤害源
     */
    @Override
    public void die(@NotNull DamageSource damageSource) {
        super.die(damageSource);
    }

    /**
     * 嘲讽周围的敌对生物
     * <p>
     * 在半径16格范围内搜索所有怪物，并将它们的攻击目标设置为该分身
     */
    private void attractHostileMobs() {
        int tauntRadius = 16;
        var searchArea = getBoundingBox().inflate(tauntRadius);

        // 获取范围内的所有怪物，并设置攻击目标为当前分身
        level().getEntitiesOfClass(Mob.class, searchArea, mob -> {
            if (mob.isRemoved()) return false;
            return mob.canAttack(this);
        }).forEach(mob -> {
            mob.setTarget(this);
        });
    }

    /**
     * 移除残影分身
     * <p>
     * 安全地移除实体，如果尚未被移除则执行移除操作
     */
    private void removeAfterimageClone() {
        if (!isRemoved()) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    /**
     * 设置过期时间
     *
     * @param expiryTime 过期游戏刻数
     */
    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    /**
     * 获取拥有者名称
     *
     * @return 拥有者名称字符串
     */
    public String getOwnerName() {
        return this.entityData.get(DATA_OWNER_NAME);
    }

    /**
     * 获取拥有者UUID
     *
     * @return 拥有者的UUID
     */
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * 获取盔甲槽位
     *
     * @return 空的迭代器
     */
    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    /**
     * 获取指定槽位的物品
     *
     * @param slot 装备槽位
     * @return 空的物品栈
     */
    @Override
    public @NotNull ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    /**
     * 设置指定槽位的物品
     *
     * @param slot 装备槽位
     * @param stack 物品栈
     */
    @Override
    public void setItemSlot(@NotNull EquipmentSlot slot, ItemStack stack) {
    }

    /**
     * 获取主要手臂方向
     *
     * @return 右手为主手臂
     */
    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    /**
     * 从NBT读取额外数据
     *
     * @param tag NBT标签
     */
    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
        this.expiryTime = tag.getLong("ExpiryTime");
        this.setHealth(tag.getFloat("Health"));
        this.entityData.set(DATA_OWNER_NAME, tag.getString("OwnerName"));
    }

    /**
     * 将额外数据写入NBT
     *
     * @param tag NBT标签
     */
    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putLong("ExpiryTime", expiryTime);
        tag.putFloat("Health", this.getHealth());
        tag.putString("OwnerName", this.getOwnerName());
    }

    /**
     * 创建残影分身（效果召唤方法）
     * <p>
     * 在玩家位置生成一个分身，设置过期时间、旋转角度和生命值
     *
     * @param owner 召唤者（玩家）
     * @param durationTicks 存在持续时间（刻）
     */
    public static void createClone(Player owner, int durationTicks) {
        // 仅在服务端执行
        if (owner.level().isClientSide() || !(owner.level() instanceof ServerLevel level)) {
            return;
        }

        // 创建分身实体
        AfterimageClone clone = new AfterimageClone(level, owner.getX(), owner.getY(), owner.getZ(), owner);

        // 设置过期时间
        clone.setExpiryTime(level.getGameTime() + durationTicks);

        // 复制拥有者的旋转角度
        clone.setYRot(owner.getYRot());
        clone.setXRot(owner.getXRot());
        clone.setYHeadRot(owner.getYHeadRot());

        // 设置满生命值
        clone.setHealth(clone.getMaxHealth());

        // 添加到世界
        level.addFreshEntity(clone);
    }
}