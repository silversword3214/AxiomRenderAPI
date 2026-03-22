package com.silversword3214.axiomrenderapi.core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class RenderCore {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderCore.class);
    private final Map<RenderPipeline, Batch> batches = new HashMap<>();

    private final ByteBufferBuilder allocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private MappableRingBuffer vertexBuffer;

    // 3D
    private RenderPipeline linePipeline = RenderPipelines.WORLD_COLORED_LINES;
    private RenderPipeline quadPipeline = RenderPipelines.WORLD_COLORED;

    // 2D
    private RenderPipeline uiColoredPipeline = RenderPipelines.UI_COLORED;
    private RenderPipeline uiColoredLinesPipeline = RenderPipelines.UI_COLORED_LINES;

    private Matrix4f currentProjectionMatrix;
    private Matrix4f currentModelViewMatrix;

    public RenderCore() {
        // Ensure pipelines are built
        RenderPipelines.rebuildAll();
    }

    public void beginFrame(Matrix4f projection, Matrix4f modelView) {
        batches.clear();
        this.currentProjectionMatrix = projection;
        this.currentModelViewMatrix = modelView;

    }



    public void flush() {
        for (Map.Entry<RenderPipeline, Batch> entry : batches.entrySet()) {
            RenderPipeline pipeline = entry.getKey();
            Batch batch = entry.getValue();
            drawBatch(pipeline, batch);
        }
        batches.clear();
    }

    // The core of the rendering
    private void drawBatch(RenderPipeline pipeline, Batch batch) {
        if (pipeline == null) {
            LOGGER.error("Pipeline is null");
            return;
        }

        MeshData mesh = buildMeshFromBatch(batch);
        if (mesh == null) {
            LOGGER.warn("Mesh is null for batch (vertexCount={})", batch.vertexCount());
            return;
        }

        MeshData.DrawState drawParams = mesh.drawState();
        VertexFormat format = drawParams.format();
        int vertexBufferSize = drawParams.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) vertexBuffer.close();
            vertexBuffer = new MappableRingBuffer(
                    () -> "axiomrenderapi_vertex_buffer",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                    vertexBufferSize
            );
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView mapped = encoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, mesh.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(mesh.vertexBuffer(), mapped.data());
        } catch (Exception e) {
            LOGGER.error("Failed to upload vertex data", e);
            mesh.close();
            return;
        }
        GpuBuffer vertices = vertexBuffer.currentBuffer();

        GpuBuffer indices;
        VertexFormat.IndexType indexType;
        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            mesh.sortQuads(allocator, RenderSystem.getProjectionType().vertexSorting());
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(mesh.indexBuffer());
            indexType = mesh.drawState().indexType();
        } else {
            // For lines, use sequential index buffer
            RenderSystem.AutoStorageIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = indexBuffer.getBuffer(drawParams.indexCount());
            indexType = indexBuffer.type();
        }

        // Compute combined MVP matrix for the standard DynamicTransforms uniform
        Matrix4f mvp = new Matrix4f(currentProjectionMatrix).mul(currentModelViewMatrix);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                mvp,
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), // ColorModulator
                new Vector3f(0.0f, 0.0f, 0.0f),         // ModelOffset
                new Matrix4f()                          // TextureMatrix (identity)
        );

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> "axiomrenderapi_draw",
                        Minecraft.getInstance().getMainRenderTarget().getColorTextureView(),
                        OptionalInt.empty(),
                        Minecraft.getInstance().getMainRenderTarget().getDepthTextureView(),
                        OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            // Use the standard uniform name expected by Minecraft shaders
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0, 0, drawParams.indexCount(), 1);
        } catch (Exception e) {
            LOGGER.error("Error during render pass", e);
        }

        mesh.close();
        vertexBuffer.rotate();
    }

    private MeshData buildMeshFromBatch(Batch batch) {
        if (batch.vertexCount() == 0) return null;
        BufferBuilder builder = new BufferBuilder(allocator, batch.getMode(), batch.getFormat());
        for (float[] v : batch.getVertices()) {
            // v[0]=x, v[1]=y, v[2]=z, v[3]=r, v[4]=g, v[5]=b, v[6]=a
            builder.addVertex(v[0], v[1], v[2])
                    .setColor(v[3], v[4], v[5], v[6]);
        }
        return builder.buildOrThrow();
    }

    //  - - -  3D drawing methods  - - -

    public void addLine3D(double x1, double y1, double z1, double x2, double y2, double z2, float thickness, int color) {
        if (linePipeline == null) return;

        Batch batch = batches.computeIfAbsent(linePipeline, k -> new Batch(AxiomVertexFormats.POS3_COLOR, VertexFormat.Mode.DEBUG_LINES));
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        // Position + color
        batch.vertex((float)x1, (float)y1, (float)z1, r, g, b, a);
        batch.vertex((float)x2, (float)y2, (float)z2, r, g, b, a);
    }


    public void addQuad(double x1, double y1, double z1,
                        double x2, double y2, double z2,
                        double x3, double y3, double z3,
                        double x4, double y4, double z4,
                        int color) {
        if (quadPipeline == null) return;

        Batch batch = batches.computeIfAbsent(quadPipeline, k -> new Batch(AxiomVertexFormats.POS3_COLOR, VertexFormat.Mode.TRIANGLES));
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        // Split quad into two triangles
        batch.vertex((float)x1, (float)y1, (float)z1, r, g, b, a);
        batch.vertex((float)x2, (float)y2, (float)z2, r, g, b, a);
        batch.vertex((float)x3, (float)y3, (float)z3, r, g, b, a);
        batch.vertex((float)x1, (float)y1, (float)z1, r, g, b, a);
        batch.vertex((float)x3, (float)y3, (float)z3, r, g, b, a);
        batch.vertex((float)x4, (float)y4, (float)z4, r, g, b, a);
    }

    // - - -  2D drawing methods  - - -

    // Rect drawing
    public void addRect2D(float x, float y, float width, float height, int color) {
        if (uiColoredPipeline == null) return;
        Batch batch = batches.computeIfAbsent(uiColoredPipeline,
                k -> new Batch(AxiomVertexFormats.POS2_COLOR, VertexFormat.Mode.TRIANGLES));

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        // two triangles to make rect
        float x2 = x + width;
        float y2 = y + height;
        // tri 1
        batch.vertex2D(x, y, r, g, b, a);
        batch.vertex2D(x2, y, r, g, b, a);
        batch.vertex2D(x, y2, r, g, b, a);
        // tri 2
        batch.vertex2D(x, y2, r, g, b, a);
        batch.vertex2D(x2, y, r, g, b, a);
        batch.vertex2D(x2, y2, r, g, b, a);
    }

    // Rect outline
    public void addRectOutline2D(float x, float y, float width, float height, float thickness, int color) {
        if (uiColoredLinesPipeline == null) return;
        Batch batch = batches.computeIfAbsent(uiColoredLinesPipeline,
                k -> new Batch(AxiomVertexFormats.POS2_COLOR, VertexFormat.Mode.DEBUG_LINES));

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        float x2 = x + width;
        float y2 = y + height;

        // bottom
        batch.vertex2D(x, y, r, g, b, a);
        batch.vertex2D(x2, y, r, g, b, a);
        // top
        batch.vertex2D(x, y2, r, g, b, a);
        batch.vertex2D(x2, y2, r, g, b, a);
        // left side
        batch.vertex2D(x, y, r, g, b, a);
        batch.vertex2D(x, y2, r, g, b, a);
        // right side
        batch.vertex2D(x2, y, r, g, b, a);
        batch.vertex2D(x2, y2, r, g, b, a);
    }

    // Simple line
    public void addLine2D(float x1, float y1, float x2, float y2, float thickness, int color) {
        if (uiColoredLinesPipeline == null) return;
        Batch batch = batches.computeIfAbsent(uiColoredLinesPipeline,
                k -> new Batch(AxiomVertexFormats.POS2_COLOR, VertexFormat.Mode.DEBUG_LINES));

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        batch.vertex2D(x1, y1, r, g, b, a);
        batch.vertex2D(x2, y2, r, g, b, a);
    }

    // Rounded rect
    public void addRoundedRect(float x, float y, float w, float h, float radius, int color, int segmentsPerCorner) {
        if (uiColoredPipeline == null) return;

        radius = Math.min(radius, Math.min(w, h) / 2);
        if (radius <= 0) {
            // Tavallinen suorakulmio
            addRect2D(x, y, w, h, color);
            return;
        }

        Batch batch = batches.computeIfAbsent(uiColoredPipeline,
                k -> new Batch(AxiomVertexFormats.POS2_COLOR, VertexFormat.Mode.TRIANGLES));

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        float left = x;
        float right = x + w;
        float top = y;
        float bottom = y + h;
        float rad = radius;

        if (rad * 2 < w) {
            addRect2D(left + rad, top, w - rad * 2, h, color);
        }
        if (rad * 2 < h) {
            addRect2D(left, top + rad, w, h - rad * 2, color);
        }

        // (left+rad, top+rad)
        drawQuarterCircle(batch, left + rad, top + rad, rad, 0, r, g, b, a, segmentsPerCorner);
        // (right-rad, top+rad)
        drawQuarterCircle(batch, right - rad, top + rad, rad, 1, r, g, b, a, segmentsPerCorner);
        // (right-rad, bottom-rad)
        drawQuarterCircle(batch, right - rad, bottom - rad, rad, 2, r, g, b, a, segmentsPerCorner);
        // (left+rad, bottom-rad)
        drawQuarterCircle(batch, left + rad, bottom - rad, rad, 3, r, g, b, a, segmentsPerCorner);
    }

    // Quarter circle for rounded rect
    private void drawQuarterCircle(Batch batch, float cx, float cy, float radius, int quadrant,
                                   float r, float g, float b, float a, int segments) {
        float startAngle = quadrant * 90f;
        float endAngle = startAngle + 90f;

        for (int i = 0; i < segments; i++) {
            float angle1 = startAngle + (endAngle - startAngle) * i / segments;
            float angle2 = startAngle + (endAngle - startAngle) * (i + 1) / segments;
            float rad1 = (float) Math.toRadians(angle1);
            float rad2 = (float) Math.toRadians(angle2);
            float x1 = cx + (float) Math.cos(rad1) * radius;
            float y1 = cy + (float) Math.sin(rad1) * radius;
            float x2 = cx + (float) Math.cos(rad2) * radius;
            float y2 = cy + (float) Math.sin(rad2) * radius;

            batch.vertex2D(cx, cy, r, g, b, a);
            batch.vertex2D(x1, y1, r, g, b, a);
            batch.vertex2D(x2, y2, r, g, b, a);
        }
    }

    // Rounded rect outline
    public void addRoundedRectOutline(float x, float y, float w, float h, float radius, float thickness, int color, int segmentsPerCorner) {
        if (uiColoredLinesPipeline == null) return;

        radius = Math.min(radius, Math.min(w, h) / 2);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        float left = x;
        float right = x + w;
        float top = y;
        float bottom = y + h;
        float rad = radius;

        Batch batch = batches.computeIfAbsent(uiColoredLinesPipeline,
                k -> new Batch(AxiomVertexFormats.POS2_COLOR, VertexFormat.Mode.DEBUG_LINES));

        if (rad * 2 < h) {
            batch.vertex2D(left, top + rad, r, g, b, a);
            batch.vertex2D(left, bottom - rad, r, g, b, a);

            batch.vertex2D(right, top + rad, r, g, b, a);
            batch.vertex2D(right, bottom - rad, r, g, b, a);
        }

        if (rad * 2 < w) {
            // top
            batch.vertex2D(left + rad, top, r, g, b, a);
            batch.vertex2D(right - rad, top, r, g, b, a);
            // bottom
            batch.vertex2D(left + rad, bottom, r, g, b, a);
            batch.vertex2D(right - rad, bottom, r, g, b, a);
        }

        drawQuarterArc(batch, left + rad, top + rad, rad, 0, r, g, b, a, segmentsPerCorner);
        drawQuarterArc(batch, right - rad, top + rad, rad, 1, r, g, b, a, segmentsPerCorner);
        drawQuarterArc(batch, right - rad, bottom - rad, rad, 2, r, g, b, a, segmentsPerCorner);
        drawQuarterArc(batch, left + rad, bottom - rad, rad, 3, r, g, b, a, segmentsPerCorner);
    }

    private void drawQuarterArc(Batch batch, float cx, float cy, float radius, int quadrant,
                                float r, float g, float b, float a, int segments) {
        float startAngle = quadrant * 90f;
        float endAngle = startAngle + 90f;

        for (int i = 0; i < segments; i++) {
            float angle1 = startAngle + (endAngle - startAngle) * i / segments;
            float angle2 = startAngle + (endAngle - startAngle) * (i + 1) / segments;
            float rad1 = (float) Math.toRadians(angle1);
            float rad2 = (float) Math.toRadians(angle2);
            float x1 = cx + (float) Math.cos(rad1) * radius;
            float y1 = cy + (float) Math.sin(rad1) * radius;
            float x2 = cx + (float) Math.cos(rad2) * radius;
            float y2 = cy + (float) Math.sin(rad2) * radius;
            batch.vertex2D(x1, y1, r, g, b, a);
            batch.vertex2D(x2, y2, r, g, b, a);
        }
    }

    // Circle drawing
    public void addCircle(float cx, float cy, float radius, int color, int segments) {
        if (uiColoredPipeline == null) return;
        Batch batch = batches.computeIfAbsent(uiColoredPipeline,
                k -> new Batch(AxiomVertexFormats.POS2_COLOR, VertexFormat.Mode.TRIANGLES));

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            float x1 = cx + (float) Math.cos(angle1) * radius;
            float y1 = cy + (float) Math.sin(angle1) * radius;
            float x2 = cx + (float) Math.cos(angle2) * radius;
            float y2 = cy + (float) Math.sin(angle2) * radius;
            batch.vertex2D(cx, cy, r, g, b, a);
            batch.vertex2D(x1, y1, r, g, b, a);
            batch.vertex2D(x2, y2, r, g, b, a);
        }
    }

    // Circle outline
    public void addCircleOutline(float cx, float cy, float radius, float thickness, int color, int segments) {
        if (uiColoredLinesPipeline == null) return;
        Batch batch = batches.computeIfAbsent(uiColoredLinesPipeline,
                k -> new Batch(AxiomVertexFormats.POS2_COLOR, VertexFormat.Mode.DEBUG_LINES));

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            float x1 = cx + (float) Math.cos(angle1) * radius;
            float y1 = cy + (float) Math.sin(angle1) * radius;
            float x2 = cx + (float) Math.cos(angle2) * radius;
            float y2 = cy + (float) Math.sin(angle2) * radius;
            batch.vertex2D(x1, y1, r, g, b, a);
            batch.vertex2D(x2, y2, r, g, b, a);
        }
    }

    public void close() {
        if (allocator != null) allocator.close();
        if (vertexBuffer != null) vertexBuffer.close();
    }
}