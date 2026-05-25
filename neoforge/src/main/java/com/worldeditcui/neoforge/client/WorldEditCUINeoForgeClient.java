package com.worldeditcui.neoforge.client;

import com.worldeditcui.WorldEditCUIReborn;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import org.enginehub.worldeditcui.protocol.CUIPacketHandler;

@Mod(value = WorldEditCUIReborn.MOD_ID, dist = Dist.CLIENT)
public class WorldEditCUINeoForgeClient {

    public WorldEditCUINeoForgeClient(net.neoforged.fml.ModContainer container) {
        WorldEditCUIReborn.init();

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, parent) -> new com.worldeditcui.config.ConfigScreen(parent));

        // Register CUI packet handler
        CUIPacketHandler.instance().registerClientboundHandler((packet, ctx) -> {
            ctx.workExecutor().execute(() -> {
                WorldEditCUIReborn.getProtocolHandler().handleEvent(
                        packet.multi(),
                        packet.eventType(),
                        packet.args().toArray(new String[0])
                );
            });
        });

        // Register game event listeners
        NeoForge.EVENT_BUS.register(GameEvents.class);
    }

    @EventBusSubscriber(modid = WorldEditCUIReborn.MOD_ID, value = Dist.CLIENT)
    public static class GameEvents {

        private static int delayedHelo = 0;

        @SubscribeEvent
        public static void onSystemChat(ClientChatReceivedEvent.System event) {
            if (com.worldeditcui.config.Config.get().hideGizmoChatFeedback) {
                if (System.currentTimeMillis() - com.worldeditcui.gizmo.GizmoManager.lastGizmoActionTime < 100) {
                    String msg = event.getMessage().getString();
                    if (msg.startsWith("Region ") || msg.startsWith("Selection ")) {
                        event.setCanceled(true);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentParticles event) {
            var mc = Minecraft.getInstance();
            var bufferSource = mc.renderBuffers().bufferSource();
            var camera = mc.gameRenderer.getMainCamera();
            WorldEditCUIReborn.onRenderWorld(
                    event.getPoseStack(),
                    bufferSource,
                    camera
            );
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            var mc = Minecraft.getInstance();
            if (mc.player != null) {
                WorldEditCUIReborn.getGizmoManager().tick();

                // Delayed CUI handshake — wait for command system to initialize
                if (delayedHelo > 0) {
                    delayedHelo--;
                    if (delayedHelo == 0) {
                        try {
                            mc.player.connection.sendCommand("we cui");
                        } catch (Exception e) {
                            WorldEditCUIReborn.LOGGER.warn("Failed to send CUI handshake", e);
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
            // Delay handshake by 10 ticks to let command system initialize
            delayedHelo = 10;
        }

        @SubscribeEvent
        public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            delayedHelo = 0;
            WorldEditCUIReborn.onDisconnect();
        }

        @SubscribeEvent
        public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            if (WorldEditCUIReborn.getGizmoManager().onScroll(event.getScrollDeltaY())) {
                event.setCanceled(true);
            }
        }
    }
}
