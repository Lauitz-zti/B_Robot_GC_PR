package com.brazor.Cliente;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Camera {
    // Estado de la camara
    public float angleX = 25.0f;
    public float angleY = 45.0f;
    public float distance = 9.0f;
    
    // Variables para el mouse
    private double lastX = 0, lastY = 0;
    private boolean scroll = false;

    // Aplica la transformacion de la camara (El "ojo")
    public void apply() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        float aspect = 1024.0f / 768.0f;
        float fH = (float)Math.tan(Math.toRadians(45)/2) * 0.1f;
        float fW = fH * aspect;
        glFrustum(-fW, fW, -fH, fH, 0.1f, 100.0f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(0.0f, -1.0f, -distance);
        glRotatef(angleX, 1, 0, 0);
        glRotatef(angleY, 0, 1, 0);
    }

    // Procesa el mouse
    public void input(long window) {
        // Zoom con rueda
        if (scroll) {
            double[] x = new double[1], y = new double[1];
            glfwGetCursorPos(window, x, y);
            angleY += (x[0] - lastX) * 0.4f;
            angleX += (y[0] - lastY) * 0.4f;
            lastX = x[0]; lastY = y[0];
        }
    }

    // Callbacks para conectar con GLFW en el Main
    public void registerCallbacks(long window) {
        glfwSetMouseButtonCallback(window, (win, boton, action, mods) -> {
            if (boton == 0) {
                scroll = (action == GLFW_PRESS);
                double[] x = new double[1], y = new double[1];
                glfwGetCursorPos(win, x, y);
                lastX = x[0]; lastY = y[0];
            }
        });
        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> distance -= yoffset * 0.5f);
    }
}
