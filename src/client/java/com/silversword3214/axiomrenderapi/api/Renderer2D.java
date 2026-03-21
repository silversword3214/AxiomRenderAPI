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


}