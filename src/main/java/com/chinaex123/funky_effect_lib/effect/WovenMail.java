package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.NetworkHandler;
import com.chinaex123.funky_effect_lib.network.effect.WovenMailSyncPacket;
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

/**
 * 织造铠甲：拾取经验球时有概率生成缠结，每个缠结提供8%减伤
 * <p>
 * 机制：
 * <ol>
 *   <li>拾取经验球时有概率生成缠结（基础25%，每级+5%）</li>
 *   <li>最大缠结数量为10个</li>
 *   <li>每个缠结提供8%伤害减免，最高80%</li>
 *   <li>每个缠结持续100刻（5秒），过期后自动移除</li>
 *   <li>缠结过期时间存储在持久化数据中</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class WovenMail extends MobEffect {

    private static final ResourceLocation TANGLE_EXPIRY_ARRAY_KEY = FunkyEffectLib.id("woven_mail_tangle_expiry_array");

    /** 基础缠结生成概率 **/
    private static final float BASE_TANGLE_CHANCE = 0.25f;
    /** 每级增加的缠结生成概率 **/
    private static final float CHANCE_PER_LEVEL = 0.05f;
    /** 每层减伤比例 **/
    private static final float DAMAGE_REDUCTION_PER_LAYER = 0.08f;
    /** 最大缠结数量 **/
    public static final int MAX_TANGLES = 10;
    /** 每层持续时间 **/
    private static final int LAYER_DURATION = 100;

    public WovenMail(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 每Tick清理过期的缠结
        cleanExpiredTangles(entity);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家登录事件处理
     * 同步织造铠甲状态到客户端
     *
     * @param event 玩家登录事件
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.hasEffect(FELEffects.WOVEN_MAIL.get())) {
            long[] expiryArray = getExpiryArray(player);
            int count = expiryArray.length;
            long earliestExpiry = count > 0 ? expiryArray[0] : 0;
            int remainingSeconds = getRemainingSeconds(player);
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new WovenMailSyncPacket(player.getUUID(), count, earliestExpiry, remainingSeconds));
        }
    }

    /**
     * 获取实体的缠结过期时间数组
     *
     * @param entity 目标实体
     * @return 过期时间数组（排序）
     */
    private static long[] getExpiryArray(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = TANGLE_EXPIRY_ARRAY_KEY.toString();

        if (!persistentData.contains(key)) {
            return new long[0];
        }
        return persistentData.getLongArray(key);
    }

    /**
     * 保存实体的缠结过期时间数组
     *
     * @param entity 目标实体
     * @param expiryArray 过期时间数组
     */
    private static void saveExpiryArray(LivingEntity entity, long[] expiryArray) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = TANGLE_EXPIRY_ARRAY_KEY.toString();

        if (expiryArray.length == 0) {
            persistentData.remove(key);
        } else {
            persistentData.putLongArray(key, expiryArray);
        }
    }

    /**
     * 获取实体的缠结数量
     *
     * @param entity 目标实体
     * @return 缠结数量
     */
    public static int getTangleCount(LivingEntity entity) {
        return getExpiryArray(entity).length;
    }

    /**
     * 清理过期的缠结
     *
     * @param entity 目标实体
     */
    private static void cleanExpiredTangles(LivingEntity entity) {
        long currentTime = entity.level().getGameTime();
        long[] current = getExpiryArray(entity);

        if (current.length == 0) {
            return;
        }

        // 获取最早的过期时间
        long earliestExpiry = current[0];
        int expiredLayers = 0;

        // 计算过期层数
        for (long expiry : current) {
            if (currentTime >= expiry) {
                expiredLayers++;
            } else {
                break;
            }
        }

        if (expiredLayers > 0) {
            if (expiredLayers >= current.length) {
                // 所有缠结已过期
                clearTangles(entity);
            } else {
                // 移除过期的层数
                long[] newArray = new long[current.length - expiredLayers];
                System.arraycopy(current, expiredLayers, newArray, 0, current.length - expiredLayers);
                saveExpiryArray(entity, newArray);
                syncToClient(entity);
            }
        }
    }

    /**
     * 添加一个缠结
     *
     * @param entity 目标实体
     */
    private static void addTangle(LivingEntity entity) {
        long[] current = getExpiryArray(entity);
        long currentTime = entity.level().getGameTime();

        // 重新计算所有缠结的过期时间，确保每层都有完整的5秒
        long[] newArray = new long[current.length + 1];
        for (int i = 0; i < current.length; i++) {
            newArray[i] = currentTime + ((long) (i + 1) * LAYER_DURATION);
        }
        newArray[current.length] = currentTime + ((long) (current.length + 1) * LAYER_DURATION);

        saveExpiryArray(entity, newArray);
        syncToClient(entity);
    }

    /**
     * 获取剩余时间（秒）
     *
     * @param entity 目标实体
     * @return 剩余秒数
     */
    private static int getRemainingSeconds(LivingEntity entity) {
        long[] current = getExpiryArray(entity);
        if (current.length == 0) {
            return 0;
        }

        long currentTime = entity.level().getGameTime();
        long earliestExpiry = current[0];
        long remainingTicks = earliestExpiry - currentTime;
        return (int) Math.max(0, Math.ceil(remainingTicks / 20.0));
    }

    /**
     * 清除所有缠结
     *
     * @param entity 目标实体
     */
    public static void clearTangles(LivingEntity entity) {
        saveExpiryArray(entity, new long[0]);
        syncToClient(entity);
    }

    /**
     * 同步缠结数据到客户端
     *
     * @param entity 目标实体
     */
    private static void syncToClient(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            long[] expiryArray = getExpiryArray(entity);
            int count = expiryArray.length;
            long earliestExpiry = count > 0 ? expiryArray[0] : 0;
            int remainingSeconds = getRemainingSeconds(entity);
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new WovenMailSyncPacket(entity.getUUID(), count, earliestExpiry, remainingSeconds));
        }
    }

    /**
     * 经验球拾取事件处理
     * 有概率生成新的缠结
     *
     * @param event 经验球拾取事件
     */
    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        // 检查玩家是否拥有织造铠甲效果
        MobEffectInstance effect = player.getEffect(FELEffects.WOVEN_MAIL.get());
        if (effect == null) {
            return;
        }

        // 清理过期的缠结
        cleanExpiredTangles(player);

        int amplifier = effect.getAmplifier();
        int currentTangles = getTangleCount(player);

        // 计算生成概率：基础 + 等级 × 每级加成
        float tangleChance = BASE_TANGLE_CHANCE + (amplifier * CHANCE_PER_LEVEL);
        // 未达到最大数量且触发概率时生成新缠结
        if (currentTangles < MAX_TANGLES && player.getRandom().nextFloat() < tangleChance) {
            addTangle(player);
        }
    }

    /**
     * 实体受伤事件处理
     * 根据缠结数量提供伤害减免
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) {
            return;
        }

        // 检查实体是否拥有织造铠甲效果
        MobEffectInstance effect = entity.getEffect(FELEffects.WOVEN_MAIL.get());
        if (effect == null) {
            return;
        }

        // 清理过期的缠结
        cleanExpiredTangles(entity);

        int currentTangles = getTangleCount(entity);

        if (currentTangles > 0) {
            // 计算减伤比例（每层8%，最高80%）
            float damageReduction = Math.min(currentTangles * DAMAGE_REDUCTION_PER_LAYER, 0.8f);
            float originalDamage = event.getAmount();
            float reducedDamage = originalDamage * (1.0F - damageReduction);
            event.setAmount(Math.max(0, reducedDamage));
            // 缠结不会被消耗，只通过时间衰减
        }
    }

    /**
     * 效果移除事件处理
     * 效果被手动移除时清除所有缠结
     *
     * @param event 效果移除事件
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.WOVEN_MAIL.get()) {
            clearTangles(event.getEntity());
        }
    }

    /**
     * 效果过期事件处理
     * 效果自然过期时清除所有缠结
     *
     * @param event 效果过期事件
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.WOVEN_MAIL.get()) {
            clearTangles(event.getEntity());
        }
    }
}