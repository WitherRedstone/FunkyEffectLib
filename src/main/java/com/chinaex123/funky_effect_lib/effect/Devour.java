package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 吞食：击杀敌人可回血、延长持续时间并叠加移速 **/
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Devour extends MobEffect {

    private static final ResourceLocation SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "devour_speed");

    private static final int EXTEND_DURATION = 100; // 每次击杀延长 5秒
    private static final int LEVEL_UP_NEED = 10; // 需要击杀 10次 才能升级
    private static final int MAX_LEVEL = 4; // 最高5级
    private static final int LEVEL_DURATION = 300; // 升级后持续时间 15秒
    private static final int MAX_DURATION = 1800; // 最高5级时最大持续时间 1分30秒
    private static final float SPEED_BOOST_PER_KILL = 0.002f; // 每次击杀增加的速度
    private static final float MAX_SPEED_BOOST = 0.20f; // 最大速度限制

    private static final Map<UUID, Integer> killCountMap = new HashMap<>(); // 记录每个实体的击杀次数
    private static final Map<UUID, Integer> levelKillCountMap = new HashMap<>(); // 记录每个实体的当前等级击杀次数

    public Devour(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 根据击杀次数应用速度加成
     */
    private static void applySpeedModifier(LivingEntity entity) {
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) return;

        UUID uuid = entity.getUUID();
        int killCount = killCountMap.getOrDefault(uuid, 0);

        movementSpeed.removeModifier(SPEED_MODIFIER);

        float speedBonus = Math.min(killCount * SPEED_BOOST_PER_KILL, MAX_SPEED_BOOST);

        if (speedBonus > 0) {
            movementSpeed.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER,
                    speedBonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    /**
     * 击杀生物时触发
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity killer)) {
            return;
        }

        MobEffectInstance effect = killer.getEffect(FELEffects.DEVOUR);
        if (effect == null) {
            return;
        }

        UUID killerId = killer.getUUID();
        int currentLevel = effect.getAmplifier();
        int currentDuration = effect.getDuration();

        // 增加总击杀计数（用于速度）
        int currentKillCount = killCountMap.getOrDefault(killerId, 0);
        killCountMap.put(killerId, currentKillCount + 1);

        // 增加当前等级的击杀计数（用于升级）
        int currentLevelKills = levelKillCountMap.getOrDefault(killerId, 0);
        int newLevelKills = currentLevelKills + 1;

        int newLevel = currentLevel;
        int newDuration = currentDuration + EXTEND_DURATION;

        // 检查是否可以升级
        if (newLevelKills >= LEVEL_UP_NEED && newLevel < MAX_LEVEL) {
            newLevel++;
            newLevelKills = 0;  // 重置当前等级击杀计数
            newDuration = LEVEL_DURATION;  // 升级后重置为15秒

            // 升级时额外回血
            if (killer instanceof Player player) {
                player.heal(4.0f);
            }
        } else if (newLevel >= MAX_LEVEL) {
            // 达到最高等级后，限制最大持续时间为1分30秒
            newDuration = Math.min(newDuration, MAX_DURATION);
        }

        // 更新当前等级的击杀计数
        levelKillCountMap.put(killerId, newLevelKills);

        // 添加新效果
        killer.forceAddEffect(new MobEffectInstance(
                FELEffects.DEVOUR,
                newDuration,
                newLevel,
                effect.isAmbient(),
                effect.isVisible(),
                effect.showIcon()
        ), null);

        // 击杀回血
        killer.heal(2.0f);

        // 应用速度加成
        applySpeedModifier(killer);
    }

    /**
     * 效果结束时清理数据
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide()) {
            return;
        }

        if (!entity.hasEffect(FELEffects.DEVOUR)) {
            killCountMap.remove(entity.getUUID());
            levelKillCountMap.remove(entity.getUUID());

            AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeed != null) {
                movementSpeed.removeModifier(SPEED_MODIFIER);
            }
        }
    }
}