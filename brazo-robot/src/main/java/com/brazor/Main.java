package com.brazor;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*; 
import static org.lwjgl.system.MemoryUtil.NULL;
import org.lwjgl.system.MemoryStack;

import com.brazor.Cliente.Camera;
import com.brazor.Cliente.RobotRenderer;

public class Main {
    
    public static void main(String[] args) {
        //Iniacializacion
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Error al iniciar GLFW");
        glfwWindowHint(GLFW_SAMPLES, 4); // Activar multisampling de 4 muestras

        long window = glfwCreateWindow(1024, 768, "Brazo Robot", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Error al crear ventana");

        Camera camera = new Camera();
        RobotRenderer robot = new RobotRenderer();
        
        // Conectar callbacks de la camara
        camera.registerCallbacks(window);

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        //Configuracion Global OpenGL (Luces, profundidad, etc)
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_NORMALIZE); 
        glShadeModel(GL_SMOOTH);
        
        // Configurar Luz Fija
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glLightfv(GL_LIGHT0, GL_POSITION, stack.floats(4.0f, 10.0f, 6.0f, 1.0f));
            glLightfv(GL_LIGHT0, GL_DIFFUSE, stack.floats(1.0f, 1.0f, 1.0f, 1.0f));
            glLightModelfv(GL_LIGHT_MODEL_AMBIENT, stack.floats(0.3f, 0.3f, 0.35f, 1.0f));
        }

        //Bucle Principal
        while (!glfwWindowShouldClose(window)) {
            // Inputs
            camera.input(window);
            //robot.input(window);

            // Render
            glClearColor(0.25f, 0.25f, 0.25f, 1.0f); // Gris
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Aplicar Camara
            camera.apply();

           //Suelo malla
            glPushMatrix();
                glTranslatef(0, -0.05f, 0); 
                dibujarGrid(10, 1.0f); // Rejilla de 20x20 metros
            glPopMatrix();

            // Dibujar Robot
            robot.draw();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        glfwTerminate();
    }

    static void materialPlata() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, stack.floats(0.8f, 0.8f, 0.85f, 1.0f));
            glMaterialfv(GL_FRONT, GL_SPECULAR, stack.floats(0.9f, 0.9f, 0.9f, 1.0f));
            glMaterialf(GL_FRONT, GL_SHININESS, 60.0f);
        }
    }

    // Dibuja una rejilla tecnica infinita
    static void dibujarGrid(int size, float step) {
        glDisable(GL_LIGHTING); // La rejilla son lineas, no necesitamos luz
        glLineWidth(1.0f);
        
        glBegin(GL_LINES);
        glColor3f(0.5f, 0.5f, 0.5f); // Color gris medio para las lineas
        for(float i = -size; i <= size; i += step) {
            // ineas paralelas al eje X
            glVertex3f(-size, 0, i); 
            glVertex3f(size, 0, i);
            // Lineas paralelas al eje Z
            glVertex3f(i, 0, -size); 
            glVertex3f(i, 0, size);
        }
        // Ejes principales mas oscuros
        glColor3f(0.2f, 0.2f, 0.2f);
            glVertex3f(-size, 0, 0); 
            glVertex3f(size, 0, 0);
            glVertex3f(0, 0, -size); 
            glVertex3f(0, 0, size);
        glEnd();
        
        glEnable(GL_LIGHTING); // Volvemos a activar luces para el robot
    }
}