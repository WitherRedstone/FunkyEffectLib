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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 魔力灼烧：每秒造成魔法伤害
 * <p>
 * 机制：
 * <ol>
 *   <li>基础伤害为2点，每级增加1.5点</li>
 *   <li>每1秒（20刻）造成一次伤害</li>
 *   <li>伤害类型为自定义的MANA_BURN（魔力灼烧）</li>
 *   <li>效果消失时停止伤害</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class ManaBurn extends MobEffect {

    /** 基础伤害 **/
    private static final float BASE_DAMAGE = 2.0f;
    /** 每级额外伤害 **/
    private static final float ADDITIONAL_DAMAGE_PER_LEVEL = 1.5f;
    /** 伤害间隔 **/
    private static final int DAMAGE_INTERVAL = 20;

    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> tickCounterMap = new HashMap<>();

    public ManaBurn(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 空实现，实际逻辑在事件中处理
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * 玩家Tick事件处理
     * 管理魔力灼烧的计时和伤害
     *
     * @param event 玩家Tick事件
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

        UUID entityId = entity.getUUID();
        MobEffectInstance effect = entity.getEffect(FELEffects.MANA_BURN.get());

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
            // 计算伤害：基础 + 等级 × 每级加成
            float damage = BASE_DAMAGE + (amplifier * ADDITIONAL_DAMAGE_PER_LEVEL);

            // 创建魔力灼烧伤害源
            DamageSource manaBurnDamage = new DamageSource(
                    entity.level().registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(FELDamageTypes.MANA_BURN)
            );
            // 对实体造成伤害
            entity.hurt(manaBurnDamage, damage);
            // 重置计时器
            tickCounter = 0;
        }

        tickCounterMap.put(entityId, tickCounter);
    }
}