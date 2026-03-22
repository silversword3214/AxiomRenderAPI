package com.silversword3214.axiomrenderapi;

import com.silversword3214.axiomrenderapi.api.Renderer2D;
import com.silversword3214.axiomrenderapi.event.Render3DEvent;
import com.silversword3214.axiomrenderapi.event.RenderEventDispatcher;
import com.silversword3214.axiomrenderapi.integration.FabricHudHook;
import com.silversword3214.axiomrenderapi.integration.FabricWorldHook;
import com.silversword3214.axiomrenderapi.test.ESP;
import com.silversword3214.axiomrenderapi.test.Tracers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AxiomMod implements ClientModInitializer {
    public static final String MOD_ID = "axiomrenderapi";
    private static KeyMapping espKey;
    private static KeyMapping tracerKey;
    private static KeyMapping shaderESPKey;
    public static Minecraft mc;

    @Override
    public void onInitializeClient() {
        mc = Minecraft.getInstance();
        FabricHudHook.register();
        FabricWorldHook.register();

        // Register test modules as event listeners
        RenderEventDispatcher.addRender3DListener(event -> {
            ESP esp = ESP.getInstance();
            if (esp.isEnabled()) {
                esp.render(event.getRenderer(), event.getTickDelta());
            }
        });

        RenderEventDispatcher.addRender3DListener(event -> {
            Tracers tracers = Tracers.getInstance();
            if (tracers.isEnabled()) {
                tracers.render(event.getRenderer(), event.getTickDelta());
            }
        });

        // Keybindings for toggling
        espKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.axiomrenderapi.toggle_esp",
                GLFW.GLFW_KEY_R,
                KeyMapping.Category.MISC
        ));

        tracerKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.axiomrenderapi.toggle_tracers",
                GLFW.GLFW_KEY_G,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (espKey.consumeClick()) {
                ESP.getInstance().setEnabled(!ESP.getInstance().isEnabled());
                System.out.println("ESP toggled to: " + ESP.getInstance().isEnabled());
            }
            if (tracerKey.consumeClick()) {
                Tracers.getInstance().setEnabled(!Tracers.getInstance().isEnabled());
                System.out.println("Tracers toggled to: " + Tracers.getInstance().isEnabled());
            }
        });


        // AxiomMod.java, onInitializeClient-metodissa

        RenderEventDispatcher.addRenderHUDListener(event -> {
            Renderer2D r2d = event.getRenderer();

            // Pyöristetty suorakulmio (täytetty)
            r2d.drawRoundedRect(10, 10, 100, 100, 10, 0x88FF0000);

            // Pyöristetty reunus
            r2d.drawRoundedRectOutline(10, 120, 100, 100, 10, 2, 0xFF00FF00);

            // Ympyrä
            r2d.drawCircle(200, 60, 40, 0x880000FF);

            // Ympyrän reunus
            r2d.drawCircleOutline(200, 60, 40, 2, 0xFFFFFF00);
        });
    }
}