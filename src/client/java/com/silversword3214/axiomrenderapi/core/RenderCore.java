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

    // Use the custom pipelines from the provider
    private RenderPipeline linePipeline = RenderPipelines.WORLD_COLORED_LINES;
    private RenderPipeline quadPipeline = RenderPipelines.WORLD_COLORED;

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
            // For POS3_COLOR format: v[0..2] = position, v[3..6] = color
            builder.addVertex(v[0], v[1], v[2])
                    .setColor(v[3], v[4], v[5], v[6]);
        }
        return builder.buildOrThrow();
    }

    // --- Drawing methods using custom pipeline and format ---

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

    public void close() {
        if (allocator != null) allocator.close();
        if (vertexBuffer != null) vertexBuffer.close();
    }
}