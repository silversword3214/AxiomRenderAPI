#version 150

in vec4 vertexColor;
in vec2 fragCoord;
out vec4 fragColor;

uniform vec4 u_Rect; // x1, y1, x2, y2 in screen space
uniform float u_Radius;
uniform vec2 u_ScreenSize;

float roundedBoxSDF(vec2 p, vec2 b, float r) {
    vec2 d = abs(p) - b + vec2(r);
    return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
}

void main() {
    vec2 p = fragCoord;
    vec2 center = (u_Rect.xy + u_Rect.zw) * 0.5;
    vec2 halfSize = (u_Rect.zw - u_Rect.xy) * 0.5;
    float dist = roundedBoxSDF(p - center, halfSize, u_Radius);
    if (dist > 0.0) discard;
    fragColor = vertexColor;
}