package com.chinaex123.funky_effect_lib.entity;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
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

import java.util.UUID;

public class AfterimageClone extends LivingEntity {

    private static final EntityDataAccessor<String> DATA_OWNER_NAME = SynchedEntityData.defineId(AfterimageClone.class, EntityDataSerializers.STRING);

    private UUID ownerUUID;
    private long expiryTime;

    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .build();
    }

    public AfterimageClone(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.expiryTime = 0;
    }

    public AfterimageClone(Level level, double x, double y, double z, Player owner) {
        this(FELEntityTypes.AFTERIMAGE_CLONE.get(), level);
        this.setPos(x, y, z);
        this.ownerUUID = owner.getUUID();
        this.entityData.set(DATA_OWNER_NAME, owner.getName().getString());
        this.expiryTime = 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER_NAME, "");
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            return;
        }

        // 只有设置了过期时间（>0）才检查
        if (expiryTime > 0 && level().getGameTime() >= expiryTime) {
            removeAfterimageClone();
            return;
        }

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

//    @Override
//    public boolean isInvulnerable() {
//    // 免疫伤害
//        return true;
//    }

//    // 防止饥饿等效果造成伤害
//    @Override
//    public boolean canBeAffected(MobEffectInstance effect) {
//        return false;
//    }

//    // 防止被击退
//    @Override
//    public boolean isPushable() {
//        return false;
//    }


    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
    }

//    @Override
//    public void setHealth(float health) {
//        // 防止生命值被设置为0（除非是正常的伤害导致）
//        if (health <= 0 && this.getHealth() > 0 && !this.level().isClientSide()) {
//            return;
//        }
//        super.setHealth(health);
//    }

    private void attractHostileMobs() {
        int tauntRadius = 16;
        var searchArea = getBoundingBox().inflate(tauntRadius);

        level().getEntitiesOfClass(Mob.class, searchArea, mob -> {
            if (mob.isRemoved()) return false;
            return mob.canAttack(this);
        }).forEach(mob -> {
            mob.setTarget(this);
        });
    }

    private void removeAfterimageClone() {
        if (!isRemoved()) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getOwnerName() {
        return this.entityData.get(DATA_OWNER_NAME);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return java.util.Collections.emptyList();
    }

    @Override
    public @NotNull ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
        this.expiryTime = tag.getLong("ExpiryTime");
        this.setHealth(tag.getFloat("Health"));
        this.entityData.set(DATA_OWNER_NAME, tag.getString("OwnerName"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putLong("ExpiryTime", expiryTime);
        tag.putFloat("Health", this.getHealth());
        tag.putString("OwnerName", this.getOwnerName());
    }

    // 效果召唤
    public static void createClone(Player owner, int durationTicks) {
        if (owner.level().isClientSide() || !(owner.level() instanceof ServerLevel level)) {
            return;
        }

        AfterimageClone clone = new AfterimageClone(level, owner.getX(), owner.getY(), owner.getZ(), owner);
        clone.setExpiryTime(level.getGameTime() + durationTicks);
        clone.setYRot(owner.getYRot());
        clone.setXRot(owner.getXRot());
        clone.setYHeadRot(owner.getYHeadRot());
        clone.setHealth(clone.getMaxHealth());

        level.addFreshEntity(clone);
    }
}