package com.worldeditcui.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.worldeditcui.gizmo.GizmoHandle;
import com.worldeditcui.gizmo.GizmoManager;
import com.worldeditcui.selection.Selection;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Renders the gizmo handles on the selection box — colored cubes at each face center
 * and a gold cube at the selection center for moving.
 */
public final class GizmoRenderer {
    private static final float HANDLE_HALF_SIZE = 0.3f;

    private GizmoRenderer() {}

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera,
                              Selection selection, GizmoManager gizmoManager) {
        if (!selection.hasSelection()) return;

        double camX = camera.position().x;
        double camY = camera.position().y;
        double camZ = camera.position().z;

        poseStack.pushPose();
        var pose = poseStack.last().pose();

        VertexConsumer fill = bufferSource.getBuffer(RenderUtils.SEE_THROUGH_QUADS);

        for (GizmoHandle handle : GizmoHandle.values()) {
            Vec3 center = gizmoManager.getHandleCenter(handle);
            if (center == null) continue;

            boolean isHovered = handle == gizmoManager.getHoveredHandle();
            boolean isDragged = handle == gizmoManager.getDraggedHandle();
            int color = (isHovered || isDragged) ? handle.getHighlightColor() : handle.getColor();

            float cx = (float) (center.x - camX);
            float cy = (float) (center.y - camY);
            float cz = (float) (center.z - camZ);

            float size = isDragged ? HANDLE_HALF_SIZE * 1.3f : (isHovered ? HANDLE_HALF_SIZE * 1.15f : HANDLE_HALF_SIZE);

            renderHandleCube(fill, pose, cx, cy, cz, size, color);
        }

        bufferSource.endLastBatch();

        // Draw handle outlines
        VertexConsumer lines = bufferSource.getBuffer(RenderUtils.SEE_THROUGH_LINES);
        for (GizmoHandle handle : GizmoHandle.values()) {
            Vec3 center = gizmoManager.getHandleCenter(handle);
            if (center == null) continue;

            boolean isHovered = handle == gizmoManager.getHoveredHandle();
            boolean isDragged = handle == gizmoManager.getDraggedHandle();
            int outlineColor = 0xFFFFFFFF; // White outlines
            if (!isHovered && !isDragged) outlineColor = 0xAACCCCCC; // Dimmer for non-hovered

            float cx = (float) (center.x - camX);
            float cy = (float) (center.y - camY);
            float cz = (float) (center.z - camZ);
            float size = isDragged ? HANDLE_HALF_SIZE * 1.3f : (isHovered ? HANDLE_HALF_SIZE * 1.15f : HANDLE_HALF_SIZE);

            renderHandleOutline(lines, poseStack, cx, cy, cz, size, outlineColor);
        }

        bufferSource.endLastBatch();
        poseStack.popPose();
    }

    private static void renderHandleCube(VertexConsumer consumer, org.joml.Matrix4f pose,
                                          float cx, float cy, float cz, float hs, int color) {
        float x0 = cx - hs, y0 = cy - hs, z0 = cz - hs;
        float x1 = cx + hs, y1 = cy + hs, z1 = cz + hs;

        // Bottom
        consumer.addVertex(pose, x0, y0, z0).setColor(color);
        consumer.addVertex(pose, x1, y0, z0).setColor(color);
        consumer.addVertex(pose, x1, y0, z1).setColor(color);
        consumer.addVertex(pose, x0, y0, z1).setColor(color);
        // Top
        consumer.addVertex(pose, x0, y1, z0).setColor(color);
        consumer.addVertex(pose, x0, y1, z1).setColor(color);
        consumer.addVertex(pose, x1, y1, z1).setColor(color);
        consumer.addVertex(pose, x1, y1, z0).setColor(color);
        // North
        consumer.addVertex(pose, x0, y0, z0).setColor(color);
        consumer.addVertex(pose, x0, y1, z0).setColor(color);
        consumer.addVertex(pose, x1, y1, z0).setColor(color);
        consumer.addVertex(pose, x1, y0, z0).setColor(color);
        // South
        consumer.addVertex(pose, x0, y0, z1).setColor(color);
        consumer.addVertex(pose, x1, y0, z1).setColor(color);
        consumer.addVertex(pose, x1, y1, z1).setColor(color);
        consumer.addVertex(pose, x0, y1, z1).setColor(color);
        // West
        consumer.addVertex(pose, x0, y0, z0).setColor(color);
        consumer.addVertex(pose, x0, y0, z1).setColor(color);
        consumer.addVertex(pose, x0, y1, z1).setColor(color);
        consumer.addVertex(pose, x0, y1, z0).setColor(color);
        // East
        consumer.addVertex(pose, x1, y0, z0).setColor(color);
        consumer.addVertex(pose, x1, y1, z0).setColor(color);
        consumer.addVertex(pose, x1, y1, z1).setColor(color);
        consumer.addVertex(pose, x1, y0, z1).setColor(color);
    }

    private static void renderHandleOutline(VertexConsumer consumer, PoseStack poseStack,
                                             float cx, float cy, float cz, float hs, int color) {
        float x0 = cx - hs, y0 = cy - hs, z0 = cz - hs;
        float x1 = cx + hs, y1 = cy + hs, z1 = cz + hs;

        var pose = poseStack.last();
        // Bottom ring
        line(consumer, pose, x0, y0, z0, x1, y0, z0, color);
        line(consumer, pose, x1, y0, z0, x1, y0, z1, color);
        line(consumer, pose, x1, y0, z1, x0, y0, z1, color);
        line(consumer, pose, x0, y0, z1, x0, y0, z0, color);
        // Top ring
        line(consumer, pose, x0, y1, z0, x1, y1, z0, color);
        line(consumer, pose, x1, y1, z0, x1, y1, z1, color);
        line(consumer, pose, x1, y1, z1, x0, y1, z1, color);
        line(consumer, pose, x0, y1, z1, x0, y1, z0, color);
        // Vertical edges
        line(consumer, pose, x0, y0, z0, x0, y1, z0, color);
        line(consumer, pose, x1, y0, z0, x1, y1, z0, color);
        line(consumer, pose, x1, y0, z1, x1, y1, z1, color);
        line(consumer, pose, x0, y0, z1, x0, y1, z1, color);
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
