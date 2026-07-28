package com.chinaex123.funky_effect_lib.attribute;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 伤害减免事件处理器
 * <p>
 * 处理基于伤害减免属性的伤害计算
 */
@Mod.EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class DamageReductionHandler {

    /**
     * 生物受到伤害事件处理
     * 应用伤害减免属性
     *
     * @param event 生物伤害事件
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();

        // 获取伤害减免属性
        AttributeInstance attribute = entity.getAttribute(FELAttributes.DAMAGE_REDUCTION.get());
        if (attribute == null) {
            return;
        }

        // 获取伤害减免值（0.0-1.0）
        double reduction = attribute.getValue();

        // 确保减免在有效范围内
        reduction = Math.max(0.0, Math.min(1.0, reduction));

        // 如果有伤害减免，应用减免效果
        if (reduction > 0.0) {
            float originalDamage = event.getAmount();
            // 减免后伤害 = 原始伤害 × (1 - 减免比例)
            float reducedDamage = (float) (originalDamage * (1.0 - reduction));

            // 确保减免后的伤害不为负数
            reducedDamage = Math.max(0, reducedDamage);

            // 设置减免后的伤害
            event.setAmount(reducedDamage);
        }
    }
}