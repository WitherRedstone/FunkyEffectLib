package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.api.event.BoltCharge.BoltChargeAPI;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.network.NetworkHandler;
import com.chinaex123.funky_effect_lib.network.effect.BoltChargeSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 电光充能：缓慢叠加层数，当层数达到10层时消耗所有层数，召唤一道闪电攻击敌人
 * <p>
 * 机制：
 * <ol>
 *   <li>每2.5秒（50刻）自动增加1层充能</li>
 *   <li>最大充能层数为10层</li>
 *   <li>受到攻击时触发闪电打击，消耗所有充能</li>
 *   <li>支持服务端与客户端的数据同步</li>
 *   <li>效果移除或过期时自动清除充能数据</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class BoltCharge extends MobEffect {

    /** 每次充能的间隔 **/
    private static final int CHARGE_INTERVAL = 50;

    /** 缓存每个玩家的充能层数，用于检测变化并同步到客户端 **/
    private static final Map<UUID, Integer> CHARGE_CACHE = new HashMap<>();

    public BoltCharge(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        Level level = entity.level();
        long currentTime = level.getGameTime();
        long lastChargeTime = getLastChargeTime(entity);

        // 检查是否到达充能间隔
        if (currentTime - lastChargeTime >= CHARGE_INTERVAL) {
            // 增加1层充能
            addCharge(entity, 1);
            // 更新上次充能时间
            setLastChargeTime(entity, currentTime);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家登录事件处理
     * 当玩家登录时，同步其充能层数到客户端
     *
     * @param event 玩家登录事件
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.hasEffect(FELEffects.BOLT_CHARGE.get())) {
            int chargeCount = BoltChargeAPI.getChargeCount(player);
            syncToClient(player, chargeCount);
        }
    }

    /**
     * 获取实体的上次充能时间
     *
     * @param entity 目标实体
     * @return 上次充能时间（游戏刻）
     */
    private static long getLastChargeTime(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = BoltChargeAPI.LAST_CHARGE_TIME_KEY.toString();
        return persistentData.getLong(key);
    }

    /**
     * 设置实体的上次充能时间
     *
     * @param entity 目标实体
     * @param time 充能时间（游戏刻）
     */
    private static void setLastChargeTime(LivingEntity entity, long time) {
        CompoundTag persistentData = entity.getPersistentData();
        String key = BoltChargeAPI.LAST_CHARGE_TIME_KEY.toString();
        persistentData.putLong(key, time);
    }

    /**
     * 增加实体的充能层数
     *
     * @param entity 目标实体
     * @param amount 增加的层数
     */
    public static void addCharge(LivingEntity entity, int amount) {
        BoltChargeAPI.addCharge(entity, amount);
    }

    /**
     * 设置实体的充能层数
     *
     * @param entity 目标实体
     * @param count 充能层数
     */
    public static void setChargeCount(LivingEntity entity, int count) {
        BoltChargeAPI.setChargeCount(entity, count);
    }

    /**
     * 清除实体的充能层数
     *
     * @param entity 目标实体
     */
    public static void clearCharges(LivingEntity entity) {
        BoltChargeAPI.clearCharges(entity);
    }

    /**
     * 将充能层数同步给客户端
     *
     * @param entity 目标实体
     * @param chargeCount 充能层数
     */
    public static void syncToClient(LivingEntity entity, int chargeCount) {
        if (entity instanceof ServerPlayer serverPlayer) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new BoltChargeSyncPacket(entity.getUUID(), chargeCount));
        }
    }

    /**
     * 实体攻击事件处理
     * 当充能满层时，对攻击目标触发闪电打击
     *
     * @param event 实体攻击事件
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        // 仅在服务端执行
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        // 获取攻击者
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ?
                (LivingEntity) event.getSource().getEntity() : null;

        if (attacker != null) {
            // 检查充能是否已满
            int currentCharges = BoltChargeAPI.getChargeCount(attacker);
            if (currentCharges >= BoltChargeAPI.MAX_CHARGES) {
                // 触发闪电打击，消耗所有充能
                BoltChargeAPI.triggerLightning(event.getEntity(), attacker);
            }
        }
    }

    /**
     * 效果被移除事件处理
     * 效果被手动移除时清除充能数据
     *
     * @param event 效果移除事件
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == FELEffects.BOLT_CHARGE.get()) {
            clearCharges(event.getEntity());
        }
    }

    /**
     * 效果过期事件处理
     * 效果自然过期时清除充能数据
     *
     * @param event 效果过期事件
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance != null && instance.getEffect() == FELEffects.BOLT_CHARGE.get()) {
            clearCharges(event.getEntity());
        }
    }

    /**
     * 服务端Tick事件处理
     * 检测充能变化并同步给客户端
     *
     * @param event 服务端Tick事件
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // 遍历所有在线玩家
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            int chargeCount = BoltChargeAPI.getChargeCount(player);
            // 检测充能层数是否发生变化
            if (CHARGE_CACHE.getOrDefault(player.getUUID(), -1) != chargeCount) {
                // 更新缓存并同步到客户端
                CHARGE_CACHE.put(player.getUUID(), chargeCount);
                syncToClient(player, chargeCount);
            }
        }
    }
}