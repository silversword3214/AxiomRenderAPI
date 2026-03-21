package com.silversword3214.axiomrenderapi.integration;

import com.silversword3214.axiomrenderapi.RenderAPI;
import com.silversword3214.axiomrenderapi.api.Renderer3D;
import com.silversword3214.axiomrenderapi.event.Render3DEvent;
import com.silversword3214.axiomrenderapi.event.RenderEventDispatcher;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class FabricWorldHook {
    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            RenderAPI api = RenderAPI.getInstance();
            Renderer3D renderer = api.beginWorld(context);
            float tickDelta = renderer.getTickDelta();

            // Get camera position from the game renderer
            Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();

            // Dispatch the event – external listeners will react
            RenderEventDispatcher.dispatchRender3D(new Render3DEvent(
                    renderer,
                    tickDelta,
                    cameraPos,
                    renderer.getProjectionMatrix(),
                    renderer.getViewMatrix()
            ));

            api.end();
        });
    }
}