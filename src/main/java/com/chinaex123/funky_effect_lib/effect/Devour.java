package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 吞食：击杀敌人可回血、延长持续时间并叠加移速 **/
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Devour extends MobEffect {

    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("32d4db92-95c5-4d9a-aad8-4f70bb0ba8f5");
    private static final String SPEED_MODIFIER_STRING = UUID.nameUUIDFromBytes("devour_speed".getBytes()).toString();

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
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
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

        movementSpeed.removeModifier(SPEED_MODIFIER_UUID);

        float speedBonus = Math.min(killCount * SPEED_BOOST_PER_KILL, MAX_SPEED_BOOST);

        if (speedBonus > 0) {
            movementSpeed.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER_UUID,
                    SPEED_MODIFIER_STRING,
                    speedBonus,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    private static void removeSpeedModifier(LivingEntity entity) {
        AttributeInstance movementSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(SPEED_MODIFIER_UUID);
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

        if (killer.level().isClientSide()) {
            return;
        }

        MobEffectInstance effect = killer.getEffect(FELEffects.DEVOUR.get());
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

        // 添加新效果（使用 addEffect 而不是 forceAddEffect）
        killer.addEffect(new MobEffectInstance(
                FELEffects.DEVOUR.get(),
                newDuration,
                newLevel,
                effect.isAmbient(),
                effect.isVisible(),
                effect.showIcon()
        ));

        // 击杀回血
        killer.heal(2.0f);

        // 应用速度加成
        applySpeedModifier(killer);
    }

    /**
     * 效果结束时清理数据
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        LivingEntity entity = event.player;

        if (entity.level().isClientSide()) {
            return;
        }

        if (!entity.hasEffect(FELEffects.DEVOUR.get())) {
            if (killCountMap.containsKey(entity.getUUID()) || levelKillCountMap.containsKey(entity.getUUID())) {
                killCountMap.remove(entity.getUUID());
                levelKillCountMap.remove(entity.getUUID());
                removeSpeedModifier(entity);
            }
        }
    }
}