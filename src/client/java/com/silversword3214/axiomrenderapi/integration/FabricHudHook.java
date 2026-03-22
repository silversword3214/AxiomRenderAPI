package com.silversword3214.axiomrenderapi.integration;

import com.silversword3214.axiomrenderapi.RenderAPI;
import com.silversword3214.axiomrenderapi.api.Renderer2D;
import com.silversword3214.axiomrenderapi.event.RenderEventDispatcher;
import com.silversword3214.axiomrenderapi.event.RenderHUDEvent;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;


public class FabricHudHook {
    public static void register() {
        HudRenderCallback.EVENT.register((GuiGraphics graphics, DeltaTracker deltaTracker) -> {
            RenderAPI api = RenderAPI.getInstance();
            float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
            api.beginHUD(graphics, tickDelta);
            Renderer2D renderer = api.hud();
            RenderHUDEvent event = new RenderHUDEvent(renderer, tickDelta, graphics);
            RenderEventDispatcher.dispatchRenderHUD(event);
            api.end();
        });
    }
}