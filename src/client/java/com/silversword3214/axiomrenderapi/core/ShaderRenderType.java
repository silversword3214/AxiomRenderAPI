package com.silversword3214.axiomrenderapi.core;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public class ShaderRenderType extends RenderType {
    private final ShaderProgram shader;

    private ShaderRenderType(ShaderProgram shader, RenderSetup setup) {
        super(shader.toString(), setup);
        this.shader = shader;
    }

    public static RenderType shader(ShaderProgram shader, Identifier texture) {
        // Build a RenderSetup using the pipeline from the shader
        RenderSetup setup = RenderSetup.builder(shader.getPipeline())
                .withTexture("Texture0", texture)   // Name can be anything, matches uniform in shader
                .createRenderSetup();
        return new ShaderRenderType(shader, setup);
    }
}