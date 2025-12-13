package com.brazor.Cliente;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import com.brazor.Cinematic.CinematicDirector;
import com.brazor.GC.Camera;
import com.brazor.GC.RobotRenderer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*; 
import static org.lwjgl.system.MemoryUtil.NULL;

public class ClientMain {
    
    public static boolean modoEspera = true; // VARIABLE DE ESTADO: true = Pantalla de Carga, false = Control Manual
    private static CinematicDirector director;//Detener la cinematica si se despierta el cliente

    //Avisa a UDPListener que debe cambiar de modo
    public static void despertar() {
        System.out.println("Iniciando modo de control manual...");
        modoEspera = false;
        if (director != null) 
            director.stop();
    }

    public static void main(String[] args) {
         //Objetos a llamar
        RobotRenderer robot = new RobotRenderer();
        NetworkClient network = new NetworkClient(robot);
        BRMovimiento movimiento = new BRMovimiento(network);
        UdpListener udpListener = new UdpListener(robot, 6000); //ESCUCHAR actualizaciones rápidas (Servidor -> Pantalla)
                                                                    // Escucha en el puerto 6000 donde el servidor hace el broadcast
        Camera camera = new Camera();
       director = new CinematicDirector(camera);

        director.start();// ARRANCAMOS EN MODO CINE

        udpListener.startListening();
        network.ConClientServer("localhost", 5000);//Conexion de red

        //Inicializacion de GLFW
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Error GLFW");
        glfwWindowHint(GLFW_SAMPLES, 4);

        long window = glfwCreateWindow(1024, 768, "Cliente Robot Modular", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Error ventana");

        camera.registerCallbacks(window);

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        //CONFIGURACION OPENGL
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LIGHTING); glEnable(GL_LIGHT0);
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
            // Inputs delegados al Movimiento del Brazo
            movimiento.processInput(window);
            camera.input(window);

            //CONTROL DE LA CINEMATICA CAMARA
            if (director.isActive()) {
                // Si la demo corre, el "Director" mueve la camara
                director.update();
            } else {
                // Si no, el usuario usa el mouse
                camera.input(window);
                
                // Tecla "P" para iniciar demo manual (para probar)
                if (glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS) {
                    director.start();
                }
            }

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

            if (modoEspera) {
                director.renderTitleScreen(1024, 768);// Dibuja el titulo "ESPERANDO CONEXION..." 
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        
        //LIMPIEZA
        network.close();
        udpListener.close();
        glfwTerminate();
    }
}