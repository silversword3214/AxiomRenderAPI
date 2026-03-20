package com.silversword3214.axiomrenderapi.core;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
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
    private final Map<ShaderProgram, Batch> batches = new HashMap<>();

    private final ByteBufferBuilder allocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private MappableRingBuffer vertexBuffer;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private ShaderProgram colorShader;
    private ShaderProgram lineShader;
    private ShaderProgram roundedRectShader;

    private Matrix4f currentViewMatrix;

    public RenderCore() {
        loadShaders();
    }

    private void loadShaders() {
        try {
            colorShader = new ShaderProgram(
                    Identifier.fromNamespaceAndPath("axiomrenderapi", "shaders/color.vert"),
                    Identifier.fromNamespaceAndPath("axiomrenderapi", "shaders/color.frag"),
                    VertexFormat.Mode.QUADS
            );
            lineShader = new ShaderProgram(
                    Identifier.fromNamespaceAndPath("axiomrenderapi", "shaders/line.vert"),
                    Identifier.fromNamespaceAndPath("axiomrenderapi", "shaders/line.frag"),
                    VertexFormat.Mode.LINES
            );
            roundedRectShader = new ShaderProgram(
                    Identifier.fromNamespaceAndPath("axiomrenderapi", "shaders/rounded_rect.vert"),
                    Identifier.fromNamespaceAndPath("axiomrenderapi", "shaders/rounded_rect.frag"),
                    VertexFormat.Mode.QUADS
            );
            LOGGER.info("Shaders loaded successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to load shaders", e);
            colorShader = null;
            lineShader = null;
            roundedRectShader = null;
        }
    }

    public void beginFrame() {
        batches.clear();
    }

    public void setViewMatrix(Matrix4f viewMatrix) {
        this.currentViewMatrix = viewMatrix;
    }

    public void flush() {
        LOGGER.info("flush: batches.size() = {}", batches.size());
        for (Map.Entry<ShaderProgram, Batch> entry : batches.entrySet()) {
            ShaderProgram shader = entry.getKey();
            if (shader == null) continue;
            Batch batch = entry.getValue();
            LOGGER.info("Flushing batch for shader {} with {} vertices", shader, batch.vertexCount());
            drawBatch(shader, batch);
        }
        batches.clear();
    }

    private void drawBatch(ShaderProgram shader, Batch batch) {
        RenderPipeline pipeline = shader.getPipeline();
        if (pipeline == null) {
            LOGGER.error("Pipeline is null for shader {}", shader);
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

        // Allocate vertex buffer...
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
            RenderSystem.AutoStorageIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = indexBuffer.getBuffer(drawParams.indexCount());
            indexType = indexBuffer.type();
        }

        Matrix4f view = (currentViewMatrix != null) ? currentViewMatrix : new Matrix4f();
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(view, COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

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
            builder.addVertex(v[0], v[1], v[2])
                    .setColor(v[3], v[4], v[5], v[6]);
        }
        return builder.buildOrThrow();
    }

    // Primitive methods

    public void addColoredQuad(Matrix4f model, Matrix4f projection, float x1, float y1, float x2, float y2, int color) {
        if (colorShader == null) return;
        Batch batch = batches.computeIfAbsent(colorShader, k -> new Batch(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS));
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        Matrix4f mvp = new Matrix4f(projection).mul(model);
        Vector4f v1 = new Vector4f(x1, y1, 0, 1).mul(mvp);
        Vector4f v2 = new Vector4f(x2, y1, 0, 1).mul(mvp);
        Vector4f v3 = new Vector4f(x2, y2, 0, 1).mul(mvp);
        Vector4f v4 = new Vector4f(x1, y2, 0, 1).mul(mvp);

        if (v1.w != 0) v1.div(v1.w);
        if (v2.w != 0) v2.div(v2.w);
        if (v3.w != 0) v3.div(v3.w);
        if (v4.w != 0) v4.div(v4.w);

        batch.vertex(v1.x, v1.y, v1.z, r, g, b, a);
        batch.vertex(v2.x, v2.y, v2.z, r, g, b, a);
        batch.vertex(v3.x, v3.y, v3.z, r, g, b, a);
        batch.vertex(v4.x, v4.y, v4.z, r, g, b, a);
    }

    public void addRoundedRect(Matrix4f model, Matrix4f projection, float x1, float y1, float x2, float y2, float radius, int color) {
        addColoredQuad(model, projection, x1, y1, x2, y2, color);
    }

    public void addLine2D(Matrix4f model, Matrix4f projection, float x1, float y1, float x2, float y2, float thickness, int color) {
        if (lineShader == null) return;
        Batch batch = batches.computeIfAbsent(lineShader, k -> new Batch(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES));
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        Matrix4f mvp = new Matrix4f(projection).mul(model);
        Vector4f v1 = new Vector4f(x1, y1, 0, 1).mul(mvp);
        Vector4f v2 = new Vector4f(x2, y2, 0, 1).mul(mvp);

        if (v1.w != 0) v1.div(v1.w);
        if (v2.w != 0) v2.div(v2.w);

        batch.vertex(v1.x, v1.y, v1.z, r, g, b, a);
        batch.vertex(v2.x, v2.y, v2.z, r, g, b, a);
    }

    public void addLine3D(double x1, double y1, double z1, double x2, double y2, double z2, float thickness, int color) {
        if (lineShader == null) return;

        Batch batch = batches.computeIfAbsent(lineShader, k -> new Batch(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES));
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        batch.vertex((float)x1, (float)y1, (float)z1, r, g, b, a);
        batch.vertex((float)x2, (float)y2, (float)z2, r, g, b, a);
    }

    // Test method: draw a red line in camera space (should be projected by the uniform)
    public void testCameraLine() {
        if (lineShader == null) return;
        Batch batch = new Batch(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES);
        // Draw a line from (0,0,-5) to (0,0,5) in camera space. This should be a line through the screen.
        batch.vertex(0, 0, -5, 1, 0, 0, 1);
        batch.vertex(0, 0,  5, 1, 0, 0, 1);
        batches.put(lineShader, batch);
        flush();
    }

    // Test method: fullscreen red quad (NDC space) – should always appear
    public void testRedQuad() {
        if (colorShader == null) return;
        Batch batch = new Batch(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS);
        batch.vertex(-1, -1, 0, 1, 0, 0, 1);
        batch.vertex( 1, -1, 0, 1, 0, 0, 1);
        batch.vertex( 1,  1, 0, 1, 0, 0, 1);
        batch.vertex(-1,  1, 0, 1, 0, 0, 1);
        batches.put(colorShader, batch);
        flush();
    }

    public void close() {
        if (allocator != null) allocator.close();
        if (vertexBuffer != null) vertexBuffer.close();
    }
}