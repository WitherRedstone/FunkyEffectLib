package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.NetworkHandler;
import com.chinaex123.funky_effect_lib.network.effect.FrostArmorSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/** 冰霜护甲效果类：拾取经验球时有概率生成冰晶，每个冰晶提供高额减伤 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class FrostArmor extends MobEffect {

    private static final ResourceLocation CRYSTAL_EXPIRY_ARRAY_KEY = FunkyEffectLib.id("frost_armor_crystal_expiry_array");

    private static final float BASE_CRYSTAL_CHANCE = 0.25f; // 基础冰晶生成概率
    private static final float CHANCE_PER_LEVEL = 0.05f; // 每级增加的冰晶生成概率
    private static final float BASE_DAMAGE_REDUCTION = 0.10f; // 每个冰晶基础减伤
    private static final int MAX_CRYSTALS = 10; // 最大冰晶数量
    private static final int CRYSTAL_DURATION = 600; // 存在时间

    public FrostArmor(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        cleanExpiredCrystals(entity);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    // ==================== 玩家登录时同步 ====================
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // 同步冰霜护甲状态
        if (player.hasEffect(FELEffects.FROST_ARMOR.get())) {
            long[] expiryArray = getExpiryArray(player);
            int count = expiryArray.length;
            long earliestExpiry = count > 0 ? expiryArray[0] : 0;
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new FrostArmorSyncPacket(player.getUUID(), count, earliestExpiry));
        }
    }

    // ==================== NBT 存储方法 ====================
    private static long[] getExpiryArray(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = CRYSTAL_EXPIRY_ARRAY_KEY.toString();

        if (!persistentData.contains(key)) {
            return new long[0];
        }
        return persistentData.getLongArray(key);
    }

    private static void saveExpiryArray(LivingEntity entity, long[] expiryArray) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = CRYSTAL_EXPIRY_ARRAY_KEY.toString();

        if (expiryArray.length == 0) {
            persistentData.remove(key);
        } else {
            persistentData.putLongArray(key, expiryArray);
        }
    }

    // ==================== 冰晶数量获取 ====================
    public static int getCrystalCount(LivingEntity entity) {
        return getExpiryArray(entity).length;
    }

    // ==================== 清理过期冰晶 ====================
    private static void cleanExpiredCrystals(LivingEntity entity) {
        long currentTime = entity.level().getGameTime();
        long[] current = getExpiryArray(entity);

        long[] newArray = Arrays.stream(current)
                .filter(expiry -> currentTime < expiry)
                .toArray();

        if (newArray.length != current.length) {
            saveExpiryArray(entity, newArray);
            syncToClient(entity);
        }
    }

    // ==================== 添加冰晶 ====================
    private static void addCrystal(LivingEntity entity, long expiryTime) {
        long[] current = getExpiryArray(entity);
        long[] newArray = new long[current.length + 1];
        System.arraycopy(current, 0, newArray, 0, current.length);
        newArray[current.length] = expiryTime;
        saveExpiryArray(entity, newArray);
        syncToClient(entity);
    }

    // ==================== 移除冰晶（消耗一个） ====================
    private static void removeOldestCrystal(LivingEntity entity) {
        long[] current = getExpiryArray(entity);
        if (current.length <= 1) {
            clearCrystals(entity);
        } else {
            long[] newArray = new long[current.length - 1];
            System.arraycopy(current, 1, newArray, 0, current.length - 1);
            saveExpiryArray(entity, newArray);
            syncToClient(entity);
        }
    }

    // ==================== 清除所有冰晶 ====================
    public static void clearCrystals(LivingEntity entity) {
        saveExpiryArray(entity, new long[0]);
        syncToClient(entity);
    }

    // ==================== 客户端同步 ====================
    private static void syncToClient(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            long[] expiryArray = getExpiryArray(entity);
            int count = expiryArray.length;
            long earliestExpiry = count > 0 ? expiryArray[0] : 0;
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new FrostArmorSyncPacket(entity.getUUID(), count, earliestExpiry));
        }
    }

    // ==================== 事件监听 ====================
    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        MobEffectInstance effect = player.getEffect(FELEffects.FROST_ARMOR.get());
        if (effect == null) {
            return;
        }

        cleanExpiredCrystals(player);

        int amplifier = effect.getAmplifier();
        int currentCrystals = getCrystalCount(player);

        float crystalChance = BASE_CRYSTAL_CHANCE + (amplifier * CHANCE_PER_LEVEL);
        if (currentCrystals < MAX_CRYSTALS && player.getRandom().nextFloat() < crystalChance) {
            long currentTime = player.level().getGameTime();
            long expiryTime = currentTime + CRYSTAL_DURATION;
            addCrystal(player, expiryTime);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        MobEffectInstance effect = entity.getEffect(FELEffects.FROST_ARMOR.get());
        if (effect == null) {
            return;
        }

        cleanExpiredCrystals(entity);

        int currentCrystals = getCrystalCount(entity);

        if (currentCrystals > 0) {
            float damageReduction = Math.min(currentCrystals * BASE_DAMAGE_REDUCTION, 0.8f);
            float originalDamage = event.getAmount();
            float reducedDamage = originalDamage * (1.0F - damageReduction);
            event.setAmount(Math.max(0, reducedDamage));

            removeOldestCrystal(entity);
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.FROST_ARMOR.get()) {
            clearCrystals(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.FROST_ARMOR.get()) {
            clearCrystals(event.getEntity());
        }
    }
}