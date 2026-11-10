package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

/**
 * Shader - kompilasi dan linking GLSL Vertex/Fragment Shader.
 * Menyediakan setter uniform yang dipakai Renderer dan Main.
 */
public class Shader {

    private final int programId;

    public Shader(String vertexPath, String fragmentPath) {
        String vertexSource = readFile(vertexPath);
        String fragmentSource = readFile(fragmentPath);
        int vertexId = compileShader(vertexSource, GL_VERTEX_SHADER);
        int fragmentId = compileShader(fragmentSource, GL_FRAGMENT_SHADER);
        programId = glCreateProgram();
        glAttachShader(programId, vertexId);
        glAttachShader(programId, fragmentId);
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new IllegalStateException("Gagal link shader program: "
                    + glGetProgramInfoLog(programId));
        }
        glValidateProgram(programId);
        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);
    }

    private static String readFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException("Tidak bisa membaca shader: " + path, e);
        }
    }

    private static int compileShader(String source, int type) {
        int shaderId = glCreateShader(type);
        glShaderSource(shaderId, source);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            String kind = (type == GL_VERTEX_SHADER) ? "vertex" : "fragment";
            throw new IllegalStateException("Gagal compile " + kind + " shader: "
                    + glGetShaderInfoLog(shaderId));
        }
        return shaderId;
    }

    public void use() {
        glUseProgram(programId);
    }

    public void delete() {
        glDeleteProgram(programId);
    }

    public int getProgramId() {
        return programId;
    }

    public void setMat4(String name, Matrix4f value) {
        int location = glGetUniformLocation(programId, name);
        if (location == -1) {
            return;
        }
        float[] m = new float[16];
        value.get(m);
        glUniformMatrix4fv(location, false, m);
    }

    public void setVec3(String name, Vector3f value) {
        int location = glGetUniformLocation(programId, name);
        if (location == -1) {
            return;
        }
        glUniform3f(location, value.x, value.y, value.z);
    }

    public void setVec3(String name, float x, float y, float z) {
        int location = glGetUniformLocation(programId, name);
        if (location == -1) {
            return;
        }
        glUniform3f(location, x, y, z);
    }

    public void setFloat(String name, float value) {
        int location = glGetUniformLocation(programId, name);
        if (location == -1) {
            return;
        }
        glUniform1f(location, value);
    }

    public void setInt(String name, int value) {
        int location = glGetUniformLocation(programId, name);
        if (location == -1) {
            return;
        }
        glUniform1i(location, value);
    }

    public void setBool(String name, boolean value) {
        setInt(name, value ? 1 : 0);
    }
}
