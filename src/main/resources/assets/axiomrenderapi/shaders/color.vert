#version 150

in vec3 Position;
in vec4 Color;

out vec4 vertexColor;

uniform mat4 ModelViewProj;

void main() {
    gl_Position = ModelViewProj * vec4(Position, 1.0);
    vertexColor = Color;
}