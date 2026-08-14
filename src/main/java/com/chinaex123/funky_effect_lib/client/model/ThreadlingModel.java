package com.chinaex123.funky_effect_lib.client.model;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.entity.Threadling;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/**
 * 线虫实体模型类
 * <p>
 * 负责渲染线虫实体的分段身体模型，身体由4个分段组成，
 * 每个分段具有独立的尺寸、纹理坐标和动画效果
 */
public class ThreadlingModel extends HierarchicalModel<Threadling> {

    /** 模型层位置，用于注册和获取模型 */
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            FunkyEffectLib.id("threadling"), "main"
    );

    /** 身体分段数量 */
    private static final int BODY_COUNT = 4;
    /** 每个身体分段的尺寸数组 [宽度, 高度, 深度] */
    private static final int[][] BODY_SIZES = new int[][]{
            {4, 3, 2},  // 分段0：较小
            {6, 4, 5},  // 分段1：最大
            {3, 3, 1},  // 分段2：较薄
            {1, 2, 1}   // 分段3：尾部最小
    };
    /** 每个身体分段的纹理坐标偏移 [u, v] */
    private static final int[][] BODY_TEXS = new int[][]{
            {0, 0},    // 分段0的纹理起始位置
            {0, 5},    // 分段1的纹理起始位置
            {0, 14},   // 分段2的纹理起始位置
            {0, 18}    // 分段3的纹理起始位置
    };

    /** 模型根节点 */
    private final ModelPart root;
    /** 身体分段模型部件数组 */
    private final ModelPart[] bodyParts;

    /**
     * 构造线虫模型
     *
     * @param root 模型根节点
     */
    public ThreadlingModel(ModelPart root) {
        this.root = root;
        this.bodyParts = new ModelPart[4];
        // 从根节点中获取所有身体分段部件
        for (int i = 0; i < 4; i++) {
            this.bodyParts[i] = root.getChild(createSegmentName(i));
        }
    }

    /**
     * 生成身体分段的名称
     *
     * @param index 分段索引 (0-3)
     * @return 分段名称字符串
     */
    private static String createSegmentName(int index) {
        return "segment" + index;
    }

    /**
     * 创建模型的图层定义
     * <p>
     * 定义模型的所有立方体部件及其位置和尺寸
     *
     * @return 图层定义对象
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 初始Z轴偏移，控制第一个分段的位置
        float zOffset = -3.5F;

        // 循环创建4个身体分段
        for (int i = 0; i < 4; i++) {
            int[] size = BODY_SIZES[i];
            int[] tex = BODY_TEXS[i];

            // 添加或替换身体分段部件
            partdefinition.addOrReplaceChild(
                    createSegmentName(i),
                    CubeListBuilder.create()
                            .texOffs(tex[0], tex[1])  // 设置纹理偏移
                            .addBox(
                                    (float) size[0] * -0.5F,  // X轴居中
                                    0.0F,                      // Y轴基准
                                    (float) size[2] * -0.5F,  // Z轴居中
                                    (float) size[0],          // 宽度
                                    (float) size[1],          // 高度
                                    (float) size[2]           // 深度
                            ),
                    PartPose.offset(0.0F, (float) (24 - size[1]), zOffset)  // 位置偏移
            );

            // 计算下一个分段的位置，使分段之间首尾相连
            if (i < 3) {
                zOffset += (float) (size[2] + BODY_SIZES[i + 1][2]) * 0.5F;
            }
        }

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    /**
     * 获取模型的根节点
     *
     * @return 根节点ModelPart
     */
    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }

    /**
     * 设置模型的动画
     * <p>
     * 为每个身体分段应用波浪式的摆动动画，模拟蠕动的效果
     *
     * @param entity 实体对象
     * @param limbSwing 摆动幅度
     * @param limbSwingAmount 摆动速度
     * @param ageInTicks 实体存活刻数
     * @param netHeadYaw 头部偏航角
     * @param headPitch 头部俯仰角
     */
    @Override
    public void setupAnim(@NotNull Threadling entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // 重置所有部件的姿态
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // 为每个身体分段计算动画
        for (int i = 0; i < this.bodyParts.length; i++) {
            // 偏航旋转：产生左右摆动的效果，每个分段有相位差
            this.bodyParts[i].yRot = Mth.cos(ageInTicks * 0.9F + (float) i * 0.15F * (float) Math.PI)
                    * (float) Math.PI * 0.01F * (float) (1 + Math.abs(i - 2));
            // X轴偏移：产生横向位移，中间分段摆动幅度更大
            this.bodyParts[i].x = Mth.sin(ageInTicks * 0.9F + (float) i * 0.15F * (float) Math.PI)
                    * (float) Math.PI * 0.1F * (float) Math.abs(i - 2);
        }
    }

    /**
     * 渲染模型到缓冲区
     * <p>
     * 遍历所有身体分段并分别渲染
     *
     * @param poseStack 姿态堆栈
     * @param vertexConsumer 顶点消费者
     * @param packedLight 打包的光照数据
     * @param packedOverlay 打包的覆盖数据
     * @param red 红色分量
     * @param green 绿色分量
     * @param blue 蓝色分量
     * @param alpha 透明度分量
     */
    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        for (ModelPart part : this.bodyParts) {
            part.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}