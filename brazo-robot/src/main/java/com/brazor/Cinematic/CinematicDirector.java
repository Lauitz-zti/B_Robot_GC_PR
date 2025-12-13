package com.brazor.Cinematic;

import static org.lwjgl.opengl.GL11.*;
import com.brazor.GC.Camera;

public class CinematicDirector {
    
    private boolean isPlaying = false;
    private long startTime;
    private Camera camera;// Referencia a la camara para poder moverla

    public CinematicDirector(Camera camera) {
        this.camera = camera;
    }

    public void start() {
        this.isPlaying = true;
        this.startTime = System.currentTimeMillis();
        System.out.println("CINEMATICA INICIADA");
    }

    public void stop() {
        this.isPlaying = false;
        System.out.println("DETENIENDO CINE");
        // Restaurar camara a posicion original
        camera.setPosition(0, 5, 20); 
        camera.setRotation(15, 0);
    }

    public boolean isActive() {
        return isPlaying;
    }

    public void renderTitleScreen(int width, int height) {
        if (!isPlaying) return;

        //Frame para el titulo de carga
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(0.0f, 0.0f, 0.0f, 0.5f); // Negro al 50%
        
        glMatrixMode(GL_PROJECTION); glPushMatrix(); glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW); glLoadIdentity();
        glDisable(GL_DEPTH_TEST);

        // Dibuja una franja negra en el centro
        glRectf(0, height/2 - 50, width, height/2 + 50);

        //Simulacion de Texto "ESPERANDO CONEXION..."
        //Como no tenemos nativamente drawText, usaremos una imagen :)
        //...... en proceso
        
        // Restaurar estado
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glMatrixMode(GL_PROJECTION); glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    //Camara dinamica (simulamos el girar siempre hasta esprar usuario)
    public void update() {
        if (!isPlaying) return;

        long timeLoop = (System.currentTimeMillis() - startTime) % 30000;// Usamos '%' para que el tiempo vaya de 0 a 30 una y otra vez
        float t = timeLoop / 1000.0f;// Convertir tiempo a segundos para cálculos matemáticos

        //FASE 1
        // La camara hace un Zoom In suave
        if (timeLoop < 5000) {
            // Acercarse de Z=30 a Z=15
            float zoom = 30 - (t / 5.0f) * 15;
            camera.setPosition(0, 5, zoom);
            camera.setRotation(10, 0);
        }
        //FASE 2
        // La camara gira alrededor del robot (Showcase)
        else if (timeLoop < 25000) {
            float angle = (t - 5) * 0.5f; // Velocidad de giro
            float radio = 15.0f;
            
            float x = (float) Math.sin(angle) * radio;
            float z = (float) Math.cos(angle) * radio;
            
            camera.setPosition(x, 8, z);
            
            //la camara siempre mira al centro (0,0,0)
            // Calculamos el angulo Y invertido + offset
            float angleY = (float) Math.toDegrees(angle) + 180; 
            camera.setRotation(20, angleY); // 20 grados hacia abajo
        }
        //FASE 3: SALIDA
        else {
            float progress = (t - 25) / 5.0f;
            float zoom = 15 + progress * 10; // Alejarse de 15 a 25
            camera.setPosition(0, 8, zoom);
            camera.setRotation(20, 0); // Volver al frente
        }
    }
    
}