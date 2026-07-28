package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELAttributes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.effect.FrostArmorSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/**
 * 冰霜护甲：拾取经验球时有概率生成冰晶，每个冰晶提供高额减伤
 * <p>
 * 机制：
 * <ol>
 *   <li>拾取经验球时有概率生成冰晶（基础25%，每级+5%）</li>
 *   <li>最大冰晶数量为10个</li>
 *   <li>每个冰晶提供5%伤害减免，最高50%</li>
 *   <li>每个冰晶持续100刻（5秒），过期后自动移除</li>
 *   <li>冰晶过期时间存储在持久化数据中</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class FrostArmor extends MobEffect {

    private static final ResourceLocation CRYSTAL_EXPIRY_ARRAY_KEY = FunkyEffectLib.id("frost_armor_crystal_expiry_array");
    private static final ResourceLocation DAMAGE_REDUCTION_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "damage_reduction");

    /** 基础冰晶生成概率 **/
    private static final float BASE_CRYSTAL_CHANCE = 0.25f;
    /** 每级增加的冰晶生成概率 **/
    private static final float CHANCE_PER_LEVEL = 0.05f;
    /** 每层减伤比例 **/
    private static final float DAMAGE_REDUCTION_PER_LAYER = 0.05f;
    /** 最大冰晶数量 **/
    public static final int MAX_CRYSTALS = 10;
    /** 每层持续时间 **/
    private static final int LAYER_DURATION = 100;

    public FrostArmor(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 每Tick清理过期的冰晶
        cleanExpiredCrystals(entity);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家登录事件处理
     * 同步冰霜护甲状态到客户端
     *
     * @param event 玩家登录事件
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.hasEffect(FELEffects.FROST_ARMOR)) {
            long[] expiryArray = getExpiryArray(player);
            int count = expiryArray.length;
            long earliestExpiry = count > 0 ? expiryArray[0] : 0;
            int remainingSeconds = getRemainingSeconds(player);
            PacketDistributor.sendToPlayer(player, new FrostArmorSyncPacket(player.getUUID(), count, earliestExpiry, remainingSeconds));
        }
    }

    /**
     * 获取实体的冰晶过期时间数组
     *
     * @param entity 目标实体
     * @return 过期时间数组（排序）
     */
    private static long[] getExpiryArray(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = CRYSTAL_EXPIRY_ARRAY_KEY.toString();

        if (!persistentData.contains(key)) {
            return new long[0];
        }
        return persistentData.getLongArray(key);
    }

    /**
     * 保存实体的冰晶过期时间数组
     *
     * @param entity 目标实体
     * @param expiryArray 过期时间数组
     */
    private static void saveExpiryArray(LivingEntity entity, long[] expiryArray) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = CRYSTAL_EXPIRY_ARRAY_KEY.toString();

        if (expiryArray.length == 0) {
            persistentData.remove(key);
        } else {
            persistentData.putLongArray(key, expiryArray);
        }
    }

    /**
     * 获取实体的冰晶数量
     *
     * @param entity 目标实体
     * @return 冰晶数量
     */
    public static int getCrystalCount(LivingEntity entity) {
        return getExpiryArray(entity).length;
    }

    /**
     * 清理过期的冰晶
     *
     * @param entity 目标实体
     */
    private static void cleanExpiredCrystals(LivingEntity entity) {
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
                // 所有冰晶已过期
                clearCrystals(entity);
            } else {
                // 移除过期的层数
                long[] newArray = new long[current.length - expiredLayers];
                System.arraycopy(current, expiredLayers, newArray, 0, current.length - expiredLayers);
                saveExpiryArray(entity, newArray);
                updateDamageReductionAttribute(entity);
                syncToClient(entity);
            }
        }
    }

    /**
     * 添加一个冰晶
     *
     * @param entity 目标实体
     */
    private static void addCrystal(LivingEntity entity) {
        long[] current = getExpiryArray(entity);
        long currentTime = entity.level().getGameTime();

        // 重新计算所有冰晶的过期时间，确保每层都有完整的5秒
        long[] newArray = new long[current.length + 1];
        for (int i = 0; i < current.length; i++) {
            newArray[i] = currentTime + ((long) (i + 1) * LAYER_DURATION);
        }
        newArray[current.length] = currentTime + ((long) (current.length + 1) * LAYER_DURATION);

        saveExpiryArray(entity, newArray);
        updateDamageReductionAttribute(entity);
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
     * 清除所有冰晶
     *
     * @param entity 目标实体
     */
    public static void clearCrystals(LivingEntity entity) {
        saveExpiryArray(entity, new long[0]);
        removeDamageReductionAttribute(entity);
        syncToClient(entity);
    }

    /**
     * 同步冰晶数据到客户端
     *
     * @param entity 目标实体
     */
    private static void syncToClient(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            long[] expiryArray = getExpiryArray(entity);
            int count = expiryArray.length;
            long earliestExpiry = count > 0 ? expiryArray[0] : 0;
            int remainingSeconds = getRemainingSeconds(entity);
            PacketDistributor.sendToPlayer(serverPlayer,
                    new FrostArmorSyncPacket(entity.getUUID(), count, earliestExpiry, remainingSeconds));
        }
    }

    /**
     * 经验球拾取事件处理
     * 有概率生成新的冰晶
     *
     * @param event 经验球拾取事件
     */
    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();

        // 检查玩家是否拥有冰霜护甲效果
        MobEffectInstance effect = player.getEffect(FELEffects.FROST_ARMOR);
        if (effect == null) {
            return;
        }

        // 清理过期的冰晶
        cleanExpiredCrystals(player);

        int amplifier = effect.getAmplifier();
        int currentCrystals = getCrystalCount(player);

        // 计算生成概率：基础 + 等级 × 每级加成
        float crystalChance = BASE_CRYSTAL_CHANCE + (amplifier * CHANCE_PER_LEVEL);
        // 未达到最大数量且触发概率时生成新冰晶
        if (currentCrystals < MAX_CRYSTALS && player.getRandom().nextFloat() < crystalChance) {
            addCrystal(player);
        }
    }

    /**
     * 实体受伤事件处理
     * 清理过期的冰晶并更新伤害减免属性
     *
     * @param event 实体受伤事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();

        // 检查实体是否拥有冰霜护甲效果
        MobEffectInstance effect = entity.getEffect(FELEffects.FROST_ARMOR);
        if (effect == null) {
            return;
        }

        // 清理过期的冰晶
        cleanExpiredCrystals(entity);

        // 更新伤害减免属性
        updateDamageReductionAttribute(entity);
    }

    /**
     * 更新实体的伤害减免属性
     *
     * @param entity 目标实体
     */
    private static void updateDamageReductionAttribute(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION);
        if (attribute == null) {
            return;
        }

        int currentCrystals = getCrystalCount(entity);
        double damageReduction = 0.0;

        if (currentCrystals > 0) {
            // 计算减伤比例（每层5%，最高50%）
            damageReduction = Math.min(currentCrystals * DAMAGE_REDUCTION_PER_LAYER, 0.5);
        }

        // 移除旧的修饰符
        attribute.removeModifier(DAMAGE_REDUCTION_MODIFIER);

        // 如果有减伤，添加新的修饰符
        if (damageReduction > 0.0) {
            AttributeModifier modifier = new AttributeModifier(
                    DAMAGE_REDUCTION_MODIFIER,
                    damageReduction,
                    AttributeModifier.Operation.ADD_VALUE
            );
            attribute.addPermanentModifier(modifier);
        }
    }

    /**
     * 移除实体的伤害减免属性
     *
     * @param entity 目标实体
     */
    private static void removeDamageReductionAttribute(LivingEntity entity) {
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION);
        if (attribute != null) {
            attribute.removeModifier(DAMAGE_REDUCTION_MODIFIER);
        }
    }

    /**
     * 效果移除事件处理
     * 效果被手动移除时清除所有冰晶
     *
     * @param event 效果移除事件
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.FROST_ARMOR) {
            clearCrystals(event.getEntity());
        }
    }

    /**
     * 效果过期事件处理
     * 效果自然过期时清除所有冰晶
     *
     * @param event 效果过期事件
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.FROST_ARMOR) {
            clearCrystals(event.getEntity());
        }
    }
}