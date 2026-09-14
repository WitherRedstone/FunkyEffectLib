package com.chinaex123.funky_effect_lib.effect;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.chinaex123.funky_effect_lib.init.FELSounds;
import com.chinaex123.funky_effect_lib.network.effect.DiastoleSyncPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/**
 * 舒张：也许预示着不详...
 * <p>
 * 该效果会周期性地叠加层数，
 * 叠满4层后强制实体跳跃一次，随后进入冷却，形成循环。
 * <p>
 * 核心机制：
 * <ul>
 *   <li>每26刻叠加一层，最多4层</li>
 *   <li>叠到4层时强制将实体跳跃一次（跳跃速度0.70）</li>
 *   <li>跳跃后进入3秒（80刻）冷却时间</li>
 *   <li>冷却结束后重新开始叠层</li>
 *   <li>每次叠层时播放对应的DIASTOLE音效</li>
 *   <li>HUD显示：□□□□ → ■■■■ 表示叠层进度</li>
 * </ul>
 * <p>
 * 状态流转：
 * <pre>
 * 叠层1 → 叠层2 → 叠层3 → 叠层4 → 跳跃 → 冷却80刻 → 重新叠层
 * </pre>
 * <p>
 * 数据存储：使用实体的持久化数据（PersistentData）保存叠层状态，
 * 通过 {@link DiastoleSyncPacket} 同步到客户端用于HUD显示。
 */
@EventBusSubscriber(modid = FunkyEffectLib.MOD_ID)
public class Diastole extends MobEffect {

    /** 叠层间隔（刻） */
    private static final int STACK_INTERVAL = 25;
    /** 冷却时间（刻） */
    private static final int COOLDOWN_TIME = 80;
    /** 最大叠层数，达到此值触发跳跃 */
    private static final int MAX_STACKS = 4;
    /** 跳跃速度 */
    private static final double JUMP_VELOCITY = 0.80;

    /** NBT键名：当前叠层数 */
    private static final String STACKS_KEY = "diastole_stacks";
    /** NBT键名：最后一次叠层的时间 */
    private static final String LAST_STACK_TIME_KEY = "diastole_last_stack_time";
    /** NBT键名：冷却结束时间 */
    private static final String COOLDOWN_END_KEY = "diastole_cooldown_end";
    /** NBT键名：是否已发送重置同步包 */
    private static final String RESET_SENT_KEY = "diastole_reset_sent";
    /** 重置同步延迟（刻） */
    private static final int RESET_DELAY = 10;

    public Diastole(int color) {
        super(MobEffectCategory.NEUTRAL, color);
    }

    /**
     * 每帧应用效果时调用的方法
     * <p>
     * 叠层逻辑在 {@link #onLivingTick(EntityTickEvent.Post)} 中处理，
     * 此处留空实现
     *
     * @param entity 拥有该效果的实体
     * @param amplifier 效果等级（从0开始）
     */
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
     * <p>
     * 管理舒张效果的完整生命周期：
     * <ol>
     *   <li>检查效果是否存在，不存在则清除数据</li>
     *   <li>如果处于冷却期，检查是否需要发送重置同步包</li>
     *   <li>如果超过叠层间隔，叠加一层并播放音效</li>
     *   <li>叠满4层时触发跳跃并进入冷却</li>
     *   <li>每层变化时同步数据到客户端</li>
     * </ol>
     *
     * @param event 实体Tick事件
     */
    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        // 仅在服务端执行逻辑
        if (entity.level().isClientSide()) {
            return;
        }

        // 检查实体是否拥有舒张效果
        MobEffectInstance effect = entity.getEffect(FELEffects.DIASTOLE);
        if (effect == null) {
            // 效果不存在时清除所有相关数据
            clearDiastoleData(entity);
            return;
        }

        CompoundTag nbt = entity.getPersistentData();
        long currentTime = entity.level().getGameTime();

        // 读取当前状态数据
        int stacks = nbt.getInt(STACKS_KEY);
        long lastStackTime = nbt.getLong(LAST_STACK_TIME_KEY);
        long cooldownEnd = nbt.getLong(COOLDOWN_END_KEY);
        boolean resetSent = nbt.getBoolean(RESET_SENT_KEY);

        // 冷却期处理
        if (currentTime < cooldownEnd) {
            // 冷却期间：如果叠层已重置但尚未通知客户端，延迟后发送重置同步包
            if (!resetSent && stacks == 0 && lastStackTime > 0 && currentTime - lastStackTime >= RESET_DELAY) {
                if (entity instanceof ServerPlayer player) {
                    PacketDistributor.sendToPlayer(player,
                            new DiastoleSyncPacket(player.getUUID(), 0, cooldownEnd));
                    nbt.putBoolean(RESET_SENT_KEY, true);
                }
            }
            return;
        }

        // 判断是否达到叠层间隔（首次叠层或距离上次叠层超过间隔）
        if (lastStackTime == 0 || currentTime - lastStackTime >= STACK_INTERVAL) {
            stacks++;
            nbt.putInt(STACKS_KEY, stacks);
            nbt.putLong(LAST_STACK_TIME_KEY, currentTime);
            nbt.putBoolean(RESET_SENT_KEY, false);

            // 根据叠层数播放对应的音效
            switch (stacks) {
                case 1:
                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            FELSounds.DIASTOLE_1.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    break;
                case 2:
                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            FELSounds.DIASTOLE_2.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    break;
                case 3:
                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            FELSounds.DIASTOLE_3.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    break;
                case 4:
                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            FELSounds.DIASTOLE_4.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    break;
            }

            // ===== 达到最大叠层，触发跳跃 =====
            if (stacks >= MAX_STACKS) {
                // 施加向上的跳跃速度
                entity.setDeltaMovement(entity.getDeltaMovement().x, JUMP_VELOCITY, entity.getDeltaMovement().z);
                // 标记速度已改变，强制同步到客户端
                entity.hurtMarked = true;

                // 重置叠层并进入冷却
                nbt.putInt(STACKS_KEY, 0);
                nbt.putLong(COOLDOWN_END_KEY, currentTime + COOLDOWN_TIME);

                spawnBloomParticles(entity);

                // 同步数据到客户端
                if (entity instanceof ServerPlayer player) {
                    PacketDistributor.sendToPlayer(player,
                            new DiastoleSyncPacket(player.getUUID(), stacks, cooldownEnd));
                }
            } else {
                // 未满层时同步当前叠层进度
                if (entity instanceof ServerPlayer player) {
                    PacketDistributor.sendToPlayer(player,
                            new DiastoleSyncPacket(player.getUUID(), stacks, cooldownEnd));
                }
            }
        }
    }

    /**
     * 清除舒张效果相关数据
     * <p>
     * 当效果被移除或实体不再拥有该效果时，
     * 清除所有持久化数据并发送重置同步包给客户端
     *
     * @param entity 目标实体
     */
    private static void clearDiastoleData(LivingEntity entity) {
        CompoundTag nbt = entity.getPersistentData();
        // 移除所有相关NBT数据
        nbt.remove(STACKS_KEY);
        nbt.remove(LAST_STACK_TIME_KEY);
        nbt.remove(COOLDOWN_END_KEY);
        nbt.remove(RESET_SENT_KEY);

        // 通知客户端重置HUD显示
        if (entity instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player,
                    new DiastoleSyncPacket(player.getUUID(), 0, 0));
        }
    }

    /**
     * 生成绽放的白色粒子效果
     * <p>
     * 在实体脚下生成三种不同类型的白色粒子，形成绽放效果：
     * <ul>
     *   <li>20个白色烟雾粒子：形成基础光环</li>
     *   <li>10个末影烛粒子：增加闪烁效果</li>
     *   <li>15个雪花粒子：增加飘散效果</li>
     * </ul>
     *
     * @param entity 目标实体
     */
    private static void spawnBloomParticles(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            double x = entity.getX();
            double y = entity.getY();
            double z = entity.getZ();
            
            // 生成20个烟雾粒子，形成基础光环
            for (int i = 0; i < 20; i++) {
                double angle = (i * 18.0) * (Math.PI / 180.0);
                double radius = 0.5 + serverLevel.random.nextDouble() * 0.5;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double offsetY = serverLevel.random.nextDouble() * 0.3;
                
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        x + offsetX, y + offsetY, z + offsetZ,
                        1, 0, 0.05, 0, 0.02);
            }
            
            // 生成10个末影烛粒子，增加闪烁效果
            for (int i = 0; i < 10; i++) {
                double angle = (i * 36.0) * (Math.PI / 180.0);
                double radius = 0.8 + serverLevel.random.nextDouble() * 0.3;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                double offsetY = serverLevel.random.nextDouble() * 0.2;
                
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        x + offsetX, y + offsetY, z + offsetZ,
                        1, 0, 0.03, 0, 0.01);
            }
            
            // 生成15个雪花粒子，增加飘散效果
            for (int i = 0; i < 15; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5) * 1.2;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 1.2;
                double offsetY = serverLevel.random.nextDouble() * 0.4;
                
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        x + offsetX, y + offsetY, z + offsetZ,
                        1, 0, 0.02, 0, 0.01);
            }
        }
    }
}