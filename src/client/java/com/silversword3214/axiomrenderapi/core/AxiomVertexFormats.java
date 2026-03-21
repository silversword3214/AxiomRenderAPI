package com.silversword3214.axiomrenderapi.core;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public abstract class AxiomVertexFormats {
    // 2D position (x,y,0) – we'll use standard POSITION with count 3
    public static final VertexFormat POS2_COLOR = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION) // count 3
            .add("Color", VertexFormatElement.COLOR)
            .build();

    // 3D position (x,y,z)
    public static final VertexFormat POS3_COLOR = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION) // count 3
            .add("Color", VertexFormatElement.COLOR)
            .build();

    // For textured UI (2D position + UV + color)
    public static final VertexFormat POS2_UV_COLOR = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION) // count 3
            .add("UV", VertexFormatElement.UV)
            .add("Color", VertexFormatElement.COLOR)
            .build();

    private AxiomVertexFormats() {}
}