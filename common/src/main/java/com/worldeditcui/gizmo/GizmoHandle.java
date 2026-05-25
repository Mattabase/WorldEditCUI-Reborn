package com.worldeditcui.gizmo;

import net.minecraft.core.Direction;

/**
 * Represents a single gizmo handle — a grabbable point on the selection box.
 */
public enum GizmoHandle {
    EAST(Direction.EAST, 0xFFFF3333),   // Red — +X
    WEST(Direction.WEST, 0xFFCC2222),   // Dark Red — -X
    UP(Direction.UP, 0xFF33FF33),       // Green — +Y
    DOWN(Direction.DOWN, 0xFF22CC22),   // Dark Green — -Y
    SOUTH(Direction.SOUTH, 0xFF3333FF), // Blue — +Z
    NORTH(Direction.NORTH, 0xFF2222CC), // Dark Blue — -Z
    CENTER(null, 0xFFFFCC33);           // Gold — move

    private final Direction direction;
    private final int color;

    GizmoHandle(Direction direction, int color) {
        this.direction = direction;
        this.color = color;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getColor() {
        return color;
    }

    public int getHighlightColor() {
        // Brighten by blending toward white
        int r = Math.min(255, ((color >> 16) & 0xFF) + 60);
        int g = Math.min(255, ((color >> 8) & 0xFF) + 60);
        int b = Math.min(255, (color & 0xFF) + 60);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Returns the WorldEdit direction string for this handle.
     */
    public String getDirectionName() {
        if (direction == null) return "";
        return direction.getName();
    }

    /**
     * Whether this is a resize handle (all except CENTER).
     */
    public boolean isResize() {
        return direction != null;
    }
}
