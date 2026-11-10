package com.grafkom.viewing3d;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Camera - kamera orbit sederhana (JOML).
 * Kamera mengorbit di sekitar target (0, 1, 0) pada jarak
 * tertentu; drag mouse memutar yaw/pitch, scroll mengubah jarak.
 */
public class Camera {

    private final Vector3f target = new Vector3f(0.0f, 1.0f, 0.0f);
    private float yaw = -45.0f;      // derajat
    private float pitch = 20.0f;     // derajat
    private float distance = 9.0f;   // jarak ke target
    private final Vector3f position = new Vector3f();

    public Camera() {
        updatePosition();
    }

    public void orbit(float dx, float dy) {
        yaw += dx * 0.4f;
        pitch += dy * 0.4f;
        if (pitch > 85.0f) {
            pitch = 85.0f;
        }
        if (pitch < -85.0f) {
            pitch = -85.0f;
        }
        updatePosition();
    }

    public void zoom(float amount) {
        distance += amount;
        if (distance < 3.0f) {
            distance = 3.0f;
        }
        if (distance > 30.0f) {
            distance = 30.0f;
        }
        updatePosition();
    }

    private void updatePosition() {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        position.x = target.x + distance * (float) Math.cos(pitchRad) * (float) Math.sin(yawRad);
        position.y = target.y + distance * (float) Math.sin(pitchRad);
        position.z = target.z + distance * (float) Math.cos(pitchRad) * (float) Math.cos(yawRad);
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, target, new Vector3f(0.0f, 1.0f, 0.0f));
    }

    public Matrix4f getProjectionMatrix(float aspect) {
        return new Matrix4f().perspective((float) Math.toRadians(45.0f), aspect, 0.1f, 100.0f);
    }
}
