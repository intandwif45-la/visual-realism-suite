package com.grafkom.viewing3d;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * MeshFactory - membangun mesh 3D bermateri:
 * Sphere UV, Cube/House, dan Ground Plane.
 *
 * Setiap mesh membawa posisi, normal, dan UV (8 float per vertex).
 * - Smooth: normal vertex = rata-rata normal face yang berbagi vertex.
 * - Flat: setiap face memakai vertex-nya sendiri, normal = normal face
 *   (mesh jadi faceted, Mach Band terlihat pada permukaan lengkung).
 */
public final class MeshFactory {

    private MeshFactory() {
    }

    // ---------------------------------------------------------------- Sphere
    public static Mesh createSphere(float radius, int sectors, int stacks, boolean flat) {
        List<Float> pos = new ArrayList<>();
        List<Float> uv = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();

        for (int i = 0; i <= stacks; i++) {
            float phi = (float) (Math.PI * i / stacks);          // 0..PI
            for (int j = 0; j <= sectors; j++) {
                float theta = (float) (2.0 * Math.PI * j / sectors); // 0..2PI
                float x = radius * (float) (Math.sin(phi) * Math.cos(theta));
                float y = radius * (float) Math.cos(phi);
                float z = radius * (float) (Math.sin(phi) * Math.sin(theta));
                pos.add(x);
                pos.add(y);
                pos.add(z);
                uv.add((float) j / sectors);
                uv.add(1.0f - (float) i / stacks);
            }
        }
        int cols = sectors + 1;
        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < sectors; j++) {
                int a = i * cols + j;
                int b = a + cols;
                int c = a + 1;
                int d = b + 1;
                idx.add(a);
                idx.add(b);
                idx.add(c);
                idx.add(c);
                idx.add(b);
                idx.add(d);
            }
        }
        float[] positions = toFloatArray(pos);
        float[] uvs = toFloatArray(uv);
        int[] indices = toIntArray(idx);
        if (flat) {
            return buildFlatMesh(positions, uvs, indices);
        }
        return buildSmoothMesh(positions, uvs, indices);
    }

    // ------------------------------------------------------------------ Cube
    /** Kubus dengan UV per face (dipakai juga sebagai badan rumah). */
    public static Mesh createCube(float size, boolean flat) {
        float h = size / 2.0f;
        // 24 vertex: 4 per face x 6 face, supaya tiap face punya UV sendiri.
        float[] positions = {
                // depan (+z)
                -h, -h, h, h, -h, h, h, h, h, -h, h, h,
                // belakang (-z)
                h, -h, -h, -h, -h, -h, -h, h, -h, h, h, -h,
                // kiri (-x)
                -h, -h, -h, -h, -h, h, -h, h, h, -h, h, -h,
                // kanan (+x)
                h, -h, h, h, -h, -h, h, h, -h, h, h, h,
                // atas (+y)
                -h, h, h, h, h, h, h, h, -h, -h, h, -h,
                // bawah (-y)
                -h, -h, -h, h, -h, -h, h, -h, h, -h, -h, h
        };
        float[] uvs = new float[24 * 2];
        for (int f = 0; f < 6; f++) {
            uvs[f * 8 + 0] = 0; uvs[f * 8 + 1] = 0;
            uvs[f * 8 + 2] = 1; uvs[f * 8 + 3] = 0;
            uvs[f * 8 + 4] = 1; uvs[f * 8 + 5] = 1;
            uvs[f * 8 + 6] = 0; uvs[f * 8 + 7] = 1;
        }
        int[] indices = new int[36];
        for (int f = 0; f < 6; f++) {
            int b = f * 4;
            indices[f * 6 + 0] = b;
            indices[f * 6 + 1] = b + 1;
            indices[f * 6 + 2] = b + 2;
            indices[f * 6 + 3] = b;
            indices[f * 6 + 4] = b + 2;
            indices[f * 6 + 5] = b + 3;
        }
        if (flat) {
            return buildFlatMesh(positions, uvs, indices);
        }
        return buildSmoothMesh(positions, uvs, indices);
    }

    // ----------------------------------------------------------------- House
    /**
     * Rumah 3D: kubus (badan) + prisma atap (2 segitiga + 2 persegi).
     * Disusun sebagai satu mesh gabungan; atap selalu flat,
     * badan mengikuti parameter flat.
     */
    public static Mesh createHouse(float size, boolean flat) {
        float h = size / 2.0f;
        float roofH = size * 0.6f;
        // Badan: kubus, tanpa face atas (tertutup atap) - pakai createCube lalu
        // digabung dengan atap di sini secara manual agar tetap satu Mesh.
        List<Float> pos = new ArrayList<>();
        List<Float> uv = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();

        // --- badan kubus (5 face, tanpa atas) ---
        float[][] faces = {
                {-h, -h, h, h, -h, h, h, h, h, -h, h, h},       // depan
                {h, -h, -h, -h, -h, -h, -h, h, -h, h, h, -h},   // belakang
                {-h, -h, -h, -h, -h, h, -h, h, h, -h, h, -h},   // kiri
                {h, -h, h, h, -h, -h, h, h, -h, h, h, h},       // kanan
                {-h, -h, -h, h, -h, -h, h, -h, h, -h, -h, h}    // bawah
        };
        for (float[] f : faces) {
            int base = pos.size() / 3;
            for (float v : f) {
                pos.add(v);
            }
            uv.add(0f); uv.add(0f); uv.add(1f); uv.add(0f);
            uv.add(1f); uv.add(1f); uv.add(0f); uv.add(1f);
            idx.add(base); idx.add(base + 1); idx.add(base + 2);
            idx.add(base); idx.add(base + 2); idx.add(base + 3);
        }
        // --- atap prisma: punggung di (0, h+roofH, 0), sisi di z = +/-h ---
        // segitiga depan (z = +h)
        addTriangle(pos, uv, idx,
                -h, h, h, h, h, h, 0, h + roofH, h);
        // segitiga belakang (z = -h)
        addTriangle(pos, uv, idx,
                h, h, -h, -h, h, -h, 0, h + roofH, -h);
        // persegi miring kiri
        addQuad(pos, uv, idx,
                -h, h, -h, -h, h, h, 0, h + roofH, h, 0, h + roofH, -h);
        // persegi miring kanan
        addQuad(pos, uv, idx,
                h, h, h, h, h, -h, 0, h + roofH, -h, 0, h + roofH, h);

        float[] positions = toFloatArray(pos);
        float[] uvs = toFloatArray(uv);
        int[] indices = toIntArray(idx);
        if (flat) {
            return buildFlatMesh(positions, uvs, indices);
        }
        return buildSmoothMesh(positions, uvs, indices);
    }

    private static void addTriangle(List<Float> pos, List<Float> uv, List<Integer> idx,
                                    float... v) {
        int base = pos.size() / 3;
        for (float f : v) {
            pos.add(f);
        }
        uv.add(0f); uv.add(0f); uv.add(1f); uv.add(0f); uv.add(0.5f); uv.add(1f);
        idx.add(base); idx.add(base + 1); idx.add(base + 2);
    }

    private static void addQuad(List<Float> pos, List<Float> uv, List<Integer> idx,
                                float... v) {
        int base = pos.size() / 3;
        for (float f : v) {
            pos.add(f);
        }
        uv.add(0f); uv.add(0f); uv.add(1f); uv.add(0f);
        uv.add(1f); uv.add(1f); uv.add(0f); uv.add(1f);
        idx.add(base); idx.add(base + 1); idx.add(base + 2);
        idx.add(base); idx.add(base + 2); idx.add(base + 3);
    }

    // ------------------------------------------------------------ Ground
    /** Bidang lantai besar (penerima bayangan), normal +Y, UV diulang. */
    public static Mesh createGroundPlane(float size) {
        float h = size / 2.0f;
        float[] positions = {-h, 0, -h, h, 0, -h, h, 0, h, -h, 0, h};
        float[] uvs = {0, 0, 8, 0, 8, 8, 0, 8};
        int[] indices = {0, 1, 2, 0, 2, 3};
        return buildSmoothMesh(positions, uvs, indices);
    }

    // ------------------------------------------------------- Normal builders
    /** Smooth: normal vertex = rata-rata normal face yang berbagi vertex. */
    public static Mesh buildSmoothMesh(float[] positions, float[] uvs, int[] indices) {
        int vertexCount = positions.length / 3;
        Vector3f[] normals = new Vector3f[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            normals[i] = new Vector3f();
        }
        for (int t = 0; t < indices.length; t += 3) {
            Vector3f a = vec(positions, indices[t]);
            Vector3f b = vec(positions, indices[t + 1]);
            Vector3f c = vec(positions, indices[t + 2]);
            Vector3f faceNormal = new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a));
            if (faceNormal.lengthSquared() > 0) {
                faceNormal.normalize();
            }
            normals[indices[t]].add(faceNormal);
            normals[indices[t + 1]].add(faceNormal);
            normals[indices[t + 2]].add(faceNormal);
        }
        float[] interleaved = new float[vertexCount * 8];
        for (int i = 0; i < vertexCount; i++) {
            Vector3f n = normals[i];
            if (n.lengthSquared() > 0) {
                n.normalize();
            }
            interleaved[i * 8] = positions[i * 3];
            interleaved[i * 8 + 1] = positions[i * 3 + 1];
            interleaved[i * 8 + 2] = positions[i * 3 + 2];
            interleaved[i * 8 + 3] = n.x;
            interleaved[i * 8 + 4] = n.y;
            interleaved[i * 8 + 5] = n.z;
            interleaved[i * 8 + 6] = uvs[i * 2];
            interleaved[i * 8 + 7] = uvs[i * 2 + 1];
        }
        return new Mesh(interleaved, indices);
    }

    /** Flat: setiap face memakai vertex-nya sendiri, normal = normal face. */
    public static Mesh buildFlatMesh(float[] positions, float[] uvs, int[] indices) {
        int triCount = indices.length / 3;
        float[] interleaved = new float[triCount * 3 * 8];
        int[] flatIndices = new int[indices.length];
        for (int t = 0; t < triCount; t++) {
            Vector3f a = vec(positions, indices[t * 3]);
            Vector3f b = vec(positions, indices[t * 3 + 1]);
            Vector3f c = vec(positions, indices[t * 3 + 2]);
            Vector3f faceNormal = new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a));
            if (faceNormal.lengthSquared() > 0) {
                faceNormal.normalize();
            }
            for (int k = 0; k < 3; k++) {
                int src = indices[t * 3 + k];
                int dst = t * 3 + k;
                interleaved[dst * 8] = positions[src * 3];
                interleaved[dst * 8 + 1] = positions[src * 3 + 1];
                interleaved[dst * 8 + 2] = positions[src * 3 + 2];
                interleaved[dst * 8 + 3] = faceNormal.x;
                interleaved[dst * 8 + 4] = faceNormal.y;
                interleaved[dst * 8 + 5] = faceNormal.z;
                interleaved[dst * 8 + 6] = uvs[src * 2];
                interleaved[dst * 8 + 7] = uvs[src * 2 + 1];
                flatIndices[dst] = dst;
            }
        }
        return new Mesh(interleaved, flatIndices);
    }

    private static Vector3f vec(float[] positions, int vertexIndex) {
        return new Vector3f(positions[vertexIndex * 3],
                positions[vertexIndex * 3 + 1], positions[vertexIndex * 3 + 2]);
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] out = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            out[i] = list.get(i);
        }
        return out;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] out = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            out[i] = list.get(i);
        }
        return out;
    }
}
