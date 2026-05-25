package com.worldeditcui.protocol;

import com.worldeditcui.WorldEditCUIReborn;
import com.worldeditcui.selection.Selection;

/**
 * Parses incoming CUI protocol events from WorldEdit and updates the selection state.
 * 
 * CUI protocol format: eventType|arg1|arg2|...
 * Key events:
 *   s|cuboid         — set shape type
 *   p|id|x|y|z|area  — set point
 *   p2|x|y|z|area    — set point (alternate format)
 *   mm|min|max        — min/max Y bounds (unused for cuboid)
 */
public class CUIProtocolHandler {
    private final Selection selection;

    public CUIProtocolHandler(Selection selection) {
        this.selection = selection;
    }

    /**
     * Handles a raw CUI event string. Called when a CUI packet is received.
     * @param multi whether this is multi-mode (multi-selection) — currently ignored
     * @param eventType the event type string (e.g., "s", "p", "p2")
     * @param args the event arguments
     */
    public void handleEvent(boolean multi, String eventType, String[] args) {
        try {
            switch (eventType) {
                case "s" -> handleShape(args);
                case "p" -> handlePoint(args);
                case "p2" -> handlePoint2(args);
                case "cyl" -> handleShape(new String[]{"cylinder"});
                case "e" -> handleEllipsoid(args);
                case "mm" -> {} // min/max Y — not needed for cuboid rendering
                default -> WorldEditCUIReborn.LOGGER.debug("Unhandled CUI event: {} args: {}", eventType, String.join("|", args));
            }
        } catch (Exception e) {
            WorldEditCUIReborn.LOGGER.error("Error handling CUI event '{}': {}", eventType, e.getMessage());
        }
    }

    private void handleShape(String[] args) {
        if (args.length < 1) return;
        String type = args[0];
        WorldEditCUIReborn.LOGGER.debug("Selection shape: {}", type);
        selection.setShapeType(type);
    }

    private void handlePoint(String[] args) {
        // Format: p|id|x|y|z|area
        if (args.length < 4) return;
        int id = Integer.parseInt(args[0]);
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        int z = Integer.parseInt(args[3]);
        WorldEditCUIReborn.LOGGER.debug("Point {}: {}, {}, {}", id, x, y, z);
        selection.setPoint(id, x, y, z);
    }

    private void handlePoint2(String[] args) {
        // Alternate format: p2|x|y|z|area
        if (args.length < 3) return;
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        int z = Integer.parseInt(args[2]);
        WorldEditCUIReborn.LOGGER.debug("Point2: {}, {}, {}", x, y, z);
        selection.setPoint(1, x, y, z);
    }

    private void handleEllipsoid(String[] args) {
        // Placeholder — ellipsoid not fully supported yet
        WorldEditCUIReborn.LOGGER.debug("Ellipsoid event (not yet supported)");
    }
}
