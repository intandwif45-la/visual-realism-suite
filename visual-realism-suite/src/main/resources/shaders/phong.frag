#version 330 core
// FILE: src/main/resources/shaders/phong.frag (Fragment Shader)
// Persis Modul Minggu 11 (Blinn-Phong per-pixel), DIPERLUAS dengan
// shadow mapping: uniform shadowMap + lightSpaceMatrix, shadow factor
// dengan bias = max(0.05 * (1.0 - dot(N, L)), 0.005),
// shadowFactor 0.2 jika dalam bayangan, 1.0 jika terkena cahaya.
out vec4 FragColor;

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoords;
in vec4 FragPosLightSpace;

uniform vec3 lightPos;
uniform vec3 viewPos;
uniform vec3 lightColor;
uniform vec3 objectColor;
uniform sampler2D textureSampler;
uniform sampler2D shadowMap;
uniform bool useTexture;
uniform bool useBlinnPhong;
uniform bool useShadow;

float calculateShadow(vec3 norm, vec3 lightDir) {
    // Transformasi ke koordinat shadow map [0,1]
    vec3 projCoords = FragPosLightSpace.xyz / FragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;
    // Di luar shadow map = tidak berbayang
    if (projCoords.z > 1.0) {
        return 1.0;
    }
    float d_current = projCoords.z;
    float d_shadow = texture(shadowMap, projCoords.xy).r;
    // Shadow Bias dinamis (Modul: bias = max(0.05*(1.0 - N.L), 0.005))
    float bias = max(0.05 * (1.0 - dot(norm, lightDir)), 0.005);
    if (d_current > d_shadow + bias) {
        return 0.2; // dalam bayangan
    }
    return 1.0; // terkena cahaya
}

void main() {
    // 1. Ambient Lighting
    float ambientStrength = 0.2;
    vec3 ambient = ambientStrength * lightColor;

    // 2. Diffuse Lighting (Lambertian)
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    // 3. Specular Lighting (Phong vs Blinn-Phong)
    float specularStrength = 0.8;
    vec3 viewDir = normalize(viewPos - FragPos);
    float spec = 0.0;
    if (useBlinnPhong) {
        vec3 halfwayDir = normalize(lightDir + viewDir);
        spec = pow(max(dot(norm, halfwayDir), 0.0), 32.0); // Blinn-Phong
    } else {
        vec3 reflectDir = reflect(-lightDir, norm);
        spec = pow(max(dot(viewDir, reflectDir), 0.0), 16.0); // Standard Phong
    }
    vec3 specular = specularStrength * spec * lightColor;

    // Shadow factor
    float shadowFactor = 1.0;
    if (useShadow) {
        shadowFactor = calculateShadow(norm, lightDir);
    }

    // Combine Texture & Lighting
    vec3 baseColor = useTexture ? texture(textureSampler, TexCoords).rgb : objectColor;
    vec3 result = (ambient + (diffuse + specular) * shadowFactor) * baseColor;
    FragColor = vec4(result, 1.0);
}
