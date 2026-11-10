#version 330 core
// FILE: src/main/resources/shaders/shadow.frag (Depth Fragment Shader)
// Depth-only: tidak menulis warna, depth ditulis otomatis oleh GPU
// ke Shadow Map (Depth FBO).
void main() {
    // Kosong dengan sengaja: gl_FragDepth diisi otomatis.
}
