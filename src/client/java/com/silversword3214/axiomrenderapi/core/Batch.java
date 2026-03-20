package com.silversword3214.axiomrenderapi.core;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;

public class Batch {
    private final VertexFormat format;
    private final VertexFormat.Mode mode;
    private final List<float[]> vertices = new ArrayList<>();
    private int vertexCount = 0;

    public Batch(VertexFormat format, VertexFormat.Mode mode) {
        this.format = format;
        this.mode = mode;
    }

    // For POSITION_COLOR
    public void vertex(float x, float y, float z, float r, float g, float b, float a) {
        vertices.add(new float[]{x, y, z, r, g, b, a});
        vertexCount++;
    }

    // For POSITION_COLOR_NORMAL_LINE_WIDTH
    public void vertexWithNormalAndWidth(float x, float y, float z, float r, float g, float b, float a,
                                         float nx, float ny, float nz, float lineWidth) {
        vertices.add(new float[]{x, y, z, r, g, b, a, nx, ny, nz, lineWidth});
        vertexCount++;
    }

    public int vertexCount() { return vertexCount; }
    public VertexFormat getFormat() { return format; }
    public VertexFormat.Mode getMode() { return mode; }
    public List<float[]> getVertices() { return vertices; }
    public void clear() { vertices.clear(); vertexCount = 0; }
}