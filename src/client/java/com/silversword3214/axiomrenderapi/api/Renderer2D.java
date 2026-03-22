package com.silversword3214.axiomrenderapi.api;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.silversword3214.axiomrenderapi.core.AxiomVertexFormats;
import com.silversword3214.axiomrenderapi.core.RenderCore;
import com.silversword3214.axiomrenderapi.core.MatrixUtil;
import com.silversword3214.axiomrenderapi.core.RenderPipelines;
import com.silversword3214.axiomrenderapi.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;

public class Renderer2D {
    private final GuiGraphics graphics;
    private final RenderCore core;
    private final Matrix4f projection;

    // Renderer2D.java
    // Renderer2D.java
    public Renderer2D(GuiGraphics graphics, RenderCore core, boolean useScaledCoordinates) {
        this.graphics = graphics;
        this.core = core;
        Matrix4f proj = useScaledCoordinates
                ? RenderUtils.getScaledProjection(graphics)
                : RenderUtils.getUnscaledProjection();
        this.projection = proj;
        this.core.beginFrame(this.projection, new Matrix4f().identity());
    }

    // Voit myös säilyttää vanhan konstruktorin, joka käyttää skaalattuja koordinaatteja
    public Renderer2D(GuiGraphics graphics, RenderCore core) {
        this(graphics, core, true);
    }

    public void drawRect(float x, float y, float width, float height, int color) {
        core.addRect2D(x, y, width, height, color);
    }

    public void drawRectOutline(float x, float y, float width, float height, float thickness, int color) {
        core.addRectOutline2D(x, y, width, height, thickness, color);
    }

    public void drawLine(float x1, float y1, float x2, float y2, float thickness, int color) {
        core.addLine2D(x1, y1, x2, y2, thickness, color);
    }

}