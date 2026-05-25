package com.worldeditcui.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.worldeditcui.WorldEditCUIReborn;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import org.enginehub.worldeditcui.protocol.CUIPacket;
import org.enginehub.worldeditcui.protocol.CUIPacketHandler;

public class WorldEditCUIFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldEditCUIReborn.init();

        // Register to receive CUI packets from WorldEdit
        CUIPacketHandler.instance().registerClientboundHandler((packet, ctx) -> {
            ctx.workExecutor().execute(() -> {
                WorldEditCUIReborn.getProtocolHandler().handleEvent(
                        packet.multi(),
                        packet.eventType(),
                        packet.args().toArray(new String[0])
                );
            });
        });

        // Send CUI handshake when joining a server
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientPlayNetworking.send(new CUIPacket("v", CUIPacket.protocolVersion()));
        });

        // Clear selection on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            WorldEditCUIReborn.onDisconnect();
        });

        // Intercept chat feedback
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (com.worldeditcui.config.Config.get().hideGizmoChatFeedback) {
                if (System.currentTimeMillis() - com.worldeditcui.gizmo.GizmoManager.lastGizmoActionTime < 100) {
                    String msg = message.getString();
                    if (msg.startsWith("Region ") || msg.startsWith("Selection ")) {
                        return false; // Cancel message
                    }
                }
            }
            return true;
        });

        // Tick gizmo hover detection
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                WorldEditCUIReborn.getGizmoManager().tick();
            }
        });

        // Render selection and gizmos
        LevelRenderEvents.END_MAIN.register(context -> {
            var mc = Minecraft.getInstance();
            var bufferSource = mc.renderBuffers().bufferSource();
            var camera = mc.gameRenderer.getMainCamera();
            WorldEditCUIReborn.onRenderWorld(
                    new PoseStack(),
                    bufferSource,
                    camera
            );
        });
    }
}
