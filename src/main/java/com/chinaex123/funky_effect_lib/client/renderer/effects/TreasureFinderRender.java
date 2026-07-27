package com.chinaex123.funky_effect_lib.client.renderer.effects;

import com.chinaex123.funky_effect_lib.FunkyEffectLib;
import com.chinaex123.funky_effect_lib.effect.TreasureFinder;
import com.chinaex123.funky_effect_lib.init.FELEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.Objects;

/**
 * 寻宝者渲染类
 * <p>
 * 功能：高亮显示玩家周围的所有战利品容器（箱子、运输矿车等）
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class TreasureFinderRender {

    /** 箱子高亮颜色（亮白色） **/
    private static final int HIGHLIGHT_COLOR_R = 255;
    private static final int HIGHLIGHT_COLOR_G = 255;
    private static final int HIGHLIGHT_COLOR_B = 255;

    /** 箱子高亮透明度 **/
    private static final float HIGHLIGHT_ALPHA = 1.0f;
    /** 线框宽度 **/
    private static final float LINE_WIDTH = 5.0f;
    /** 高亮扩展范围 **/
    private static final float HIGHLIGHT_EXPANSION = 0.05f;

    /**
     * 世界渲染阶段事件处理
     * 在实体渲染完成后绘制容器高亮
     *
     * @param event 世界渲染阶段事件
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        // 仅在实体渲染完成后执行
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        // 检查玩家是否拥有寻宝者效果
        if (!mc.player.hasEffect(FELEffects.TREASURE_FINDER)) return;

        // 获取效果等级并计算检测半径
        int amplifier = Objects.requireNonNull(mc.player.getEffect(FELEffects.TREASURE_FINDER)).getAmplifier();
        int detectionRadius = TreasureFinder.getDetectionRadius(amplifier);

        // 渲染容器高亮
        renderContainerHighlights(event, mc.level, mc.player, detectionRadius);
    }

    /**
     * 渲染容器高亮
     * 在检测半径内搜索容器并用彩色线框高亮
     *
     * @param event 世界渲染阶段事件
     * @param level 世界对象
     * @param player 玩家实体
     * @param radius 检测半径
     */
    private static void renderContainerHighlights(RenderLevelStageEvent event, Level level, LivingEntity player, int radius) {
        PoseStack poseStack = event.getPoseStack();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        // 平移坐标系到相机位置
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // 创建搜索区域（以玩家为中心）
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

        // 渲染方块容器
        BlockPos.betweenClosedStream(searchArea).forEach(pos -> {
            try {
                BlockState state = level.getBlockState(pos);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                // 检查是否为容器（实现了MenuProvider接口）
                if (isContainer(state, blockEntity)) {
                    renderBlockOutline(poseStack, buffer, pos, state, level);
                }
            } catch (Exception e) {
                // 静默处理，避免影响其他方块
            }
        });

        // 渲染运输矿车
        level.getEntities((Entity) null, searchArea, entity -> entity.getType() == EntityType.CHEST_MINECART).forEach(entity -> {
            try {
                AABB entityBox = entity.getBoundingBox();
                if (entityBox.getSize() >= 0.1) {
                    Vec3 center = entity.position();
                    float red = 255 / 255.0f;
                    float green = 165 / 255.0f;
                    float blue = 0 / 255.0f;  // 橙色

                    // 绘制运输矿车的碰撞箱
                    AABB expandedEntityBox = entityBox.inflate(HIGHLIGHT_EXPANSION);
                    drawLineBox(poseStack, buffer, expandedEntityBox, red, green, blue, HIGHLIGHT_ALPHA);

                    // 绘制运输矿车上的箱子部分
                    AABB chestBox = new AABB(
                            center.x - 0.375,
                            center.y + 0.4,
                            center.z - 0.375,
                            center.x + 0.375,
                            center.y + 1.1,
                            center.z + 0.375
                    );
                    AABB expandedChestBox = chestBox.inflate(0.005);
                    drawLineBox(poseStack, buffer, expandedChestBox, red, green, blue, HIGHLIGHT_ALPHA);
                }
            } catch (Exception e) {
                // 静默处理
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
     * 判断是否为容器
     *
     * @param state 方块状态
     * @param blockEntity 方块实体
     * @return true表示是容器
     */
    private static boolean isContainer(BlockState state, BlockEntity blockEntity) {
        return blockEntity instanceof MenuProvider;
    }

    /**
     * 获取方块的边界框
     * 支持大型箱子的合并边界框
     *
     * @param pos 方块位置
     * @param state 方块状态
     * @param level 世界对象
     * @return 边界框
     */
    private static AABB getBoundingBox(BlockPos pos, BlockState state, Level level) {
        try {
            VoxelShape shape = state.getShape(level, pos);
            AABB boundingBox = shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);

            // 处理大型箱子（双箱子合并）
            if (state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                try {
                    BlockPos connectedPos = pos.relative(ChestBlock.getConnectedDirection(state));
                    BlockState connectedState = level.getBlockState(connectedPos);
                    if (connectedState.hasProperty(ChestBlock.TYPE) &&
                            connectedState.getValue(ChestBlock.TYPE) != ChestType.SINGLE &&
                            ChestBlock.getConnectedDirection(connectedState).getOpposite() ==
                                    ChestBlock.getConnectedDirection(state)) {
                        if (state.getValue(ChestBlock.TYPE) == ChestType.RIGHT) {
                            return new AABB(0, 0, 0, 0, 0, 0);
                        }
                        VoxelShape connectedShape = connectedState.getShape(level, connectedPos);
                        if (!connectedShape.isEmpty()) {
                            boundingBox = boundingBox.minmax(connectedShape.bounds().move(connectedPos));
                        }
                    }
                } catch (Exception e) {
                    // 忽略合并异常
                }
            }
            return boundingBox;
        } catch (Exception e) {
            return new AABB(pos);
        }
    }

    /**
     * 渲染单个容器的线框
     *
     * @param poseStack 姿态栈
     * @param buffer BufferBuilder
     * @param pos 方块位置
     * @param state 方块状态
     * @param level 世界对象
     */
    private static void renderBlockOutline(PoseStack poseStack, BufferBuilder buffer, BlockPos pos, BlockState state, Level level) {
        AABB boundingBox = getBoundingBox(pos, state, level);
        if (boundingBox.getSize() < 0.1) return;

        // 稍微扩大边界使线框更明显
        boundingBox = boundingBox.inflate(HIGHLIGHT_EXPANSION);
        float red = HIGHLIGHT_COLOR_R / 255.0f;
        float green = HIGHLIGHT_COLOR_G / 255.0f;
        float blue = HIGHLIGHT_COLOR_B / 255.0f;

        // 绘制亮白色线框
        drawLineBox(poseStack, buffer, boundingBox, red, green, blue, HIGHLIGHT_ALPHA);
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