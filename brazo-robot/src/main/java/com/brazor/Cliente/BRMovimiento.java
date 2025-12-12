package com.brazor.Cliente;

import static org.lwjgl.glfw.GLFW.*;

//Se encarga solo de leer el teclado y mandar comandos
public class BRMovimiento {
    private NetworkClient net;

    public BRMovimiento(NetworkClient net) {
        this.net = net;
    }

    public void processInput(long window) {
        //Migramos lo que teniamos en RobotRenderer
        // BASE
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) net.sendCommand("MOVE_BASE:1.5");
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) net.sendCommand("MOVE_BASE:-1.5");
        
        // HOMBRO
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) net.sendCommand("MOVE_HOMBRO:1.5");
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) net.sendCommand("MOVE_HOMBRO:-1.5");
        
        // CODO
        if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) net.sendCommand("MOVE_CODO:1.5");
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) net.sendCommand("MOVE_CODO:-1.5");
        
        // MUÑECA ROTACIÓN
        if (glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS) net.sendCommand("MOVE_MUN1:1.5");
        if (glfwGetKey(window, GLFW_KEY_F) == GLFW_PRESS) net.sendCommand("MOVE_MUN1:-1.5");
        
        // PINZA
        if (glfwGetKey(window, GLFW_KEY_Y) == GLFW_PRESS) net.sendCommand("MOVE_PINZA:0.02");
        if (glfwGetKey(window, GLFW_KEY_H) == GLFW_PRESS) net.sendCommand("MOVE_PINZA:-0.02");

        // PEDIR CONTROL
        if (glfwGetKey(window, GLFW_KEY_ENTER) == GLFW_PRESS) net.sendCommand("TAKE_CONTROL");

        net.sendCommand("PING");// Sirve para recibir lo que hizo otros cliente (Actualizar estado) ya no por teclado
    }
}