package com.silversword3214.axiomrenderapi.api;

import com.silversword3214.axiomrenderapi.core.RenderCore;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public class Renderer3D {
    private final WorldRenderContext context;
    private final RenderCore core;
    private final Matrix4f projection;

    public Renderer3D(WorldRenderContext context, RenderCore core) {
        this.context = context;
        this.core = core;

        // Compute projection (optional, kept for reference)
        Minecraft minecraft = Minecraft.getInstance();
        Options options = minecraft.options;
        float fov = options.fov().get().floatValue();
        int width = minecraft.getWindow().getWidth();
        int height = minecraft.getWindow().getHeight();
        float aspect = (float) width / (float) height;
        this.projection = new Matrix4f().perspective((float) Math.toRadians(fov), aspect, 0.05f, 1000.0f);

        // Compute the view matrix from the camera
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 pos = camera.position();
        Quaternionf rot = camera.rotation();
        Matrix4f view = new Matrix4f().rotate(rot.conjugate()).translate(-(float)pos.x, -(float)pos.y, -(float)pos.z);
        core.setViewMatrix(view); // tell RenderCore to use this view matrix
    }

    private Matrix4f getModelViewMatrix() {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 pos = camera.position();
        Quaternionf rot = camera.rotation();
        Matrix4f view = new Matrix4f().rotate(rot.conjugate()).translate(-(float)pos.x, -(float)pos.y, -(float)pos.z);
        System.out.println("ModelView matrix: " + view);
        return view;
    }

    public void box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color) {
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
            core.addLine3D(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, 2f, color);
        }
    }

    public void line(Vector3d start, Vector3d end, float thickness, int color) {
        core.addLine3D(start.x, start.y, start.z, end.x, end.y, end.z, thickness, color);
    }
}