package com.brazor.Modelado;

import static org.lwjgl.opengl.GL11.*;

public class GlutTools {

    /*  Imitacion de glutSolidCube
    public static void solidCube(float size) {
        float x = size / 2;
        glBegin(GL_QUADS);
            // Frontal
            glNormal3f(0, 0, 1); glVertex3f(-x, -x,  x); glVertex3f( x, -x,  x); glVertex3f( x,  x,  x); glVertex3f(-x,  x,  x);
            // Trasera
            glNormal3f(0, 0, -1); glVertex3f(-x, -x, -x); glVertex3f(-x,  x, -x); glVertex3f( x,  x, -x); glVertex3f( x, -x, -x);
            // Izquierda
            glNormal3f(-1, 0, 0); glVertex3f(-x, -x, -x); glVertex3f(-x, -x,  x); glVertex3f(-x,  x,  x); glVertex3f(-x,  x, -x);
            // Derecha
            glNormal3f(1, 0, 0); glVertex3f( x, -x, -x); glVertex3f( x,  x, -x); glVertex3f( x,  x,  x); glVertex3f( x, -x,  x);
            // Arriba
            glNormal3f(0, 1, 0); glVertex3f(-x,  x, -x); glVertex3f(-x,  x,  x); glVertex3f( x,  x,  x); glVertex3f( x,  x, -x);
            // Abajo
            glNormal3f(0, -1, 0); glVertex3f(-x, -x, -x); glVertex3f( x, -x, -x); glVertex3f( x, -x,  x); glVertex3f(-x, -x,  x);
        glEnd();
    }*/

    // Imitacion de un cubo solido pero con dimensiones personalizadas
    public static void solidBox(float w, float h, float d) {
        float x = w/2, y = h/2, z = d/2;
        // h = altura, d = largo w = ancho
        glBegin(GL_QUADS);
            //Frontal
            glNormal3f(0, 0, 1); glVertex3f(-x, -y,  z); glVertex3f( x, -y,  z); glVertex3f( x,  y,  z); glVertex3f(-x,  y,  z);
            //Trasera
            glNormal3f(0, 0, -1); glVertex3f(-x, -y, -z); glVertex3f(-x,  y, -z); glVertex3f( x,  y, -z); glVertex3f( x, -y, -z);
            //Izquierda
            glNormal3f(-1, 0, 0); glVertex3f(-x, -y, -z); glVertex3f(-x, -y,  z); glVertex3f(-x,  y,  z); glVertex3f(-x,  y, -z);
            //Derecha
            glNormal3f(1, 0, 0); glVertex3f( x, -y, -z); glVertex3f( x,  y, -z); glVertex3f( x,  y,  z); glVertex3f( x, -y,  z);
            //Arriba
            glNormal3f(0, 1, 0); glVertex3f(-x,  y, -z); glVertex3f(-x,  y,  z); glVertex3f( x,  y,  z); glVertex3f( x,  y, -z);
            //Abajo
            glNormal3f(0, -1, 0); glVertex3f(-x, -y, -z); glVertex3f( x, -y, -z); glVertex3f( x, -y,  z); glVertex3f(-x, -y,  z);
        glEnd();
    }

    // Imitacion de glutSolidSphere (Esfera)
    public static void solidSphere(float radius, int slices, int stacks) {
        // En OpenGL puro sin librerias externas, se hace con trigonometria basica
        for (int i = 0; i < stacks; i++) {
            float lat0 = (float) (Math.PI * (-0.5 + (double) (i) / stacks));
            float z0 = (float) Math.sin(lat0);
            float zr0 = (float) Math.cos(lat0);

            float lat1 = (float) (Math.PI * (-0.5 + (double) (i + 1) / stacks));
            float z1 = (float) Math.sin(lat1);
            float zr1 = (float) Math.cos(lat1);

            glBegin(GL_QUAD_STRIP);
            for (int j = 0; j <= slices; j++) {
                float lng = (float) (2 * Math.PI * (double) (j - 1) / slices);
                float x = (float) Math.cos(lng);
                float y = (float) Math.sin(lng);

                glNormal3f(x * zr0, y * zr0, z0);
                glVertex3f(x * zr0 * radius, y * zr0 * radius, z0 * radius);
                glNormal3f(x * zr1, y * zr1, z1);
                glVertex3f(x * zr1 * radius, y * zr1 * radius, z1 * radius);
            }
            glEnd();
        }
    }

    // Cilindro (util para los brazos del robot)
    public static void solidCylinder(float radius, float height, int slices) {
         float y = height/2;
        glBegin(GL_QUAD_STRIP);
        for(int i=0; i<=slices; i++) {
            double angle = (2 * Math.PI * i) / slices;
            float x = (float)Math.cos(angle);
            float z = (float)Math.sin(angle);
            glNormal3f(x, 0, z);
            glVertex3f(x*radius, -y, z*radius);
            glVertex3f(x*radius,  y, z*radius);
        }
        glEnd();
        // Tapas
        glBegin(GL_TRIANGLE_FAN);
            glNormal3f(0, 1, 0); glVertex3f(0, y, 0);
            for(int i=0; i<=slices; i++) {
                double a = (2 * Math.PI * i) / slices;
                glVertex3f((float)Math.cos(a)*radius, y, (float)Math.sin(a)*radius);
            }
        glEnd();
        glBegin(GL_TRIANGLE_FAN);
            glNormal3f(0, -1, 0); glVertex3f(0, -y, 0);
            for(int i=0; i<=slices; i++) {
                double a = -(2 * Math.PI * i) / slices;
                glVertex3f((float)Math.cos(a)*radius, -y, (float)Math.sin(a)*radius);
            }
        glEnd();
    }
}