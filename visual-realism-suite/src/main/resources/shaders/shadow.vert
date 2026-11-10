#version 330 core
// FILE: src/main/resources/shaders/shadow.vert (Depth Vertex Shader)
// Dipakai pada Pass 1 shadow mapping: hanya mentransformasi posisi
// ke light space, tanpa pencahayaan.
layout (location = 0) in vec3 aPos;

uniform mat4 model;
uniform mat4 lightSpaceMatrix;

void main() {
    gl_Position = lightSpaceMatrix * model * vec4(aPos, 1.0);
}
