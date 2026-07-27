package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELDamageTypes;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 流血：每秒受到流血伤害
 * <p>
 * 机制：
 * <ol>
 *   <li>基础伤害为1点，每级增加1点</li>
 *   <li>每2.5秒（50刻）造成一次伤害</li>
 *   <li>伤害类型为自定义的BLEEDING（流血）</li>
 *   <li>效果消失时停止伤害</li>
 *   <li>适用于任何LivingEntity，不限于玩家</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Bleeding extends MobEffect {

    /** 基础伤害 **/
    private static final float BASE_DAMAGE = 1.0f;
    /** 每级额外伤害 **/
    private static final float ADDITIONAL_DAMAGE_PER_LEVEL = 1.0f;
    /** 伤害间隔 **/
    private static final int DAMAGE_INTERVAL = 50;

    /** 缓存每个玩家的Tick计数器 **/
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();

    public Bleeding(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {}

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家Tick事件处理
     * 管理流血效果的计时和伤害
     *
     * @param event 玩家Tick事件
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide()) {
            return;
        }

        UUID entityId = player.getUUID();
        MobEffectInstance effect = player.getEffect(FELEffects.BLEEDING.get());

        if (effect == null) {
            // 效果消失，清除计时器
            tickCounterMap.remove(entityId);
            return;
        }

        // 计时器递增
        int tickCounter = tickCounterMap.getOrDefault(entityId, 0);
        tickCounter++;

        // 达到伤害间隔时造成伤害
        if (tickCounter >= DAMAGE_INTERVAL) {
            int amplifier = effect.getAmplifier();
            // 计算伤害：基础伤害 + 等级 × 每级额外伤害
            float damage = BASE_DAMAGE + (amplifier * ADDITIONAL_DAMAGE_PER_LEVEL);

            // 创建流血伤害源
            DamageSource bleedingDamage = new DamageSource(
                    player.level().registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(FELDamageTypes.BLEEDING)
            );
            // 对玩家造成伤害
            player.hurt(bleedingDamage, damage);
            // 重置计时器
            tickCounter = 0;
        }

        tickCounterMap.put(entityId, tickCounter);
    }
}