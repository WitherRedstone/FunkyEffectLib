// client/effect/ProspectorRender.java
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

/** 勘探者渲染类：高亮显示周围的矿石 **/
@EventBusSubscriber(value = Dist.CLIENT, modid = FunkyEffectLib.MOD_ID)
public class ProspectorRender {

    private static final float HIGHLIGHT_ALPHA = 1.0f;
    private static final float LINE_WIDTH = 5.0f;
    private static final float HIGHLIGHT_EXPANSION = 0.05f;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (!mc.player.hasEffect(FELEffects.PROSPECTOR)) return;

        int amplifier = Objects.requireNonNull(mc.player.getEffect(FELEffects.PROSPECTOR)).getAmplifier();
        int detectionRadius = Prospector.getDetectionRadius(amplifier);

        renderOreHighlights(event, mc.level, mc.player, detectionRadius);
    }

    private static void renderOreHighlights(RenderLevelStageEvent event, Level level, LivingEntity player, int radius) {
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
                if (state.is(Tags.Blocks.ORES)) {
                    renderBlockOutline(poseStack, buffer, pos, state, level);
                }
            } catch (Exception e) {
                // 忽略渲染异常
            }
        });

        MeshData meshData = buffer.build();
        if (meshData != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferUploader.drawWithShader(meshData);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);

        poseStack.popPose();
    }

    private static AABB getBoundingBox(BlockPos pos, BlockState state, Level level) {
        try {
            VoxelShape shape = state.getShape(level, pos);
            return shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);
        } catch (Exception e) {
            return new AABB(pos);
        }
    }

    private static void renderBlockOutline(PoseStack poseStack, BufferBuilder buffer, BlockPos pos, BlockState state, Level level) {
        AABB boundingBox = getBoundingBox(pos, state, level);
        if (boundingBox.getSize() < 0.1) return;

        boundingBox = boundingBox.inflate(HIGHLIGHT_EXPANSION);

        // 从配置类获取颜色
        OreColorConfig.OreColor color = OreColorConfig.getColor(state.getBlock());

        drawLineBox(poseStack, buffer, boundingBox,
                color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), HIGHLIGHT_ALPHA);
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

        // 垂直四条棱
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