package com.silversword3214.axiomrenderapi.api;

import com.silversword3214.axiomrenderapi.core.RenderCore;
import com.silversword3214.axiomrenderapi.core.MatrixUtil;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;

public class Renderer2D {
    private final GuiGraphics graphics;
    private final RenderCore core;
    private final Matrix4f projection;

    public Renderer2D(GuiGraphics graphics, RenderCore core) {
        this.graphics = graphics;
        this.core = core;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        this.projection = new Matrix4f().setOrtho(0, width, height, 0, -1000, 1000);
    }

    public void rect(float x, float y, float w, float h, int color) {
        Matrix3x2fStack pose = graphics.pose();
        Matrix4f model = MatrixUtil.toMatrix4f(pose, 0);
        core.addColoredQuad(model, projection, x, y, x + w, y + h, color);
    }

    public void roundedRect(float x, float y, float w, float h, float radius, int color) {
        Matrix3x2fStack pose = graphics.pose();
        Matrix4f model = MatrixUtil.toMatrix4f(pose, 0);
        core.addRoundedRect(model, projection, x, y, x + w, y + h, radius, color);
    }

    public void line(float x1, float y1, float x2, float y2, float thickness, int color) {
        Matrix3x2fStack pose = graphics.pose();
        Matrix4f model = MatrixUtil.toMatrix4f(pose, 0);
        core.addLine2D(model, projection, x1, y1, x2, y2, thickness, color);
    }
}