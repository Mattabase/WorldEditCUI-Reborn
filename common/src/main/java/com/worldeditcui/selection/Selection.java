package com.worldeditcui.selection;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the current WorldEdit selection state received via the CUI protocol.
 * Currently supports cuboid selections (the most common type).
 */
public class Selection {
    private String shapeType = "";
    private @Nullable BlockPos point0;
    private @Nullable BlockPos point1;
    private @Nullable BlockPos min;
    private @Nullable BlockPos max;

    public void setShapeType(String type) {
        this.shapeType = type;
        clear();
    }

    public String getShapeType() {
        return shapeType;
    }

    public void setPoint(int id, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (id == 0) {
            point0 = pos;
        } else if (id == 1) {
            point1 = pos;
        }
        recalculateBounds();
    }

    public @Nullable BlockPos getPoint0() {
        return point0;
    }

    public @Nullable BlockPos getPoint1() {
        return point1;
    }

    public @Nullable BlockPos getMin() {
        return min;
    }

    public @Nullable BlockPos getMax() {
        return max;
    }

    public boolean hasSelection() {
        return min != null && max != null;
    }

    /**
     * Returns the center of the selection as a double[3].
     */
    public double[] getCenter() {
        if (min == null || max == null) return new double[]{0, 0, 0};
        return new double[]{
                (min.getX() + max.getX() + 1) / 2.0,
                (min.getY() + max.getY() + 1) / 2.0,
                (min.getZ() + max.getZ() + 1) / 2.0
        };
    }

    /**
     * Returns the size of the selection along each axis.
     */
    public int[] getSize() {
        if (min == null || max == null) return new int[]{0, 0, 0};
        return new int[]{
                max.getX() - min.getX() + 1,
                max.getY() - min.getY() + 1,
                max.getZ() - min.getZ() + 1
        };
    }

    public void clear() {
        point0 = null;
        point1 = null;
        min = null;
        max = null;
    }

    private void recalculateBounds() {
        if (point0 != null && point1 != null) {
            min = new BlockPos(
                    Math.min(point0.getX(), point1.getX()),
                    Math.min(point0.getY(), point1.getY()),
                    Math.min(point0.getZ(), point1.getZ())
            );
            max = new BlockPos(
                    Math.max(point0.getX(), point1.getX()),
                    Math.max(point0.getY(), point1.getY()),
                    Math.max(point0.getZ(), point1.getZ())
            );
        }
    }
}
