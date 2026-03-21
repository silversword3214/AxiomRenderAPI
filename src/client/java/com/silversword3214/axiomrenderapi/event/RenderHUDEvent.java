package com.silversword3214.axiomrenderapi.event;

import com.silversword3214.axiomrenderapi.api.Renderer2D;
import net.minecraft.client.gui.GuiGraphics;

public class RenderHUDEvent extends RenderEvent {
    private final Renderer2D renderer;
    private final GuiGraphics guiGraphics;

    public RenderHUDEvent(Renderer2D renderer, float tickDelta, GuiGraphics guiGraphics) {
        super(tickDelta);
        this.renderer = renderer;
        this.guiGraphics = guiGraphics;
    }

    public Renderer2D getRenderer() {
        return renderer;
    }

    public GuiGraphics getGuiGraphics() {
        return guiGraphics;
    }
}