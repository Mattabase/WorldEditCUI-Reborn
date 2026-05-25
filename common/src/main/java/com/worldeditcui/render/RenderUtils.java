package com.worldeditcui.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.Optional;

public class RenderUtils {
    private static final RenderPipeline SEE_THROUGH_QUADS_PIPELINE = RenderPipeline.builder()
            .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("worldeditcui", "pipeline/see_through_quads"))
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withCull(false)
            .withDepthStencilState(Optional.empty())
            .build();

    private static final RenderPipeline SEE_THROUGH_LINES_PIPELINE = RenderPipeline.builder()
            .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("worldeditcui", "pipeline/see_through_lines"))
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("Fog", UniformType.UNIFORM_BUFFER)
            .withUniform("Globals", UniformType.UNIFORM_BUFFER)
            .withVertexShader("core/rendertype_lines")
            .withFragmentShader("core/rendertype_lines")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
            .withDepthStencilState(Optional.empty())
            .build();

    public static final RenderType SEE_THROUGH_QUADS = createRenderType("worldeditcui_see_through_quads",
            RenderSetup.builder(SEE_THROUGH_QUADS_PIPELINE).createRenderSetup());

    public static final RenderType SEE_THROUGH_LINES = createRenderType("worldeditcui_see_through_lines",
            RenderSetup.builder(SEE_THROUGH_LINES_PIPELINE).createRenderSetup());

    private static RenderType createRenderType(String name, RenderSetup setup) {
        try {
            java.lang.reflect.Method method = RenderType.class.getDeclaredMethod("create", String.class, RenderSetup.class);
            method.setAccessible(true);
            return (RenderType) method.invoke(null, name, setup);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create custom RenderType", e);
        }
    }
}
