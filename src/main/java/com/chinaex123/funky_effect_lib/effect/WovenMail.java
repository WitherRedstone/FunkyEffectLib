package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.effect.WovenMailSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/** 织造铠甲：拾取经验球时有概率生成缠结，每个缠结提供高额减伤 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class WovenMail extends MobEffect {

    private static final ResourceLocation TANGLE_EXPIRY_ARRAY_KEY = FunkyEffectLib.id("woven_mail_tangle_expiry_array");

    private static final float BASE_TANGLE_CHANCE = 0.25f; // 基础缠结生成概率
    private static final float CHANCE_PER_LEVEL = 0.05f; // 每级增加的缠结生成概率
    private static final float BASE_DAMAGE_REDUCTION = 0.35f; // 每个缠结基础减伤
    private static final int MAX_TANGLES = 10; // 最大缠结数量
    private static final int TANGLE_DURATION = 600; // 存在时间

    public WovenMail(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        cleanExpiredTangles(entity);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    // ==================== 玩家登录时同步 ====================
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // 同步织造铠甲状态
        if (player.hasEffect(FELEffects.WOVEN_MAIL)) {
            long[] expiryArray = getExpiryArray(player);
            int count = expiryArray.length;
            long earliestExpiry = count > 0 ? expiryArray[0] : 0;
            PacketDistributor.sendToPlayer(player, new WovenMailSyncPacket(player.getUUID(), count, earliestExpiry));
        }
    }

    // ==================== NBT 存储方法 ====================
    private static long[] getExpiryArray(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = TANGLE_EXPIRY_ARRAY_KEY.toString();

        if (!persistentData.contains(key)) {
            return new long[0];
        }
        return persistentData.getLongArray(key);
    }

    private static void saveExpiryArray(LivingEntity entity, long[] expiryArray) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = TANGLE_EXPIRY_ARRAY_KEY.toString();

        if (expiryArray.length == 0) {
            persistentData.remove(key);
        } else {
            persistentData.putLongArray(key, expiryArray);
        }
    }

    // ==================== 缠结数量获取 ====================
    public static int getTangleCount(LivingEntity entity) {
        return getExpiryArray(entity).length;
    }

    // ==================== 清理过期缠结 ====================
    private static void cleanExpiredTangles(LivingEntity entity) {
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

    // ==================== 添加缠结 ====================
    private static void addTangle(LivingEntity entity, long expiryTime) {
        long[] current = getExpiryArray(entity);
        long[] newArray = new long[current.length + 1];
        System.arraycopy(current, 0, newArray, 0, current.length);
        newArray[current.length] = expiryTime;
        saveExpiryArray(entity, newArray);
        syncToClient(entity);
    }

    // ==================== 移除缠结（消耗一个） ====================
    private static void removeOldestTangle(LivingEntity entity) {
        long[] current = getExpiryArray(entity);
        if (current.length <= 1) {
            clearTangles(entity);
        } else {
            long[] newArray = new long[current.length - 1];
            System.arraycopy(current, 1, newArray, 0, current.length - 1);
            saveExpiryArray(entity, newArray);
            syncToClient(entity);
        }
    }

    // ==================== 清除所有缠结 ====================
    public static void clearTangles(LivingEntity entity) {
        saveExpiryArray(entity, new long[0]);
        syncToClient(entity);
    }

    // ==================== 客户端同步 ====================
    private static void syncToClient(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            long[] expiryArray = getExpiryArray(entity);
            int count = expiryArray.length;
            long earliestExpiry = count > 0 ? expiryArray[0] : 0;
            PacketDistributor.sendToPlayer(serverPlayer,
                    new WovenMailSyncPacket(entity.getUUID(), count, earliestExpiry));
        }
    }

    // ==================== 事件监听 ====================
    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();

        MobEffectInstance effect = player.getEffect(FELEffects.WOVEN_MAIL);
        if (effect == null) {
            return;
        }

        cleanExpiredTangles(player);

        int amplifier = effect.getAmplifier();
        int currentTangles = getTangleCount(player);

        float tangleChance = BASE_TANGLE_CHANCE + (amplifier * CHANCE_PER_LEVEL);
        if (currentTangles < MAX_TANGLES && player.getRandom().nextFloat() < tangleChance) {
            long currentTime = player.level().getGameTime();
            long expiryTime = currentTime + TANGLE_DURATION;
            addTangle(player, expiryTime);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();

        MobEffectInstance effect = entity.getEffect(FELEffects.WOVEN_MAIL);
        if (effect == null) {
            return;
        }

        cleanExpiredTangles(entity);

        int currentTangles = getTangleCount(entity);

        if (currentTangles > 0) {
            float damageReduction = Math.min(currentTangles * BASE_DAMAGE_REDUCTION, 0.8f);
            float originalDamage = event.getOriginalDamage();
            float reducedDamage = originalDamage * (1.0F - damageReduction);
            event.setNewDamage(Math.max(0, reducedDamage));

            removeOldestTangle(entity);
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.WOVEN_MAIL) {
            clearTangles(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.WOVEN_MAIL) {
            clearTangles(event.getEntity());
        }
    }
}