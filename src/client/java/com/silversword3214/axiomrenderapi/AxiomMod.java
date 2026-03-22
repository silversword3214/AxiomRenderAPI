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
import net.minecraft.client.gui.GuiGraphics;
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


            // 1. Perusneliö (punainen täytetty)
            r2d.drawRect(10, 10, 100, 100, 0xFFFF0000);

            // 2. Neliön reunus (sininen, paksuus 2)
            r2d.drawRectOutline(10, 120, 100, 100, 2, 0xFF0000FF);

            // 3. Viiva (vihreä)
            r2d.drawLine(10, 230, 110, 330, 2, 0xFF00FF00);

            // 4. Ympyrä (täytetty, läpikuultava punainen)
            r2d.drawCircle(200, 60, 40, 0x88FF0000);

            // 5. Ympyrän ääriviiva (vihreä, paksuus 2)
            r2d.drawCircleOutline(200, 60, 40, 0xFF00FF00, 2);

            // 6. Pyöristetty suorakulmio (sininen täytetty)
            r2d.drawRoundedRect(10, 150, 100, 80, 15, 0xFF0000FF);

            // 7. Pyöristetyn suorakulmion ääriviiva (keltainen, paksuus 2)
            r2d.drawRoundedRectOutline(10, 150, 100, 80, 15, 0xFFFFFF00, 2);

            // 8. Pyöristetty suorakulmio custom-kulmilla (vain vasen ylä- ja oikea alakulma)
            r2d.drawRoundedRectCustom(120, 150, 100, 80, 15, 0x88FF00FF, true, false, true, false);
        });
    }
}