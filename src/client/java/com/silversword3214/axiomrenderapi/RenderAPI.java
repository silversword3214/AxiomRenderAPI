package com.silversword3214.axiomrenderapi;

import com.silversword3214.axiomrenderapi.api.Renderer2D;
import com.silversword3214.axiomrenderapi.api.Renderer3D;
import com.silversword3214.axiomrenderapi.core.RenderCore;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class RenderAPI {
    private static final RenderAPI INSTANCE = new RenderAPI();

    private final RenderCore core;
    private Renderer2D renderer2D;
    private Renderer3D renderer3D;

    private RenderAPI() {
        core = new RenderCore();
    }

    public static RenderAPI getInstance() {
        return INSTANCE;
    }

    public void beginHUD(GuiGraphics graphics, float tickDelta) {
        core.beginFrame();
        core.setViewMatrix(new Matrix4f());
        renderer2D = new Renderer2D(graphics, core);
    }

    public void beginWorld(WorldRenderContext context) {
        core.beginFrame();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 pos = camera.position();
        Quaternionf rot = camera.rotation();
        Matrix4f view = new Matrix4f().rotate(rot.conjugate()).translate(-(float)pos.x, -(float)pos.y, -(float)pos.z);
        core.setViewMatrix(view);

        renderer3D = new Renderer3D(context, core);
    }

    public void end() {
        core.flush();
    }

    public Renderer2D hud() {
        if (renderer2D == null) {
            throw new IllegalStateException("beginHUD() not called");
        }
        return renderer2D;
    }

    public Renderer3D world() {
        if (renderer3D == null) {
            throw new IllegalStateException("beginWorld() not called");
        }
        return renderer3D;
    }

    public void testRedQuad() {
        core.testRedQuad();
    }

    public void testCameraLine() {
        core.testCameraLine();
    }

    // Expose close for cleanup
    public void close() {
        core.close();
    }
}