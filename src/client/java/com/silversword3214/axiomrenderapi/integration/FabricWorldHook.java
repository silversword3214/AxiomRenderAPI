package com.silversword3214.axiomrenderapi.integration;

import com.silversword3214.axiomrenderapi.RenderAPI;
import com.silversword3214.axiomrenderapi.test.ESP;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

public class FabricWorldHook {
    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            RenderAPI api = RenderAPI.getInstance();
            api.beginWorld(context);
            ESP.getInstance().render();
            api.end();
        });
    }
}