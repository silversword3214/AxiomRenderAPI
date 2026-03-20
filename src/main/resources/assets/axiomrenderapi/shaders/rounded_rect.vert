#version 330 core

in vec3 Position;
in vec4 Color;

uniform DynamicTransforms {
    mat4 modelViewProjection;
};

out vec4 vertexColor;

void main() {
    gl_Position = modelViewProjection * vec4(Position, 1.0);
    vertexColor = Color;
}