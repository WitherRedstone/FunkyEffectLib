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

/** 渲染的残影分身皮肤 **/
public class AfterimageCloneRenderer extends LivingEntityRenderer<AfterimageClone, PlayerModel<AfterimageClone>> {

    private static final ResourceLocation STEVE_SKIN = ResourceLocation.parse("textures/entity/player/wide/steve.png");
    private static final ResourceLocation ALEX_SKIN = ResourceLocation.parse("textures/entity/player/slim/alex.png");

    public AfterimageCloneRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(AfterimageClone entity) {
        String ownerName = entity.getOwnerName();
        if (ownerName != null && !ownerName.isEmpty()) {
            // 直接使用 Minecraft 的皮肤管理器获取
            if (Minecraft.getInstance().getConnection() != null) {
                var playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(ownerName);
                if (playerInfo != null) {
                    // 直接返回玩家皮肤位置
                    return playerInfo.getSkinLocation();
                }
            }
        }
        return STEVE_SKIN;
    }

    @Override
    protected void scale(AfterimageClone entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.0f, 1.0f, 1.0f);
    }

    @Override
    protected boolean isBodyVisible(AfterimageClone entity) {
        return true;
    }
}