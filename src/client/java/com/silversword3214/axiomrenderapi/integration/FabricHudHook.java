package com.silversword3214.axiomrenderapi.integration;

import com.silversword3214.axiomrenderapi.RenderAPI;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;


public class FabricHudHook {
    public static void register() {
        HudRenderCallback.EVENT.register((GuiGraphics graphics, DeltaTracker deltaTracker) -> {
            RenderAPI api = RenderAPI.getInstance();
            float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
            api.beginHUD(graphics, tickDelta);
            api.end();

        });
    }
}