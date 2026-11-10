package com.grafkom.viewing3d;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

/**
 * TextureGenerator - tekstur prosedural checkerboard di runtime.
 * Dipakai sebagai fallback kalau file tekstur di
 * src/main/resources/textures/ tidak ditemukan, supaya
 * program tetap bisa jalan dan menunjukkan texture mapping.
 */
public final class TextureGenerator {

    private TextureGenerator() {
    }

    public static Texture2D createCheckerTexture() {
        return createCheckerTexture(256, 8);
    }

    public static Texture2D createCheckerTexture(int size, int squares) {
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        ByteBuffer data = MemoryUtil.memAlloc(size * size * 4);
        int cell = size / squares;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean white = ((x / cell) + (y / cell)) % 2 == 0;
                byte c = white ? (byte) 235 : (byte) 60;
                data.put(c);       // R
                data.put(c);       // G
                data.put((byte) 110); // B (sedikit biru supaya terlihat)
                data.put((byte) 255); // A
            }
        }
        data.flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size, size,
                0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        glGenerateMipmap(GL_TEXTURE_2D);
        MemoryUtil.memFree(data);
        System.out.println("Tekstur checker prosedural dibuat (fallback, file tidak ditemukan).");
        return new Texture2D(textureID);
    }
}
