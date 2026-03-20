#version 150

layout(lines) in;
layout(triangle_strip, max_vertices = 4) out;

in vec4 vertexColor[];
out vec4 fragColor;

uniform float u_Thickness;

void main() {
    vec3 p1 = gl_in[0].gl_Position.xyz;
    vec3 p2 = gl_in[1].gl_Position.xyz;
    vec3 dir = normalize(p2 - p1);
    vec3 up = vec3(0.0, 0.0, 1.0);
    vec3 right = cross(dir, up);
    right = normalize(right) * u_Thickness * 0.5;

    vec4 v1 = vec4(p1 - right, 1.0);
    vec4 v2 = vec4(p1 + right, 1.0);
    vec4 v3 = vec4(p2 - right, 1.0);
    vec4 v4 = vec4(p2 + right, 1.0);

    gl_Position = v1; fragColor = vertexColor[0]; EmitVertex();
    gl_Position = v2; fragColor = vertexColor[0]; EmitVertex();
    gl_Position = v3; fragColor = vertexColor[1]; EmitVertex();
    gl_Position = v4; fragColor = vertexColor[1]; EmitVertex();
    EndPrimitive();
}