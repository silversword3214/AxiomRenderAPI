package com.silversword3214.axiomrenderapi;

import com.silversword3214.axiomrenderapi.api.Renderer2D;
import com.silversword3214.axiomrenderapi.event.Render3DEvent;
import com.silversword3214.axiomrenderapi.event.RenderEventDispatcher;
import com.silversword3214.axiomrenderapi.integration.FabricHudHook;
import com.silversword3214.axiomrenderapi.integration.FabricWorldHook;
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
    }
}