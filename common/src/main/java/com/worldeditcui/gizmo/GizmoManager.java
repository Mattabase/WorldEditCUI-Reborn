package com.worldeditcui.gizmo;

import com.worldeditcui.WorldEditCUIReborn;
import com.worldeditcui.selection.Selection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Manages gizmo interaction with focus-based input:
 *   Right-click a handle to focus it.
 *   Scroll wheel (or Up/Down arrow) to expand/contract/shift.
 *   Right-click again to unfocus.
 */
public class GizmoManager {
    private static final double HANDLE_SIZE = 0.35;
    private static final double HANDLE_INTERACT_RANGE = 64.0;

    private final Selection selection;
    private @Nullable GizmoHandle hoveredHandle;
    private @Nullable GizmoHandle focusedHandle;
    private boolean wasRightDown;
    private boolean wasUpDown;
    private boolean wasDownDown;

    public static long lastGizmoActionTime = 0;

    public GizmoManager(Selection selection) {
        this.selection = selection;
    }

    public @Nullable GizmoHandle getHoveredHandle() {
        return hoveredHandle;
    }

    public @Nullable GizmoHandle getFocusedHandle() {
        return focusedHandle;
    }

    /** Used by GizmoRenderer — returns focused handle for highlight rendering. */
    public @Nullable GizmoHandle getDraggedHandle() {
        return focusedHandle;
    }

    public boolean isFocused() {
        return focusedHandle != null;
    }

    public void tick() {
        if (!selection.hasSelection()) {
            hoveredHandle = null;
            focusedHandle = null;
            return;
        }

        var mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.screen != null) {
            wasRightDown = false;
            wasUpDown = false;
            wasDownDown = false;
            return;
        }

        updateHover(mc);

        long window = mc.getWindow().handle();

        // Right-click edge detection — toggle focus
        boolean rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (rightDown && !wasRightDown) {
            if (focusedHandle != null) {
                focusedHandle = null;
            } else if (hoveredHandle != null) {
                focusedHandle = hoveredHandle;
            }
        }
        wasRightDown = rightDown;

        // Arrow key edge detection (only when focused)
        if (focusedHandle != null) {
            int arrowMult = com.worldeditcui.config.Config.get().invertArrowKeys ? -1 : 1;
            
            boolean upDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS;
            if (upDown && !wasUpDown) {
                applyAdjustment(focusedHandle, 1 * arrowMult);
            }
            wasUpDown = upDown;

            boolean downDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS;
            if (downDown && !wasDownDown) {
                applyAdjustment(focusedHandle, -1 * arrowMult);
            }
            wasDownDown = downDown;
        } else {
            wasUpDown = false;
            wasDownDown = false;
        }
    }

    private void updateHover(Minecraft mc) {
        Vec3 eyePos = mc.player.getEyePosition(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        Vec3 lookVec = mc.player.getViewVector(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        Vec3 endPos = eyePos.add(lookVec.scale(HANDLE_INTERACT_RANGE));

        hoveredHandle = null;
        double closestDist = Double.MAX_VALUE;

        for (GizmoHandle handle : GizmoHandle.values()) {
            Vec3 center = getHandleCenter(handle);
            if (center == null) continue;

            AABB box = new AABB(
                    center.x - HANDLE_SIZE, center.y - HANDLE_SIZE, center.z - HANDLE_SIZE,
                    center.x + HANDLE_SIZE, center.y + HANDLE_SIZE, center.z + HANDLE_SIZE
            );

            var hitResult = box.clip(eyePos, endPos);
            if (hitResult.isPresent()) {
                double dist = hitResult.get().distanceToSqr(eyePos);
                if (dist < closestDist) {
                    closestDist = dist;
                    hoveredHandle = handle;
                }
            }
        }
    }

    /**
     * Called from platform scroll event hooks. Returns true if consumed (cancel vanilla).
     */
    public boolean onScroll(double delta) {
        if (focusedHandle == null || !selection.hasSelection()) return false;
        int amount = delta > 0 ? 1 : -1;
        if (com.worldeditcui.config.Config.get().invertScrollDirection) {
            amount = -amount;
        }
        applyAdjustment(focusedHandle, amount);
        return true;
    }

    private void applyAdjustment(GizmoHandle handle, int amount) {
        lastGizmoActionTime = System.currentTimeMillis();
        if (handle.isResize()) {
            String dir = handle.getDirectionName();
            if (amount > 0) {
                WorldEditCUIReborn.sendWorldEditCommand("expand " + amount + " " + dir);
            } else {
                // Contract uses opposite direction so it affects the focused face
                String opposite = handle.getDirection().getOpposite().getName();
                WorldEditCUIReborn.sendWorldEditCommand("contract " + (-amount) + " " + opposite);
            }
        } else {
            // CENTER — shift in direction the player is looking (including up/down)
            var mc = Minecraft.getInstance();
            if (mc.player == null) return;
            float pitch = mc.player.getXRot();
            Direction facing;
            if (pitch < -45.0f) {
                facing = Direction.UP;
            } else if (pitch > 45.0f) {
                facing = Direction.DOWN;
            } else {
                facing = mc.player.getDirection();
            }
            if (amount > 0) {
                WorldEditCUIReborn.sendWorldEditCommand("shift " + amount + " " + facing.getName());
            } else {
                WorldEditCUIReborn.sendWorldEditCommand("shift " + (-amount) + " " + facing.getOpposite().getName());
            }
        }
    }

    public @Nullable Vec3 getHandleCenter(GizmoHandle handle) {
        if (!selection.hasSelection()) return null;
        BlockPos min = selection.getMin();
        BlockPos max = selection.getMax();
        if (min == null || max == null) return null;

        double[] center = selection.getCenter();

        return switch (handle) {
            case EAST -> new Vec3(max.getX() + 1, center[1], center[2]);
            case WEST -> new Vec3(min.getX(), center[1], center[2]);
            case UP -> new Vec3(center[0], max.getY() + 1, center[2]);
            case DOWN -> new Vec3(center[0], min.getY(), center[2]);
            case SOUTH -> new Vec3(center[0], center[1], max.getZ() + 1);
            case NORTH -> new Vec3(center[0], center[1], min.getZ());
            case CENTER -> new Vec3(center[0], center[1], center[2]);
        };
    }

    public void reset() {
        focusedHandle = null;
        hoveredHandle = null;
        wasRightDown = false;
        wasUpDown = false;
        wasDownDown = false;
    }
}
