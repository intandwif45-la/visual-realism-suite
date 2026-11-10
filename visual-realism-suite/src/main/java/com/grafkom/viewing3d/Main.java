package com.grafkom.viewing3d;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.io.File;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Main - 3D Visual Realism Suite (Grafika Komputer Minggu 11).
 *
 * PENTING: Program ini TIDAK bisa di-build/run di lingkungan
 * tanpa Java/Android Studio. Build dan run HARUS di laptop
 * dengan JDK 17 + Gradle (lihat README.md).
 *
 * Kontrol keyboard (persis Modul Minggu 11):
 *   B = toggle Phong / Blinn-Phong
 *   F = toggle Flat / Smooth Shading
 *   Z = toggle Z-Buffer Depth Test
 *   C = toggle Back-Face Culling
 *   T = toggle Texture on/off
 *   M = toggle filter Nearest / Bilinear-Mipmap
 *   S = toggle Shadow on/off
 *   Mouse drag = orbit kamera, Scroll = zoom, ESC = keluar
 */
public class Main {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private long window;
    private final Camera camera = new Camera();
    private final Renderer.RenderState state = new Renderer.RenderState();
    private Texture2D texture;

    private boolean leftMouseDown = false;
    private double lastMouseX;
    private double lastMouseY;

    // Pengukur FPS: dihitung tiap 1 detik, dicetak ke console dan judul window.
    private long fpsFrames = 0;
    private double fpsLastTime = 0;
    private int lastFps = 0;

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        initWindow();
        GL.createCapabilities();

        // --- Shader (GLSL) ---
        Shader phongShader = new Shader(
                "src/main/resources/shaders/phong.vert",
                "src/main/resources/shaders/phong.frag");
        Shader depthShader = new Shader(
                "src/main/resources/shaders/depth.vert",
                "src/main/resources/shaders/depth.frag");

        // --- Mesh: smooth + flat (keduanya dibangun di awal) ---
        Mesh sphere = MeshFactory.createSphere(1.0f, 48, 32, false);
        Mesh sphereFlat = MeshFactory.createSphere(1.0f, 48, 32, true);
        Mesh house = MeshFactory.createHouse(2.0f, false);
        Mesh houseFlat = MeshFactory.createHouse(2.0f, true);
        Mesh ground = MeshFactory.createGroundPlane(20.0f);

        // --- Tekstur: file dulu, fallback prosedural ---
        texture = loadTexture();

        // --- Shadow FBO + Renderer ---
        ShadowMapFBO shadowFBO = new ShadowMapFBO();
        Renderer renderer = new Renderer(phongShader, depthShader, shadowFBO);

        HSRManager.setupDepthTest(true);
        HSRManager.setupBackFaceCulling(true);

        printStatus("Program dimulai");

        // --- Render loop ---
        fpsLastTime = glfwGetTime();
        while (!glfwWindowShouldClose(window)) {
            renderer.render(sphere, sphereFlat, house, houseFlat, ground,
                    texture, camera, WIDTH, HEIGHT, state);
            glfwSwapBuffers(window);
            glfwPollEvents();

            // Hitung FPS tiap 1 detik dan cetak ke console.
            fpsFrames++;
            double now = glfwGetTime();
            if (now - fpsLastTime >= 1.0) {
                lastFps = (int) Math.round(fpsFrames / (now - fpsLastTime));
                fpsFrames = 0;
                fpsLastTime = now;
                printStatus("FPS: " + lastFps);
            }
        }

        // --- Cleanup ---
        sphere.delete();
        sphereFlat.delete();
        house.delete();
        houseFlat.delete();
        ground.delete();
        texture.delete();
        shadowFBO.delete();
        phongShader.delete();
        depthShader.delete();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private Texture2D loadTexture() {
        String[] candidates = {
                "src/main/resources/textures/brick.png",
                "src/main/resources/textures/checker.png"
        };
        for (String path : candidates) {
            if (new File(path).exists()) {
                System.out.println("Memuat tekstur: " + path);
                return new Texture2D(path);
            }
        }
        System.out.println("File tekstur tidak ditemukan, memakai checker prosedural.");
        return TextureGenerator.createCheckerTexture();
    }

    private void initWindow() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Gagal init GLFW");
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        window = glfwCreateWindow(WIDTH, HEIGHT,
                "3D Visual Realism Suite - Grafika Komputer Minggu 11", NULL, NULL);
        if (window == NULL) {
            throw new IllegalStateException("Gagal membuat window GLFW");
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // V-Sync

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action != GLFW_PRESS) {
                return;
            }
            switch (key) {
                case GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(win, true);
                case GLFW_KEY_B -> {
                    state.useBlinnPhong = !state.useBlinnPhong;
                    printStatus("Shading: " + (state.useBlinnPhong ? "Blinn-Phong" : "Phong"));
                }
                case GLFW_KEY_F -> {
                    state.useFlatShading = !state.useFlatShading;
                    printStatus("Shading permukaan: " + (state.useFlatShading ? "Flat" : "Smooth"));
                }
                case GLFW_KEY_Z -> {
                    state.depthTestEnabled = !state.depthTestEnabled;
                    printStatus("Z-Buffer Depth Test: " + (state.depthTestEnabled ? "ON" : "OFF"));
                }
                case GLFW_KEY_C -> {
                    state.cullingEnabled = !state.cullingEnabled;
                    printStatus("Back-Face Culling: " + (state.cullingEnabled ? "ON" : "OFF"));
                }
                case GLFW_KEY_T -> {
                    state.useTexture = !state.useTexture;
                    printStatus("Texture: " + (state.useTexture ? "ON" : "OFF"));
                }
                case GLFW_KEY_M -> {
                    state.nearestFilter = !state.nearestFilter;
                    if (texture != null) {
                        if (state.nearestFilter) {
                            texture.setFilterNearest();
                        } else {
                            texture.setFilterLinear();
                        }
                    }
                    printStatus("Filter tekstur: "
                            + (state.nearestFilter ? "Nearest" : "Bilinear-Mipmap"));
                }
                case GLFW_KEY_S -> {
                    state.useShadow = !state.useShadow;
                    printStatus("Shadow Mapping: " + (state.useShadow ? "ON" : "OFF"));
                }
                default -> {
                    // tombol lain diabaikan
                }
            }
        });

        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                leftMouseDown = (action == GLFW_PRESS);
                double[] x = new double[1];
                double[] y = new double[1];
                glfwGetCursorPos(win, x, y);
                lastMouseX = x[0];
                lastMouseY = y[0];
            }
        });

        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (leftMouseDown) {
                camera.orbit((float) (xpos - lastMouseX), (float) (ypos - lastMouseY));
                lastMouseX = xpos;
                lastMouseY = ypos;
            }
        });

        glfwSetScrollCallback(window, (win, xoffset, yoffset) ->
                camera.zoom((float) (-yoffset * 0.5f)));
    }

    private void printStatus(String message) {
        String status = String.format(
                "[%s] | Shading=%s | Permukaan=%s | Depth=%s | Culling=%s | Texture=%s | Filter=%s | Shadow=%s",
                message,
                state.useBlinnPhong ? "Blinn-Phong" : "Phong",
                state.useFlatShading ? "Flat" : "Smooth",
                state.depthTestEnabled ? "ON" : "OFF",
                state.cullingEnabled ? "ON" : "OFF",
                state.useTexture ? "ON" : "OFF",
                state.nearestFilter ? "Nearest" : "Bilinear",
                state.useShadow ? "ON" : "OFF");
        System.out.println(status);
        glfwSetWindowTitle(window, "3D Visual Realism Suite | " + message
                + " | B:Phong/Blinn  F:Flat/Smooth  Z:Depth  C:Culling  T:Texture  M:Filter  S:Shadow");
    }
}
