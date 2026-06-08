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

/** 寻宝者渲染类：高亮显示周围的战利品宝箱 **/
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class TreasureFinderRender {

    private static final int HIGHLIGHT_COLOR_R = 255; // 红色
    private static final int HIGHLIGHT_COLOR_G = 255; // 绿色
    private static final int HIGHLIGHT_COLOR_B = 255; // 蓝色
    private static final float HIGHLIGHT_ALPHA = 1.0f;  // 箱子高亮的扩展边界框透明度
    private static final float LINE_WIDTH = 5.0f;  // 线条粗细
    private static final float HIGHLIGHT_EXPANSION = 0.05f;  // 箱子高亮的扩展边界框大小

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (!mc.player.hasEffect(FELEffects.TREASURE_FINDER)) return;

        int amplifier = Objects.requireNonNull(mc.player.getEffect(FELEffects.TREASURE_FINDER)).getAmplifier();
        int detectionRadius = TreasureFinder.getDetectionRadius(amplifier);

        renderContainerHighlights(event, mc.level, mc.player, detectionRadius);
    }

    private static void renderContainerHighlights(RenderLevelStageEvent event, Level level, LivingEntity player, int radius) {
        PoseStack poseStack = event.getPoseStack();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        AABB searchArea = new AABB(player.blockPosition()).inflate(radius);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(LINE_WIDTH);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        BlockPos.betweenClosedStream(searchArea).forEach(pos -> {
            try {
                BlockState state = level.getBlockState(pos);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (isContainer(state, blockEntity)) {
                    renderBlockOutline(poseStack, buffer, pos, state, level);
                }
            } catch (Exception e) {
                // 静默处理
            }
        });

        level.getEntities((Entity) null, searchArea, entity -> entity.getType() == EntityType.CHEST_MINECART).forEach(entity -> {
            try {
                AABB entityBox = entity.getBoundingBox();
                if (entityBox.getSize() >= 0.1) {
                    Vec3 center = entity.position();
                    float red = 255 / 255.0f;
                    float green = 165 / 255.0f;
                    float blue = 0 / 255.0f;
                    
                    AABB expandedEntityBox = entityBox.inflate(HIGHLIGHT_EXPANSION);
                    drawLineBox(poseStack, buffer, expandedEntityBox, red, green, blue, HIGHLIGHT_ALPHA);
                    
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

    private static boolean isContainer(BlockState state, BlockEntity blockEntity) {
        return blockEntity instanceof MenuProvider;
    }

    private static AABB getBoundingBox(BlockPos pos, BlockState state, Level level) {
        try {
            VoxelShape shape = state.getShape(level, pos);
            AABB boundingBox = shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);

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
                }
            }
            return boundingBox;
        } catch (Exception e) {
            return new AABB(pos);
        }
    }

    private static void renderBlockOutline(PoseStack poseStack, BufferBuilder buffer, BlockPos pos, BlockState state, Level level) {
        AABB boundingBox = getBoundingBox(pos, state, level);
        if (boundingBox.getSize() < 0.1) return;

        boundingBox = boundingBox.inflate(HIGHLIGHT_EXPANSION);
        float red = HIGHLIGHT_COLOR_R / 255.0f;
        float green = HIGHLIGHT_COLOR_G / 255.0f;
        float blue = HIGHLIGHT_COLOR_B / 255.0f;

        drawLineBox(poseStack, buffer, boundingBox, red, green, blue, HIGHLIGHT_ALPHA);
    }

    private static void drawLineBox(PoseStack poseStack, BufferBuilder buffer, AABB box, float r, float g, float b, float a) {
        Matrix4f matrix = poseStack.last().pose();

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // 底面四条边
        vertex(matrix, buffer, minX, minY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, minY, minZ, r, g, b, a);

        // 顶面四条边
        vertex(matrix, buffer, minX, maxY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, maxZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, maxY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, maxY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, maxY, minZ, r, g, b, a);

        // 四条垂直边
        vertex(matrix, buffer, minX, minY, minZ, r, g, b, a);
        vertex(matrix, buffer, minX, maxY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, minZ, r, g, b, a);
        vertex(matrix, buffer, maxX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, maxX, maxY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, minY, maxZ, r, g, b, a);
        vertex(matrix, buffer, minX, maxY, maxZ, r, g, b, a);
    }

    private static void vertex(Matrix4f matrix, BufferBuilder buffer, float x, float y, float z, float r, float g, float b, float a) {
        buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }
}