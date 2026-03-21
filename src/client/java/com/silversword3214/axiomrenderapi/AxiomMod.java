package com.silversword3214.axiomrenderapi;

import com.silversword3214.axiomrenderapi.integration.FabricHudHook;
import com.silversword3214.axiomrenderapi.integration.FabricWorldHook;
import com.silversword3214.axiomrenderapi.test.ESP;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AxiomMod implements ClientModInitializer {
    public static final String MOD_ID = "axiomrenderapi";
    private static KeyMapping keyBinding;
    public static Minecraft mc;

    @Override
    public void onInitializeClient() {
        mc = Minecraft.getInstance();
        FabricHudHook.register();
        FabricWorldHook.register();


        // Register a keybind to toggle ESP (press 'R' for testing)
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.axiomrenderapi.toggle_esp",
                GLFW.GLFW_KEY_R,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.consumeClick()) {
                ESP esp = ESP.getInstance();
                esp.setEnabled(!esp.isEnabled());
                System.out.println("ESP toggled to: " + esp.isEnabled());
            }
        });
    }
}