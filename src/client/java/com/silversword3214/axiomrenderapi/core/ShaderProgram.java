package com.silversword3214.axiomrenderapi.core;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ShaderProgram {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShaderProgram.class);
    private final RenderPipeline pipeline;

    public ShaderProgram(Identifier vertex, Identifier fragment, VertexFormat.Mode mode) throws Exception {
        // Always use DEBUG_FILLED_SNIPPET as base (which uses POSITION_COLOR)
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withVertexShader(vertex)
                .withFragmentShader(fragment)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withDepthWrite(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, mode)
                .withCull(false)
                .withLocation(Identifier.fromNamespaceAndPath("axiomrenderapi", "shader_" + mode.name().toLowerCase(Locale.ROOT)));

        this.pipeline = RenderPipelines.register(builder.build());
        precompile();
        LOGGER.info("Shader program compiled: {} (mode={})", vertex, mode);
    }

    private void precompile() {
        LOGGER.info("Precompiling pipeline for shader: {}", pipeline.getLocation());
        GpuDevice device = RenderSystem.getDevice();
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        device.precompilePipeline(pipeline, (id, shaderType) -> {
            try {
                var optional = resourceManager.getResource(id);
                if (optional.isEmpty()) {
                    LOGGER.error("Shader not found: {}", id);
                    return null;
                }
                Resource resource = optional.get();
                try (InputStream in = resource.open()) {
                    String source = IOUtils.toString(in, StandardCharsets.UTF_8);
                    LOGGER.info("Loaded shader {} ({} bytes)", id, source.length());
                    return source;
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load shader: " + id, e);
            }
        });
    }

    public void use() {
        // Not used – the pipeline is set during the render pass
    }

    public void setUniform(String name, float value) {
        // Not used – uniforms are handled via the render pass
    }

    public void setUniform(String name, float v1, float v2) {}
    public void setUniform(String name, float v1, float v2, float v3, float v4) {}
    public void setUniform(String name, Matrix4f matrix) {}

    @Nullable
    public RenderPipeline getPipeline() {
        return pipeline;
    }
}