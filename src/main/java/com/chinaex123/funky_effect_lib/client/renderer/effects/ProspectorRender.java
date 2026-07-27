package com.chinaex123.funky_effect_lib.client.renderer.effects;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.client.config.OreColorConfig;
import com.chinaex123.funky_effect_lib.effect.Prospector;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.Tags;
import org.joml.Matrix4f;

import java.util.Objects;

/**
 * 勘探者渲染类
 * <p>
 * 功能：高亮显示玩家周围的所有矿石
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class ProspectorRender {

    /** 高亮透明度 **/
    private static final float HIGHLIGHT_ALPHA = 1.0f;
    /** 线框宽度 **/
    private static final float LINE_WIDTH = 5.0f;
    /** 高亮扩展范围 **/
    private static final float HIGHLIGHT_EXPANSION = 0.05f;

    /**
     * 世界渲染阶段事件处理
     * 在实体渲染完成后绘制矿石高亮
     *
     * @param event 世界渲染阶段事件
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        // 仅在实体渲染完成后执行
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        // 检查玩家是否拥有勘探者效果
        if (!mc.player.hasEffect(FELEffects.PROSPECTOR)) return;

        // 获取效果等级并计算检测半径
        int amplifier = Objects.requireNonNull(mc.player.getEffect(FELEffects.PROSPECTOR)).getAmplifier();
        int detectionRadius = Prospector.getDetectionRadius(amplifier);

        // 渲染矿石高亮
        renderOreHighlights(event, mc.level, mc.player, detectionRadius);
    }

    /**
     * 渲染矿石高亮
     * 在检测半径内搜索矿石并用彩色线框高亮
     *
     * @param event 世界渲染阶段事件
     * @param level 世界对象
     * @param player 玩家实体
     * @param radius 检测半径
     */
    private static void renderOreHighlights(RenderLevelStageEvent event, Level level, LivingEntity player, int radius) {
        PoseStack poseStack = event.getPoseStack();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        // 平移坐标系到相机位置
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // 创建搜索区域
        AABB searchArea = new AABB(player.blockPosition()).inflate(radius);

        // 设置渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(LINE_WIDTH);

        // 获取Tesselator实例并开始绘制线框
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // 遍历搜索区域内的所有方块
        BlockPos.betweenClosedStream(searchArea).forEach(pos -> {
            try {
                BlockState state = level.getBlockState(pos);
                // 检查是否为矿石
                if (state.is(Tags.Blocks.ORES)) {
                    renderBlockOutline(poseStack, buffer, pos, state, level);
                }
            } catch (Exception e) {
                // 忽略渲染异常
            }
        });

        // 构建网格数据并绘制
        MeshData meshData = buffer.build();
        if (meshData != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferUploader.drawWithShader(meshData);
        }

        // 恢复渲染状态
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);

        poseStack.popPose();
    }

    /**
     * 获取方块的边界框
     * 优先使用方块的碰撞箱，如果为空则使用整方块
     *
     * @param pos 方块位置
     * @param state 方块状态
     * @param level 世界对象
     * @return 边界框
     */
    private static AABB getBoundingBox(BlockPos pos, BlockState state, Level level) {
        try {
            VoxelShape shape = state.getShape(level, pos);
            return shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);
        } catch (Exception e) {
            return new AABB(pos);
        }
    }

    /**
     * 渲染单个方块的线框
     *
     * @param poseStack 姿态栈
     * @param buffer BufferBuilder
     * @param pos 方块位置
     * @param state 方块状态
     * @param level 世界对象
     */
    private static void renderBlockOutline(PoseStack poseStack, BufferBuilder buffer, BlockPos pos, BlockState state, Level level) {
        AABB boundingBox = getBoundingBox(pos, state, level);
        // 忽略过小的方块
        if (boundingBox.getSize() < 0.1) return;

        // 稍微扩大边界使线框更明显
        boundingBox = boundingBox.inflate(HIGHLIGHT_EXPANSION);

        // 从配置类获取该矿石的颜色
        OreColorConfig.OreColor color = OreColorConfig.getColor(state.getBlock());

        // 绘制彩色线框
        drawLineBox(poseStack, buffer, boundingBox,
                color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), HIGHLIGHT_ALPHA);
    }

    /**
     * 绘制方块的12条边线框
     *
     * @param poseStack 姿态栈
     * @param buffer BufferBuilder
     * @param box 边界框
     * @param r 红色分量 (0-1)
     * @param g 绿色分量 (0-1)
     * @param b 蓝色分量 (0-1)
     * @param a 透明度 (0-1)
     */
    private static void drawLineBox(PoseStack poseStack, BufferBuilder buffer, AABB box, float r, float g, float b, float a) {
        Matrix4f matrix = poseStack.last().pose();

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // 底面四条边 (y = minY)
        vertex(matrix, buffer, minX, minY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, minY, minZ, r, g, b, a);

        // 顶面四条边 (y = maxY)
        vertex(matrix, buffer, minX, maxY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, maxZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, maxY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, maxY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, maxY, minZ, r, g, b, a);

        // 垂直四条棱 (连接底面和顶面)
        vertex(matrix, buffer, minX, minY, minZ, r, g, b, a);
        vertex(matrix, buffer, minX, maxY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, maxY, maxZ, r, g, b, a);
    }

    /**
     * 添加顶点到BufferBuilder
     *
     * @param matrix 变换矩阵
     * @param buffer BufferBuilder
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @param r 红色分量 (0-1)
     * @param g 绿色分量 (0-1)
     * @param b 蓝色分量 (0-1)
     * @param a 透明度 (0-1)
     */
    private static void vertex(Matrix4f matrix, BufferBuilder buffer, float x, float y, float z, float r, float g, float b, float a) {
        buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }
}