package com.brazor.Cliente;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import com.brazor.GC.Camera;
import com.brazor.GC.RobotRenderer;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*; 
import static org.lwjgl.system.MemoryUtil.NULL;

public class ClientMain {
    
    public static void main(String[] args) {
        //Objetos a llamar
        RobotRenderer robot = new RobotRenderer();
        NetworkClient network = new NetworkClient(robot);
        InputHandler input = new InputHandler(network);

        //Conexion de red
        network.ConClientServer("localhost", 5000);

        //Inicializacion de GLFW
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Error GLFW");
        glfwWindowHint(GLFW_SAMPLES, 4);

        long window = glfwCreateWindow(1024, 768, "Cliente Robot Modular", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Error ventana");

        Camera camera = new Camera();
        camera.registerCallbacks(window);

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        //CONFIGURACION OPENGL
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LIGHTING); 
        glEnable(GL_LIGHT0);
        glEnable(GL_NORMALIZE); 
        glEnable(GL_MULTISAMPLE);
        glShadeModel(GL_SMOOTH);

        // Luces
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glLightfv(GL_LIGHT0, GL_POSITION, stack.floats(4.0f, 10.0f, 6.0f, 1.0f));
            glLightfv(GL_LIGHT0, GL_DIFFUSE, stack.floats(1.0f, 1.0f, 1.0f, 1.0f));
            glLightfv(GL_LIGHT0, GL_SPECULAR, stack.floats(0.8f, 0.8f, 0.8f, 1.0f));
            glLightModelfv(GL_LIGHT_MODEL_AMBIENT, stack.floats(0.3f, 0.3f, 0.35f, 1.0f));
        }

        //BUCLE PRINCIPAL
        while (!glfwWindowShouldClose(window)) {
            // Inputs delegados al InputHandler
            input.processInput(window);
            camera.input(window);

            // Render
            glClearColor(0.25f, 0.25f, 0.25f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            camera.apply();
            
            // Dibujar Rejilla
            glDisable(GL_LIGHTING); glLineWidth(1.0f); glBegin(GL_LINES); glColor3f(0.4f, 0.4f, 0.4f);
            for(int i=-10; i<=10; i++) { glVertex3f(-10,0,i); glVertex3f(10,0,i); glVertex3f(i,0,-10); glVertex3f(i,0,10); }
            glEnd(); glEnable(GL_LIGHTING);

            // Dibujar Robot (Sus datos se actualizan solos gracias a NetworkClient)
            robot.draw();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        
        //LIMPIEZA
        network.close();
        glfwTerminate();
    }
}