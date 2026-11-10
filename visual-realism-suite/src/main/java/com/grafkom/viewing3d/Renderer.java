package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * Renderer - 2-pass shadow mapping (Modul Minggu 11, Indikator 5).
 *
 * Pass 1 (Depth Pass): scene dirender dari sudut pandang sumber
 * cahaya ke ShadowMapFBO, hanya depth yang disimpan.
 * Pass 2 (Lighting Pass): scene dirender dari kamera dengan
 * shader Phong/Blinn-Phong + shadow factor dari shadow map.
 *
 * Keadaan render (toggle keyboard) dikirim lewat RenderState.
 */
public class Renderer {

    /** Semua toggle yang bisa diubah dari keyboard (lihat Main). */
    public static class RenderState {
        public boolean useBlinnPhong = true;
        public boolean useFlatShading = false;
        public boolean depthTestEnabled = true;
        public boolean cullingEnabled = true;
        public boolean useTexture = true;
        public boolean useShadow = true;
        public boolean nearestFilter = false;
    }

    private final Shader phongShader;
    private final Shader depthShader;
    private final ShadowMapFBO shadowFBO;
    private final Vector3f lightPos = new Vector3f(5.0f, 8.0f, 4.0f);
    private final Vector3f lightColor = new Vector3f(1.0f, 1.0f, 1.0f);

    public Renderer(Shader phongShader, Shader depthShader, ShadowMapFBO shadowFBO) {
        this.phongShader = phongShader;
        this.depthShader = depthShader;
        this.shadowFBO = shadowFBO;
    }

    public Vector3f getLightPos() {
        return new Vector3f(lightPos);
    }

    /** Matriks light-space: proyeksi ortografik cahaya x view dari cahaya. */
    public Matrix4f getLightSpaceMatrix() {
        Matrix4f lightProjection = new Matrix4f().ortho(-10.0f, 10.0f, -10.0f, 10.0f, 1.0f, 25.0f);
        Matrix4f lightView = new Matrix4f().lookAt(
                lightPos, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        return new Matrix4f(lightProjection).mul(lightView);
    }

    private void drawMeshWithModel(Shader shader, Mesh mesh, Matrix4f model) {
        shader.setMat4("model", model);
        mesh.draw();
    }

    /**
     * Render satu frame penuh.
     * sphere/house/ground adalah mesh SMOOTH, sedangkan
     * sphereFlat/houseFlat adalah versi FLAT-nya (keduanya
     * dibangun di awal di Main, toggle F hanya memilih mesh mana
     * yang digambar).
     */
    public void render(Mesh sphere, Mesh sphereFlat, Mesh house, Mesh houseFlat,
                       Mesh ground, Texture2D texture, Camera camera,
                       int screenWidth, int screenHeight, RenderState state) {
        Matrix4f view = camera.getViewMatrix();
        Matrix4f projection = camera.getProjectionMatrix((float) screenWidth / screenHeight);
        Matrix4f lightSpaceMatrix = getLightSpaceMatrix();

        Mesh sphereMesh = state.useFlatShading ? sphereFlat : sphere;
        Mesh houseMesh = state.useFlatShading ? houseFlat : house;

        // Matriks model tiap objek
        Matrix4f sphereModel = new Matrix4f().translate(-2.2f, 1.2f, 0.0f);
        Matrix4f houseModel = new Matrix4f().translate(2.0f, 1.0f, -0.5f);
        Matrix4f groundModel = new Matrix4f().translate(0.0f, 0.0f, 0.0f);

        // ---------------- Pass 1: Depth Pass (dari cahaya) ----------------
        if (state.useShadow) {
            shadowFBO.bindForWriting();
            glClear(GL_DEPTH_BUFFER_BIT);
            depthShader.use();
            depthShader.setMat4("lightSpaceMatrix", lightSpaceMatrix);
            drawMeshWithModel(depthShader, sphereMesh, sphereModel);
            drawMeshWithModel(depthShader, houseMesh, houseModel);
            drawMeshWithModel(depthShader, ground, groundModel);
            shadowFBO.unbind(screenWidth, screenHeight);
        }

        // ---------------- Pass 2: Lighting Pass (dari kamera) --------------
        glViewport(0, 0, screenWidth, screenHeight);
        glClearColor(0.10f, 0.12f, 0.18f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        HSRManager.setupDepthTest(state.depthTestEnabled);
        HSRManager.setupBackFaceCulling(state.cullingEnabled);

        phongShader.use();
        phongShader.setMat4("view", view);
        phongShader.setMat4("projection", projection);
        phongShader.setMat4("lightSpaceMatrix", lightSpaceMatrix);
        phongShader.setVec3("lightPos", lightPos);
        phongShader.setVec3("viewPos", camera.getPosition());
        phongShader.setVec3("lightColor", lightColor);
        phongShader.setBool("useBlinnPhong", state.useBlinnPhong);
        phongShader.setBool("useTexture", state.useTexture);
        phongShader.setBool("useShadow", state.useShadow);
        phongShader.setInt("textureSampler", 0);
        phongShader.setInt("shadowMap", 1);

        if (state.useTexture && texture != null) {
            texture.bind(0);
        }
        if (state.useShadow) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, shadowFBO.getDepthTextureID());
        }

        // Sphere - warna dasar oranye
        phongShader.setVec3("objectColor", 0.95f, 0.55f, 0.20f);
        drawMeshWithModel(phongShader, sphereMesh, sphereModel);
        // Rumah - warna dasar krem
        phongShader.setVec3("objectColor", 0.90f, 0.85f, 0.70f);
        drawMeshWithModel(phongShader, houseMesh, houseModel);
        // Lantai - warna dasar abu-abu kehijauan
        phongShader.setVec3("objectColor", 0.45f, 0.55f, 0.45f);
        drawMeshWithModel(phongShader, ground, groundModel);
    }
}
