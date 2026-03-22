package com.silversword3214.axiomrenderapi.core;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;

public class Batch {
    private final VertexFormat format;
    private final VertexFormat.Mode mode;
    private final List<float[]> vertices = new ArrayList<>();

    public Batch(VertexFormat format, VertexFormat.Mode mode) {
        this.format = format;
        this.mode = mode;
    }

    public VertexFormat getFormat() { return format; }
    public VertexFormat.Mode getMode() { return mode; }
    public int vertexCount() { return vertices.size(); }
    public List<float[]> getVertices() { return vertices; }

    // For POS3_COLOR: x,y,z,r,g,b,a
    public void vertex(float x, float y, float z, float r, float g, float b, float a) {
        vertices.add(new float[]{x, y, z, r, g, b, a});
    }

    public void vertex2D(float x, float y, float r, float g, float b, float a) {
        vertices.add(new float[]{x, y, 0f, r, g, b, a});
    }

}