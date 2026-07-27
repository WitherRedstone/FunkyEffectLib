package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 黑暗之重：每2.5秒降低移动速度，最多叠加10层
 * <p>
 * 机制：
 * <ol>
 *   <li>每2.5秒（50刻）增加1级减速</li>
 *   <li>每级减少5%移动速度</li>
 *   <li>最大减速等级为10层（累计50%减速）</li>
 *   <li>使用属性修改器实现减速效果</li>
 * </ol>
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Darkness extends MobEffect {

    private static final ResourceLocation DARKNESS_MODIFIER = ResourceLocation.fromNamespaceAndPath(FunkyEffectLib.MOD_ID, "darkness_slowdown");

    /** 每级基础移动速度减少 **/
    private static final float BASE_SPEED_REDUCTION = -0.05f;
    /** 减速等级提升间隔 **/
    private static final int TICKS_PER_INTERVAL = 50;
    /** 最大减速等级 **/
    private static final int MAX_LEVEL = 10;

    /** 缓存每个实体的Tick计数器 **/
    private static final Map<UUID, Integer> entityTickMap = new HashMap<>();

    public Darkness(int color) {
        super(MobEffectCategory.HARMFUL, color);

        // 注册属性修改器：每级减少5%移动速度
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                DARKNESS_MODIFIER,
                BASE_SPEED_REDUCTION,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
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
     * 实体Tick事件处理
     * 管理减速等级的叠加
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        // 仅处理LivingEntity，且仅在服务端执行
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide()) {
            return;
        }

        MobEffectInstance darknessEffect = entity.getEffect(FELEffects.DARKNESS);

        // 如果效果消失，清除数据
        if (darknessEffect == null) {
            entityTickMap.remove(entity.getUUID());
            return;
        }

        UUID entityId = entity.getUUID();
        int ticks = entityTickMap.getOrDefault(entityId, 0) + 1;

        // 达到间隔时间，提升等级
        if (ticks >= TICKS_PER_INTERVAL) {
            entityTickMap.put(entityId, 0);
            int currentLevel = darknessEffect.getAmplifier();

            // 未达到最大等级时升级
            if (currentLevel < MAX_LEVEL - 1) {
                entity.addEffect(new MobEffectInstance(
                        darknessEffect.getEffect(),
                        darknessEffect.getDuration(),
                        currentLevel + 1,
                        darknessEffect.isAmbient(),
                        darknessEffect.isVisible(),
                        darknessEffect.showIcon()
                ));
            }
        } else {
            entityTickMap.put(entityId, ticks);
        }
    }
}