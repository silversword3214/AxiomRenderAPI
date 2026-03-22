#version 330 core

uniform sampler2D u_Texture;
in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    fragColor = texture(u_Texture, texCoord) * vertexColor;
}