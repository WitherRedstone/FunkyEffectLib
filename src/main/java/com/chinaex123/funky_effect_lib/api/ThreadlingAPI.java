package com.chinaex123.funky_effect_lib.api;

import com.chinaex123.funky_effect_lib.entity.Threadling;
import com.chinaex123.funky_effect_lib.init.FELEntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 线虫公共API类
 * <p>
 * 提供线虫召唤功能，方便其他模组调用
 * 线虫会在召唤者脚下生成，然后移动去攻击目标
 */
public class ThreadlingAPI {

    /** 默认冷却时间（tick） **/
    private static final int COOLDOWN_TICKS = 100;

    /** 玩家冷却记录 **/
    private static final Map<UUID, Integer> cooldownMap = new HashMap<>();

    /**
     * 检查玩家是否在冷却中
     *
     * @param player 玩家
     * @return 是否在冷却中
     */
    public static boolean isOnCooldown(Player player) {
        UUID playerId = player.getUUID();
        int cooldownEndTime = cooldownMap.getOrDefault(playerId, 0);
        return player.tickCount < cooldownEndTime;
    }

    /**
     * 获取玩家剩余冷却时间（tick）
     *
     * @param player 玩家
     * @return 剩余冷却时间，0表示不在冷却中
     */
    public static int getRemainingCooldown(Player player) {
        UUID playerId = player.getUUID();
        int cooldownEndTime = cooldownMap.getOrDefault(playerId, 0);
        return Math.max(0, cooldownEndTime - player.tickCount);
    }

    /**
     * 设置玩家的冷却时间
     *
     * @param player 玩家
     * @param cooldownTicks 冷却时间（tick）
     */
    public static void setCooldown(Player player, int cooldownTicks) {
        UUID playerId = player.getUUID();
        cooldownMap.put(playerId, player.tickCount + cooldownTicks);
    }

    /**
     * 清除玩家的冷却时间
     *
     * @param player 玩家
     */
    public static void clearCooldown(Player player) {
        UUID playerId = player.getUUID();
        cooldownMap.remove(playerId);
    }

    /**
     * 召唤指定数量的线虫攻击目标（带配置）
     *
     * @param owner         召唤者（玩家）
     * @param target        攻击目标
     * @param count         线虫数量
     * @param checkCooldown 是否检查冷却
     * @param cooldownTicks 冷却时间（tick）
     * @param config        线虫配置
     */
    public static void spawnThreadlings(Player owner, LivingEntity target, int count, boolean checkCooldown, int cooldownTicks, ThreadlingConfig config) {
        // 检查冷却
        if (checkCooldown && isOnCooldown(owner)) {
            return;
        }

        // 召唤线虫
        for (int i = 0; i < count; i++) {
            spawnSingleThreadling(owner, target, config);
        }

        // 更新冷却时间
        if (checkCooldown) {
            setCooldown(owner, cooldownTicks);
        }
    }

    /**
     * 召唤指定数量的线虫攻击目标（使用默认配置）
     *
     * @param owner         召唤者（玩家）
     * @param target        攻击目标
     * @param count         线虫数量
     * @param checkCooldown 是否检查冷却
     * @param cooldownTicks 冷却时间（tick）
     */
    public static void spawnThreadlings(Player owner, LivingEntity target, int count, boolean checkCooldown, int cooldownTicks) {
        spawnThreadlings(owner, target, count, checkCooldown, cooldownTicks, ThreadlingConfig.createDefault());
    }

    /**
     * 召唤指定数量的线虫攻击目标（不检查冷却）
     *
     * @param owner 召唤者（玩家）
     * @param target 攻击目标
     * @param count 线虫数量
     */
    public static void spawnThreadlings(Player owner, LivingEntity target, int count) {
        spawnThreadlings(owner, target, count, false, COOLDOWN_TICKS);
    }

    /**
     * 召唤指定数量的线虫攻击目标（使用默认冷却时间）
     *
     * @param owner         召唤者（玩家）
     * @param target        攻击目标
     * @param count         线虫数量
     * @param checkCooldown 是否检查冷却
     */
    public static void spawnThreadlings(Player owner, LivingEntity target, int count, boolean checkCooldown) {
        spawnThreadlings(owner, target, count, checkCooldown, COOLDOWN_TICKS);
    }

    /**
     * 召唤单个线虫攻击目标
     * 调用一次就生成一个线虫去攻击目标
     *
     * @param owner  召唤者（玩家）
     * @param target 攻击目标
     * @param config 线虫配置
     * @return 生成的线虫实体
     */
    public static Threadling spawnSingleThreadling(Player owner, LivingEntity target, ThreadlingConfig config) {
        // 计算生成位置（在玩家脚下周围随机位置）
        double offsetX = (owner.getRandom().nextDouble() - 0.5) * 2.0; // 玩家周围1格范围
        double offsetZ = (owner.getRandom().nextDouble() - 0.5) * 2.0; // 玩家周围1格范围
        double offsetY = owner.getRandom().nextDouble() * 0.5; // 轻微高度偏移

        double spawnX = owner.getX() + offsetX;
        double spawnY = owner.getY() + offsetY;
        double spawnZ = owner.getZ() + offsetZ;

        // 创建线虫实体
        Threadling threadling = FELEntityTypes.THREADLING.get().create(owner.level());

        if (threadling != null) {
            threadling.moveTo(spawnX, spawnY, spawnZ, owner.getYRot(), 0.0F);
            threadling.setOwner(owner);
            threadling.setTarget(target);

            // 设置配置
            if (config != null) {
                // 设置sever效果配置
                if (config.applySeverEffect) {
                    threadling.setSeverEffect(config.severDuration, config.severAmplifier);
                }
                // 设置伤害配置
                threadling.setDamageConfig(config);
            }

            owner.level().addFreshEntity(threadling);
        }

        return threadling;
    }

    /**
     * 召唤单个线虫攻击目标（使用默认配置）
     *
     * @param owner  召唤者（玩家）
     * @param target 攻击目标
     * @return 生成的线虫实体
     */
    public static Threadling spawnSingleThreadling(Player owner, LivingEntity target) {
        return spawnSingleThreadling(owner, target, ThreadlingConfig.createDefault());
    }

    /**
     * 线虫配置类
     * 用于自定义线虫的属性和行为
     */
    public static class ThreadlingConfig {
        public boolean applySeverEffect = false;  // 是否应用sever效果
        public int severDuration = 100;           // sever效果持续时间（tick）
        public int severAmplifier = 0;            // sever效果等级

        // 伤害配置
        public boolean useRandomDamage = true;    // 是否使用随机伤害
        public float fixedDamage = 7.5F;          // 固定伤害值（当useRandomDamage=false时使用）
        public float minRandomDamage = 5.0F;      // 随机伤害最小值
        public float maxRandomDamage = 10.0F;     // 随机伤害最大值

        /**
         * 创建默认配置（随机伤害5-10）
         */
        public static ThreadlingConfig createDefault() {
            return new ThreadlingConfig();
        }

        /**
         * 创建带sever效果的配置
         */
        public static ThreadlingConfig withSeverEffect(int duration, int amplifier) {
            ThreadlingConfig config = new ThreadlingConfig();
            config.applySeverEffect = true;
            config.severDuration = duration;
            config.severAmplifier = amplifier;
            return config;
        }

        /**
         * 创建固定伤害配置
         */
        public static ThreadlingConfig withFixedDamage(float damage) {
            ThreadlingConfig config = new ThreadlingConfig();
            config.useRandomDamage = false;
            config.fixedDamage = damage;
            return config;
        }

        /**
         * 创建随机伤害配置
         */
        public static ThreadlingConfig withRandomDamage(float minDamage, float maxDamage) {
            ThreadlingConfig config = new ThreadlingConfig();
            config.useRandomDamage = true;
            config.minRandomDamage = minDamage;
            config.maxRandomDamage = maxDamage;
            return config;
        }

        /**
         * 创建完整配置
         */
        public static ThreadlingConfig createFullConfig(boolean applySever, int severDuration, int severAmplifier,
                                                        boolean useRandomDamage, float damage1, float damage2) {
            ThreadlingConfig config = new ThreadlingConfig();
            config.applySeverEffect = applySever;
            config.severDuration = severDuration;
            config.severAmplifier = severAmplifier;
            config.useRandomDamage = useRandomDamage;
            if (useRandomDamage) {
                config.minRandomDamage = damage1;
                config.maxRandomDamage = damage2;
            } else {
                config.fixedDamage = damage1;
            }
            return config;
        }
    }
}