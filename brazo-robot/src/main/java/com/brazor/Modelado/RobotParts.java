package com.brazor.Modelado;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.system.MemoryStack;
public class RobotParts {

    //MATERIALES
    //Definicion de materiales reutilizables para las piezas del robot 
    public static void materialAzul() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glMaterialfv(GL_FRONT, GL_AMBIENT, stack.floats(0.0f, 0.1f, 0.3f, 1.0f));
            glMaterialfv(GL_FRONT, GL_DIFFUSE, stack.floats(0.1f, 0.4f, 0.9f, 1.0f));
            glMaterialfv(GL_FRONT, GL_SPECULAR, stack.floats(0.9f, 0.9f, 0.9f, 1.0f));
            glMaterialf(GL_FRONT, GL_SHININESS, 80.0f);
        }
    }

    public static void materialNegro() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glMaterialfv(GL_FRONT, GL_AMBIENT, stack.floats(0.05f, 0.05f, 0.05f, 1.0f));
            glMaterialfv(GL_FRONT, GL_DIFFUSE, stack.floats(0.1f, 0.1f, 0.1f, 1.0f));
            glMaterialfv(GL_FRONT, GL_SPECULAR, stack.floats(0.3f, 0.3f, 0.3f, 1.0f));
            glMaterialf(GL_FRONT, GL_SHININESS, 10.0f);
        }
    }

    public static void materialMetal() { // Para la pinza
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glMaterialfv(GL_FRONT, GL_AMBIENT, stack.floats(0.2f, 0.2f, 0.2f, 1.0f));
            glMaterialfv(GL_FRONT, GL_SPECULAR, stack.floats(0.8f, 0.8f, 0.8f, 1.0f));
            glMaterialf(GL_FRONT, GL_SHININESS, 50.0f);
        }
    }

    //PIEZAS DEL ROBOT
    public static void dibujarServo() {
        materialNegro(); // El servo siempre es negro
        glPushMatrix();
            // Cuerpo
            GlutTools.solidBox(0.4f, 0.5f, 0.3f);
            // Orejas
            glPushMatrix();
                glTranslatef(0, 0.15f, 0);
                GlutTools.solidBox(0.6f, 0.05f, 0.3f);
            glPopMatrix();
            // Eje
            glPushMatrix();
                glTranslatef(0, 0.25f, 0.1f);
                GlutTools.solidCylinder(0.08f, 0.1f, 16);
            glPopMatrix();
        glPopMatrix();
    }

        public static void dibujarBracketU(float w, float h, float d) {
            // Usamos el color naranja industrial
            materialAzul(); 
            
            float espesor = 0.05f;
            float margen = 0.001f; // Para evitar problemas capas
            glPushMatrix();
                // Fondo
                glPushMatrix(); 
                    glTranslatef(0, -h/2, -margen);
                    GlutTools.solidBox(w, espesor, d); 
                glPopMatrix();
                // Pared izq
                glPushMatrix(); 
                    glTranslatef(-w/2 + espesor/2, 0, 0); 
                    GlutTools.solidBox(espesor, h, d); 
                glPopMatrix();
                // Pared Der
                glPushMatrix(); 
                    glTranslatef(w/2 - espesor/2, 0, 0); 
                    GlutTools.solidBox(espesor, h, d); 
                glPopMatrix();

                //TORNILLOS (Circulos plateados)
                materialMetal();
                for(float y : new float[]{-h/4, h/4}) { // Dos tornillos por lado
                    glPushMatrix(); 
                        glTranslatef(-w/2, y, 0); 
                        glRotatef(90, 0, 1, 0); 
                        GlutTools.solidCylinder(0.06f, 0.06f, 8); 
                    glPopMatrix();
                    glPushMatrix(); 
                        glTranslatef( w/2, y, 0); 
                        glRotatef(90, 0, 1, 0); 
                        GlutTools.solidCylinder(0.06f, 0.06f, 8); 
                    glPopMatrix();
                }
            glPopMatrix();
        }

    public static void dibujarPinza(float apertura) {
        materialMetal(); // La pinza es gris metalica
        glPushMatrix();
            GlutTools.solidBox(0.6f, 0.1f, 0.3f); // Base
            // Engranajes decorativos
            glPushMatrix(); 
                glTranslatef(-0.15f, 0.05f, 0); 
                glRotatef(90, 1,0,0); 
                GlutTools.solidCylinder(0.1f, 0.05f, 12); 
            glPopMatrix();
                glPushMatrix(); 
                glTranslatef( 0.15f, 0.05f, 0); 
                glRotatef(90, 1,0,0); 
                GlutTools.solidCylinder(0.1f, 0.05f, 12); 
            glPopMatrix();
            
            // Dedo Izquierdo
            glPushMatrix(); 
                glTranslatef(-0.25f, 0, 0); 
                glRotatef(-apertura * 45, 0, 0, 1); 
                glTranslatef(0, 0.25f, 0); 
                GlutTools.solidBox(0.1f, 0.5f, 0.1f); 
                glTranslatef(0.1f, 0.2f, 0); 
                GlutTools.solidBox(0.2f, 0.1f, 0.1f); 
            glPopMatrix();

            // Dedo Derecho
            glPushMatrix(); 
                glTranslatef(0.25f, 0, 0); 
                glRotatef(apertura * 45, 0, 0, 1); 
                glTranslatef(0, 0.25f, 0); 
                GlutTools.solidBox(0.1f, 0.5f, 0.1f); 
                glTranslatef(-0.1f, 0.2f, 0); 
                GlutTools.solidBox(0.2f, 0.1f, 0.1f); 
            glPopMatrix();
        glPopMatrix();
    }
}