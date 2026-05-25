package com.worldeditcui;

import com.worldeditcui.gizmo.GizmoManager;
import com.worldeditcui.protocol.CUIProtocolHandler;
import com.worldeditcui.render.GizmoRenderer;
import com.worldeditcui.render.SelectionRenderer;
import com.worldeditcui.selection.Selection;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class for WorldEditCUI Reborn.
 * Client-side only — renders WorldEdit selections with interactive gizmo handles.
 */
public final class WorldEditCUIReborn {
    public static final String MOD_ID = "worldeditcui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Selection selection = new Selection();
    private static final CUIProtocolHandler protocolHandler = new CUIProtocolHandler(selection);
    private static final GizmoManager gizmoManager = new GizmoManager(selection);
    private static boolean initialized = false;

    public static Selection getSelection() {
        return selection;
    }

    public static CUIProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    public static GizmoManager getGizmoManager() {
        return gizmoManager;
    }

    /**
     * Called from platform initializers to mark the mod as ready.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;
        com.worldeditcui.config.Config.load();
        LOGGER.info("WorldEditCUI Reborn initialized");
    }

    /**
     * Called from platform render hooks after translucent rendering.
     */
    public static void onRenderWorld(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera) {
        if (!selection.hasSelection()) return;

        SelectionRenderer.render(poseStack, bufferSource, camera, selection);
        GizmoRenderer.render(poseStack, bufferSource, camera, selection, gizmoManager);
        bufferSource.endLastBatch();
    }

    /**
     * Called when the player disconnects from a server.
     */
    public static void onDisconnect() {
        selection.clear();
        gizmoManager.reset();
    }

    /**
     * Sends a WorldEdit command (e.g., "expand 3 north") from the client.
     * The command should NOT include the // prefix.
     */
    public static void sendWorldEditCommand(String command) {
        var mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.connection.sendCommand("/" + command);
        }
    }
}
