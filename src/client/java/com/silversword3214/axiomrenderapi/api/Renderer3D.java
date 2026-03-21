package com.silversword3214.axiomrenderapi.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.silversword3214.axiomrenderapi.core.RenderCore;
import com.silversword3214.axiomrenderapi.utils.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public class Renderer3D {
    private final RenderCore core;
    private final float tickDelta;

    public Renderer3D(WorldRenderContext context, RenderCore core) {
        this.core = core;
        this.tickDelta = RenderUtils.getTickDelta();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Matrix4f projection = RenderUtils.getProjectionMatrix(tickDelta);
        Matrix4f view = RenderUtils.getViewMatrix(camera);

        core.beginFrame(projection, view);
    }


    public float getTickDelta() {
        return tickDelta;
    }

    public void box(float minX, float minY, float minZ,
                    float maxX, float maxY, float maxZ,
                    int color) {
        Vector3d[] corners = {
                new Vector3d(minX, minY, minZ), new Vector3d(maxX, minY, minZ),
                new Vector3d(minX, minY, maxZ), new Vector3d(maxX, minY, maxZ),
                new Vector3d(minX, maxY, minZ), new Vector3d(maxX, maxY, minZ),
                new Vector3d(minX, maxY, maxZ), new Vector3d(maxX, maxY, maxZ)
        };

        int[][] edges = {
                {0,1}, {2,3}, {4,5}, {6,7},
                {0,2}, {1,3}, {4,6}, {5,7},
                {0,4}, {1,5}, {2,6}, {3,7}
        };

        for (int[] edge : edges) {
            Vector3d p1 = corners[edge[0]];
            Vector3d p2 = corners[edge[1]];
            core.addLine3D(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, 2.0f, color);
        }
    }

    public void line(Vector3d start, Vector3d end, float thickness, int color) {
        core.addLine3D(start.x, start.y, start.z, end.x, end.y, end.z, thickness, color);
    }

    public void flush() {
        core.flush();
    }
}