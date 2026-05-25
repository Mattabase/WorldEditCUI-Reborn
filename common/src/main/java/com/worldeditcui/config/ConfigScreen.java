package com.worldeditcui.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private EditBox fillColorBox;
    private EditBox edgeColorBox;
    private EditBox gridColorBox;

    public ConfigScreen(Screen parent) {
        super(Component.literal("WorldEditCUI Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int y = 40;
        int spacing = 24;
        int centerX = this.width / 2;

        Config.ConfigData config = Config.get();

        this.addRenderableWidget(Button.builder(
                Component.literal("Invert Scroll: " + config.invertScrollDirection),
                btn -> {
                    config.invertScrollDirection = !config.invertScrollDirection;
                    btn.setMessage(Component.literal("Invert Scroll: " + config.invertScrollDirection));
                }
        ).bounds(centerX - 100, y, 200, 20).build());
        y += spacing;

        this.addRenderableWidget(Button.builder(
                Component.literal("Invert Arrow Keys: " + config.invertArrowKeys),
                btn -> {
                    config.invertArrowKeys = !config.invertArrowKeys;
                    btn.setMessage(Component.literal("Invert Arrow Keys: " + config.invertArrowKeys));
                }
        ).bounds(centerX - 100, y, 200, 20).build());
        y += spacing;

        this.addRenderableWidget(Button.builder(
                Component.literal("See-Through Edges: " + config.renderSelectionEdgesThroughTerrain),
                btn -> {
                    config.renderSelectionEdgesThroughTerrain = !config.renderSelectionEdgesThroughTerrain;
                    btn.setMessage(Component.literal("See-Through Edges: " + config.renderSelectionEdgesThroughTerrain));
                }
        ).bounds(centerX - 100, y, 200, 20).build());
        y += spacing;

        this.addRenderableWidget(Button.builder(
                Component.literal("Hide Gizmo Chat: " + config.hideGizmoChatFeedback),
                btn -> {
                    config.hideGizmoChatFeedback = !config.hideGizmoChatFeedback;
                    btn.setMessage(Component.literal("Hide Gizmo Chat: " + config.hideGizmoChatFeedback));
                }
        ).bounds(centerX - 100, y, 200, 20).build());
        y += spacing;

        // Colors
        fillColorBox = new EditBox(this.font, centerX + 10, y, 90, 20, Component.literal("Fill Color"));
        fillColorBox.setValue(Integer.toHexString(config.selectionFillColor).toUpperCase());
        this.addRenderableWidget(fillColorBox);
        y += spacing;

        edgeColorBox = new EditBox(this.font, centerX + 10, y, 90, 20, Component.literal("Edge Color"));
        edgeColorBox.setValue(Integer.toHexString(config.selectionEdgeColor).toUpperCase());
        this.addRenderableWidget(edgeColorBox);
        y += spacing;

        gridColorBox = new EditBox(this.font, centerX + 10, y, 90, 20, Component.literal("Grid Color"));
        gridColorBox.setValue(Integer.toHexString(config.selectionGridColor).toUpperCase());
        this.addRenderableWidget(gridColorBox);
        y += spacing;

        this.addRenderableWidget(Button.builder(
                Component.literal("Save & Close"),
                btn -> this.onClose()
        ).bounds(centerX - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        
        int y = 40 + 24 * 3;
        int spacing = 24;
        int centerX = this.width / 2;
        
        graphics.text(this.font, "Fill Color (ARGB Hex):", centerX - 100, y + 6, 0xFFFFFF);
        y += spacing;
        graphics.text(this.font, "Edge Color (ARGB Hex):", centerX - 100, y + 6, 0xFFFFFF);
        y += spacing;
        graphics.text(this.font, "Grid Color (ARGB Hex):", centerX - 100, y + 6, 0xFFFFFF);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Config.ConfigData config = Config.get();
        try {
            config.selectionFillColor = (int) Long.parseLong(fillColorBox.getValue(), 16);
            config.selectionEdgeColor = (int) Long.parseLong(edgeColorBox.getValue(), 16);
            config.selectionGridColor = (int) Long.parseLong(gridColorBox.getValue(), 16);
        } catch (NumberFormatException ignored) {}
        
        Config.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
