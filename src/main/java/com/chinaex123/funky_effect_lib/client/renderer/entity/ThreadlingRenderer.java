package com.chinaex123.funky_effect_lib.client.renderer.entity;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.model.ThreadlingModel;
import com.chinaex123.funky_effect_lib.entity.Threadling;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 线虫实体的渲染器
 * <p>
 * 负责将线虫模型渲染到游戏中，包括纹理加载、缩放控制和阴影设置
 */
public class ThreadlingRenderer extends MobRenderer<Threadling, ThreadlingModel> {

    /** Threadling实体的纹理资源位置 */
    private static final ResourceLocation THREADLING_TEXTURE = FunkyEffectLib.id("textures/entity/threadling.png");

    /**
     * 构造线虫渲染器
     *
     * @param context 实体渲染器提供者上下文，用于获取模型层和渲染资源
     */
    public ThreadlingRenderer(EntityRendererProvider.Context context) {
        // 调用父类构造器，传入模型实例和阴影大小
        super(context, new ThreadlingModel(context.bakeLayer(ThreadlingModel.LAYER_LOCATION)), 0.3F);
        // 设置阴影半径，控制实体在地面上投影的大小
        this.shadowRadius = 0.15F;
    }

    /**
     * 获取实体的纹理资源位置
     *
     * @param entity 线虫实体实例
     * @return 纹理资源位置对象
     */
    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Threadling entity) {
        return THREADLING_TEXTURE;
    }

    /**
     * 对模型进行缩放处理
     * <p>
     * 将线虫模型缩放到原始大小的0.5倍，使其在游戏中显得更小巧
     *
     * @param entity 线虫实体实例
     * @param poseStack 姿态堆栈，用于应用变换
     * @param partialTick 部分渲染刻数，用于平滑插值
     */
    @Override
    protected void scale(@NotNull Threadling entity, PoseStack poseStack, float partialTick) {
        // 在X、Y、Z三个轴向上统一缩放到0.5倍
        poseStack.scale(0.5F, 0.5F, 0.5F);
    }

    /**
     * 判断实体身体是否可见
     * <p>
     * 始终返回true，确保线虫实体在任何情况下都被渲染
     *
     * @param entity 线虫实体实例
     * @return 始终返回true，表示身体可见
     */
    @Override
    protected boolean isBodyVisible(@NotNull Threadling entity) {
        return true;
    }
}