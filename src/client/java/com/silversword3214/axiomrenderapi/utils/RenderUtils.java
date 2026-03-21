package com.silversword3214.axiomrenderapi.utils;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class RenderUtils {

    /**
     * Gets the current game's projection matrix for world rendering.
     *
     * @param tickDelta The partial tick time (from DeltaTracker)
     * @return The projection matrix
     */
    public static Matrix4f getProjectionMatrix(float tickDelta) {
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        Camera camera = gameRenderer.getMainCamera();
        float fov = gameRenderer.getFov(camera, tickDelta, true);
        return gameRenderer.getProjectionMatrix(fov);
    }

    /**
     * Gets the view matrix from the current camera (with interpolation).
     *
     * @param camera The camera (already interpolated)
     * @return The view matrix
     */
    public static Matrix4f getViewMatrix(Camera camera) {
        Vec3 pos = camera.position();
        Quaternionf rot = camera.rotation();
        return new Matrix4f()
                .rotate(rot.conjugate())
                .translate(-(float) pos.x, -(float) pos.y, -(float) pos.z);
    }

    /**
     * Gets the partial tick delta for rendering interpolation.
     *
     * @return The tick delta
     */
    public static float getTickDelta() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
}