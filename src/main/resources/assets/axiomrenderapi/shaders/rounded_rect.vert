#version 150

in vec3 Position;
in vec4 Color;

out vec4 vertexColor;
out vec2 fragCoord;

uniform mat4 ModelViewProj;
uniform vec4 u_Rect;

void main() {
    gl_Position = ModelViewProj * vec4(Position, 1.0);
    vertexColor = Color;
    fragCoord = gl_Position.xy;
}