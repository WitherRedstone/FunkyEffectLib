package com.chinaex123.funky_effect_lib.client.renderer;

import com.chinaex123.funky_effect_lib.entity.AfterimageClone;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 残影分身渲染类
 * <p>
 * 功能：渲染玩家释放的残影分身实体
 */
public class AfterimageCloneRenderer extends LivingEntityRenderer<AfterimageClone, PlayerModel<AfterimageClone>> {

    /** 默认Steve皮肤纹理 **/
    private static final ResourceLocation STEVE_SKIN = ResourceLocation.parse("textures/entity/player/wide/steve.png");
    /** 默认Alex皮肤纹理 **/
    private static final ResourceLocation ALEX_SKIN = ResourceLocation.parse("textures/entity/player/slim/alex.png");

    /**
     * 构造残影分身渲染器
     *
     * @param context 实体渲染器上下文
     */
    public AfterimageCloneRenderer(EntityRendererProvider.Context context) {
        // 使用玩家模型，isSlim参数为false
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    /**
     * 获取残影分身的纹理位置
     * 尝试根据原玩家名称获取对应的皮肤，失败则返回默认Steve皮肤
     *
     * @param entity 残影分身实体
     * @return 皮肤纹理资源位置
     */
    @Override
    public @NotNull ResourceLocation getTextureLocation(AfterimageClone entity) {
        // 获取原玩家的名称
        String ownerName = entity.getOwnerName();
        if (ownerName != null && !ownerName.isEmpty()) {
            // 通过网络连接获取玩家信息
            if (Minecraft.getInstance().getConnection() != null) {
                var playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(ownerName);
                if (playerInfo != null) {
                    // 获取玩家皮肤纹理
                    var skin = playerInfo.getSkin();
                    return skin.texture();
                }
            }
        }
        // 无法获取玩家皮肤，使用默认Steve皮肤
        return STEVE_SKIN;
    }

    /**
     * 缩放残影分身
     *
     * @param entity 残影分身实体
     * @param poseStack 姿态栈
     * @param partialTick 部分Tick时间
     */
    @Override
    protected void scale(@NotNull AfterimageClone entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.0f, 1.0f, 1.0f);
    }

    /**
     * 判断残影分身是否可见
     * 始终可见
     *
     * @param entity 残影分身实体
     * @return true表示可见
     */
    @Override
    protected boolean isBodyVisible(@NotNull AfterimageClone entity) {
        return true;
    }
}