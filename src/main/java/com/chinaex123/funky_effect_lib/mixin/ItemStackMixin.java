package com.chinaex123.funky_effect_lib.mixin;

import com.chinaex123.funky_effect_lib.effect.KnowledgeHardening;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 物品堆栈混入类
 */
@Mixin(ItemStack.class)
public class ItemStackMixin {

    /**
     * 注入到耐久损耗方法
     * 在耐久损耗前检查知识固化效果，将耐久损耗转为经验值消耗
     *
     * @param amount 损耗的耐久值
     * @param entity 使用物品的实体
     * @param slot 装备槽位
     * @param ci 回调信息（用于取消原方法）
     */
    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onHurtAndBreak(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        // 只处理玩家，且仅在服务端执行
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // 检查是否拥有知识固化效果
        MobEffectInstance effect = player.getEffect(FELEffects.KNOWLEDGE_HARDENING);
        if (effect != null) {
            int amplifier = effect.getAmplifier();

            // 计算每点耐久消耗的经验值：基础 - 等级 × 每级减少量，最低为1
            int xpPerDurability = KnowledgeHardening.BASE_XP_PER_DURABILITY - (amplifier * KnowledgeHardening.XP_REDUCTION_PER_LEVEL);
            xpPerDurability = Math.max(1, xpPerDurability);

            // 计算总消耗的经验值
            int xpCost = amount * xpPerDurability;
            // 获取玩家当前总经验值
            int currentXp = fel$getPlayerTotalExperience(player);

            if (currentXp >= xpCost) {
                // 经验充足：扣除经验值，阻止耐久损耗
                fel$subtractPlayerExperience(player, xpCost);
                ci.cancel(); // 取消原方法，阻止耐久减少
            }
            // 经验不足时，允许正常损耗耐久
        }
    }

    /**
     * 获取玩家总经验值
     * 计算等级经验 + 当前进度经验
     *
     * @param player 玩家对象
     * @return 总经验值
     */
    @Unique
    private static int fel$getPlayerTotalExperience(Player player) {
        int level = player.experienceLevel;
        int progress = (int) (player.experienceProgress * player.getXpNeededForNextLevel());
        return fel$getExperienceForLevel(level) + progress;
    }

    /**
     * 计算指定等级所需的总经验值
     * 使用Minecraft原版经验计算公式
     *
     * @param level 等级
     * @return 达到该等级所需的总经验值
     */
    @Unique
    private static int fel$getExperienceForLevel(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    /**
     * 扣除玩家经验值
     *
     * @param player 玩家对象
     * @param amount 扣除的经验值数量
     */
    @Unique
    private static void fel$subtractPlayerExperience(Player player, int amount) {
        player.giveExperiencePoints(-amount);
    }
}