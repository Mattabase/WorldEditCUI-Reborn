package com.worldeditcui.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.worldeditcui.selection.Selection;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;

/**
 * Renders the selection box — a translucent filled box with wireframe edges.
 */
public final class SelectionRenderer {

    private SelectionRenderer() {}

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera, Selection selection) {
        if (!selection.hasSelection()) return;

        BlockPos min = selection.getMin();
        BlockPos max = selection.getMax();
        if (min == null || max == null) return;

        com.worldeditcui.config.Config.ConfigData config = com.worldeditcui.config.Config.get();

        // Camera-relative positions
        double camX = camera.position().x;
        double camY = camera.position().y;
        double camZ = camera.position().z;

        float x0 = (float) (min.getX() - camX);
        float y0 = (float) (min.getY() - camY);
        float z0 = (float) (min.getZ() - camZ);
        float x1 = (float) (max.getX() + 1 - camX);
        float y1 = (float) (max.getY() + 1 - camY);
        float z1 = (float) (max.getZ() + 1 - camZ);

        poseStack.pushPose();

        // Draw filled faces — inset slightly to prevent z-fighting with edge lines
        float e = 0.002f;
        VertexConsumer fill = bufferSource.getBuffer(RenderTypes.debugQuads());
        var pose = poseStack.last().pose();
        renderFilledBox(fill, pose, x0 + e, y0 + e, z0 + e, x1 - e, y1 - e, z1 - e, config.selectionFillColor);
        bufferSource.endLastBatch();

        // Draw wireframe edges using line segments
        VertexConsumer lines = bufferSource.getBuffer(config.renderSelectionEdgesThroughTerrain ? RenderUtils.SEE_THROUGH_LINES : RenderTypes.lines());
        renderEdgeLines(lines, poseStack, x0, y0, z0, x1, y1, z1, config.selectionEdgeColor);
        
        // Draw grid lines
        renderGridLines(lines, poseStack, min, max, (float)camX, (float)camY, (float)camZ, config.selectionGridColor);
        
        bufferSource.endLastBatch();

        poseStack.popPose();
    }

    private static void renderFilledBox(VertexConsumer consumer, org.joml.Matrix4f pose,
                                         float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        // Bottom face (Y-)
        consumer.addVertex(pose, x0, y0, z0).setColor(color);
        consumer.addVertex(pose, x1, y0, z0).setColor(color);
        consumer.addVertex(pose, x1, y0, z1).setColor(color);
        consumer.addVertex(pose, x0, y0, z1).setColor(color);

        // Top face (Y+)
        consumer.addVertex(pose, x0, y1, z0).setColor(color);
        consumer.addVertex(pose, x0, y1, z1).setColor(color);
        consumer.addVertex(pose, x1, y1, z1).setColor(color);
        consumer.addVertex(pose, x1, y1, z0).setColor(color);

        // North face (Z-)
        consumer.addVertex(pose, x0, y0, z0).setColor(color);
        consumer.addVertex(pose, x0, y1, z0).setColor(color);
        consumer.addVertex(pose, x1, y1, z0).setColor(color);
        consumer.addVertex(pose, x1, y0, z0).setColor(color);

        // South face (Z+)
        consumer.addVertex(pose, x0, y0, z1).setColor(color);
        consumer.addVertex(pose, x1, y0, z1).setColor(color);
        consumer.addVertex(pose, x1, y1, z1).setColor(color);
        consumer.addVertex(pose, x0, y1, z1).setColor(color);

        // West face (X-)
        consumer.addVertex(pose, x0, y0, z0).setColor(color);
        consumer.addVertex(pose, x0, y0, z1).setColor(color);
        consumer.addVertex(pose, x0, y1, z1).setColor(color);
        consumer.addVertex(pose, x0, y1, z0).setColor(color);

        // East face (X+)
        consumer.addVertex(pose, x1, y0, z0).setColor(color);
        consumer.addVertex(pose, x1, y1, z0).setColor(color);
        consumer.addVertex(pose, x1, y1, z1).setColor(color);
        consumer.addVertex(pose, x1, y0, z1).setColor(color);
    }

    /**
     * Renders 12 edges of the box using line segment pairs with normals.
     */
    private static void renderEdgeLines(VertexConsumer consumer, PoseStack poseStack,
                                         float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        var pose = poseStack.last();
        // Bottom ring (4 edges)
        line(consumer, pose, x0, y0, z0, x1, y0, z0, color);
        line(consumer, pose, x1, y0, z0, x1, y0, z1, color);
        line(consumer, pose, x1, y0, z1, x0, y0, z1, color);
        line(consumer, pose, x0, y0, z1, x0, y0, z0, color);
        // Top ring (4 edges)
        line(consumer, pose, x0, y1, z0, x1, y1, z0, color);
        line(consumer, pose, x1, y1, z0, x1, y1, z1, color);
        line(consumer, pose, x1, y1, z1, x0, y1, z1, color);
        line(consumer, pose, x0, y1, z1, x0, y1, z0, color);
        // Vertical edges (4 edges)
        line(consumer, pose, x0, y0, z0, x0, y1, z0, color);
        line(consumer, pose, x1, y0, z0, x1, y1, z0, color);
        line(consumer, pose, x1, y0, z1, x1, y1, z1, color);
        line(consumer, pose, x0, y0, z1, x0, y1, z1, color);
    }

    private static void renderGridLines(VertexConsumer consumer, PoseStack poseStack, BlockPos min, BlockPos max, float camX, float camY, float camZ, int color) {
        var pose = poseStack.last();
        float x0 = min.getX() - camX;
        float y0 = min.getY() - camY;
        float z0 = min.getZ() - camZ;
        float x1 = max.getX() + 1 - camX;
        float y1 = max.getY() + 1 - camY;
        float z1 = max.getZ() + 1 - camZ;

        // X-axis lines (along Z and Y faces)
        for (int x = min.getX() + 1; x <= max.getX(); x++) {
            float fx = x - camX;
            line(consumer, pose, fx, y0, z0, fx, y1, z0, color); // North face
            line(consumer, pose, fx, y0, z1, fx, y1, z1, color); // South face
            line(consumer, pose, fx, y0, z0, fx, y0, z1, color); // Bottom face
            line(consumer, pose, fx, y1, z0, fx, y1, z1, color); // Top face
        }

        // Y-axis lines (along X and Z faces)
        for (int y = min.getY() + 1; y <= max.getY(); y++) {
            float fy = y - camY;
            line(consumer, pose, x0, fy, z0, x1, fy, z0, color); // North face
            line(consumer, pose, x0, fy, z1, x1, fy, z1, color); // South face
            line(consumer, pose, x0, fy, z0, x0, fy, z1, color); // West face
            line(consumer, pose, x1, fy, z0, x1, fy, z1, color); // East face
        }

        // Z-axis lines (along X and Y faces)
        for (int z = min.getZ() + 1; z <= max.getZ(); z++) {
            float fz = z - camZ;
            line(consumer, pose, x0, y0, fz, x0, y1, fz, color); // West face
            line(consumer, pose, x1, y0, fz, x1, y1, fz, color); // East face
            line(consumer, pose, x0, y0, fz, x1, y0, fz, color); // Bottom face
            line(consumer, pose, x0, y1, fz, x1, y1, fz, color); // Top face
        }
    }

    private static void line(VertexConsumer c, PoseStack.Pose pose,
                              float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-6f) return;
        dx /= len; dy /= len; dz /= len;
        c.addVertex(pose, x0, y0, z0).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(2.0f);
        c.addVertex(pose, x1, y1, z1).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(2.0f);
    }
}
