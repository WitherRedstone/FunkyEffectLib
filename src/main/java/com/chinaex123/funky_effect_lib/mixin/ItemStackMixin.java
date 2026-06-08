package com.chinaex123.funky_effect_lib.mixin;

import com.chinaex123.funky_effect_lib.effect.KnowledgeHardening;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 混合类，用于修改物品耐久损耗 **/
@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onHurtAndBreak(int amount, LivingEntity entity, net.minecraft.world.entity.EquipmentSlot slot, CallbackInfo ci) {
        // 只处理玩家
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // 检查是否有知识固化效果
        MobEffectInstance effect = player.getEffect(FELEffects.KNOWLEDGE_HARDENING);
        if (effect != null) {
            int amplifier = effect.getAmplifier();

            // 计算每耐久消耗的经验值，随等级降低
            // 公式：基础值 - 等级 × 每级减少量，最低为1
            int xpPerDurability = KnowledgeHardening.BASE_XP_PER_DURABILITY - (amplifier * KnowledgeHardening.XP_REDUCTION_PER_LEVEL);
            xpPerDurability = Math.max(1, xpPerDurability);

            // 计算总消耗的经验值
            int xpCost = amount * xpPerDurability;
            // 获取玩家当前经验值
            int currentXp = funkyEffectLib_getPlayerTotalExperience(player);

            if (currentXp >= xpCost) {
                // 扣除经验，阻止耐久损耗
                funkyEffectLib_subtractPlayerExperience(player, xpCost);
                ci.cancel(); // 阻止耐久减少
            }
            // 经验不足时，允许正常损耗
        }
    }

    @Unique
    private static int funkyEffectLib_getPlayerTotalExperience(Player player) {
        int level = player.experienceLevel;
        int progress = (int) (player.experienceProgress * player.getXpNeededForNextLevel());
        return funkyEffectLib_getExperienceForLevel(level) + progress;
    }

    @Unique
    private static int funkyEffectLib_getExperienceForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    @Unique
    private static void funkyEffectLib_subtractPlayerExperience(Player player, int amount) {
        player.giveExperiencePoints(-amount);
    }
}