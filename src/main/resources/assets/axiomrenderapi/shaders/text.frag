#version 330 core

uniform sampler2D u_Texture;

in vec2 v_UV;
in vec4 v_Color;
out vec4 FragColor;

// Optional: parameters for SDF effects (outline, glow, shadow)
uniform float u_OutlineWidth = 0.0;
uniform vec4 u_OutlineColor = vec4(0.0, 0.0, 0.0, 1.0);
uniform float u_GlowRadius = 0.0;
uniform vec4 u_GlowColor = vec4(1.0, 1.0, 1.0, 0.5);
uniform float u_ShadowSoftness = 0.0;
uniform vec4 u_ShadowColor = vec4(0.0, 0.0, 0.0, 0.5);
uniform float u_Threshold = 0.5;

void main() {
    float alpha = texture(u_Texture, v_UV).r; // SDF stores distance in red channel
    float finalAlpha = 0.0;
    vec4 finalColor = v_Color;

    // Simple threshold – can be expanded for outline/glow
    if (alpha > u_Threshold) {
        finalAlpha = 1.0;
    } else {
        finalAlpha = 0.0;
    }

    FragColor = vec4(finalColor.rgb, finalColor.a * finalAlpha);
}