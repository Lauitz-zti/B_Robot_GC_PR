package com.brazor.Comunicacion.Cliente;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import com.brazor.Graficos.Cinematic.CinematicDirector;
import com.brazor.Graficos.GC.Camera;
import com.brazor.Graficos.GC.RobotRenderer;

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
        RobotRenderer robot = new RobotRenderer();  // Modelo 3D del robot
        NetworkClient network = new NetworkClient(robot); // TCP para comandos
        BRMovimiento movimiento = new BRMovimiento(network); // Mapeo teclas→comandos
        UdpListener udpListener = new UdpListener(robot, 6000); //ESCUCHAR actualizaciones rápidas (Servidor -> Pantalla)
                                                                    // Escucha en el puerto 6000 donde el servidor hace el broadcast
        Camera camera = new Camera(); //Vista 3D

        udpListener.startListening(); // Hilo que escucha broadcast UDP, Actualizaciones en tiempo real cuando otros mueven el robot
        network.ConClientServer("localhost", 5000);//Conexion de red TCP, envia comandos al servidor

        //Inicializacion de GLFW
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Error GLFW");
        glfwWindowHint(GLFW_SAMPLES, 4);

        long window = glfwCreateWindow(1024, 768, "Cliente Robot Modular", NULL, NULL); //Crear ventana
        if (window == NULL) throw new RuntimeException("Error ventana");

        camera.registerCallbacks(window);

        glfwMakeContextCurrent(window);
        GL.createCapabilities(); //Se crea el mundo OpenGL

        //Esto no puede existir antes de que se cree el mundo por eso es imprtnte que se cree despues
        director = new CinematicDirector(camera);
        director.start();// ARRANCAMOS EN MODO CINE

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

            // Si presionas ENTER, fuerzas el desbloqueo SIEMPRE
            if (modoEspera && glfwGetKey(window, GLFW_KEY_ENTER) == GLFW_PRESS) {
                System.out.println("Modo Offline");
                despertar(); 
            }

            //CONTROL DE LA CINEMATICA CAMARA
            if (modoEspera) {
                // Si la demo corre, el "Director" mueve la camara
                if (director != null) director.update();
            } else {
                // Si no, el usuario usa el mouse
                camera.input(window); // El usuario mueve la camara con el mouse
                movimiento.processInput(window); // El usuario mueve el robot con el teclado
                
                //Presionar ENTER en la PC, fuerzas el desbloqueo sin usar la red
                if (glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS) {
                    director.start();
                    modoEspera = true;
                    System.out.println("SISTEMA BLOQUEADO MANUALMENTE");
                }
            /*aunque presionemos las teclas, el objeto movimiento nunca recibe la orden de leerlas 
            la laptop quedara efectivamente "bloqueada" (solo viendo la animacion) 
            hasta que pidas el control de otro dispositivo */
            }

            // Render
            glClearColor(0.25f, 0.25f, 0.25f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            camera.apply(); // Aplicar transformaciones de camara
            
            // Dibujar Rejilla
            glDisable(GL_LIGHTING); 
            glLineWidth(1.0f); 
            glBegin(GL_LINES); 
            glColor3f(0.4f, 0.4f, 0.4f);

            for(int i=-10; i<=10; i++) {
                 glVertex3f(-10,0,i); 
                 glVertex3f(10,0,i); 
                 glVertex3f(i,0,-10); 
                 glVertex3f(i,0,10); 
                }
            glEnd(); 
            glEnable(GL_LIGHTING);

            robot.drawPiso();
            robot.draw(); // Dibujar Robot (Sus datos se actualizan solos gracias a NetworkClient)

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