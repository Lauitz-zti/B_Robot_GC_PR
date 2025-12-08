package com.brazor.Cliente;

import static org.lwjgl.opengl.GL11.*;
import com.brazor.Modelado.GlutTools;
import com.brazor.Modelado.RobotParts;

public class RobotRenderer {
    
    public float base, hombro, codo, muneca1, muneca2, pinza; //Variables en la bd actualizables via red 
    /* Estado del robot ya no los utilizaremos ahora estan por defecto en el servidor 
    y se actualizan via red en la bd
    private float base = 0, hombro = 45, codo = -45;
    private float muneca1 = 0, muneca2 = 0;
    private float pinzaApertura = 0.5f;*/ 

    public void draw() {
        glPushMatrix();
            glTranslatef(0, 0.05f, 0); 
            
            // Base giratoria
            RobotParts.materialAzul(); 
            glRotatef(base, 0, 1, 0); 
            GlutTools.solidCylinder(0.6f, 0.2f, 8); 

            //Articulaion Base
            glTranslatef(0, 0.15f, 0);
            RobotParts.dibujarServo();
            // Bracket Base
            glPushMatrix(); 
                glTranslatef(0, 0.35f, 0);
                glRotatef(180, 1, 0, 0);
                RobotParts.dibujarBracketU(0.5f, 0.5f, 0.4f);
            glPopMatrix(); 

            //Articulacion Hombro
            glTranslatef(0, 0.55f, 0);
            glRotatef(hombro, 1, 0, 0); // Movimiento

            glPushMatrix();
                glTranslatef(0, 0.2f, 0);
                glRotatef(-90, 0, 0, 1);
                RobotParts.dibujarServo();
            glPopMatrix();

            // Brazo Largo
            glPushMatrix();
                glTranslatef(0, 0.7f, 0);
                glScalef(1.0f, 2.5f, 1.0f); 
                RobotParts.dibujarBracketU(0.45f, 0.4f, 0.4f);
            glPopMatrix();

            //Articulacion Codo
            glTranslatef(0, 1.2f, 0);
            glRotatef(codo, 1, 0, 0); // Movimiento

            glPushMatrix();
                glRotatef(-90, 0, 0, 1);
                RobotParts.dibujarServo();
            glPopMatrix();

            // Antebrazo
            glPushMatrix();
                glTranslatef(0, 0.4f, 0);
                glScalef(1.0f, 1.5f, 1.0f);
                RobotParts.dibujarBracketU(0.4f, 0.4f, 0.4f);
            glPopMatrix();

            //Articulacion Muñeca
            glTranslatef(0, 0.75f, 0);
            RobotParts.dibujarServo();

            glTranslatef(0, 0.25f, 0);
            glRotatef(muneca1, 0, 1, 0); // Giro
            glTranslatef(0, -0.25f, 0);

            //Pinza
            glTranslatef(0, 0.15f, 0);
            glRotatef(muneca2, 1, 0, 0); // Flexion
            
            RobotParts.dibujarPinza(pinza);

        glPopMatrix();
    }
    /*Sigue en prueba, posiblemente migremos esto al cliente para control local
    public void input(long window) {
        float s = 1.5f; 
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) 
            base = clamp(base + s, -180, 180);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) 
            base = clamp(base - s, -180, 180);
        
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) 
            hombro = clamp(hombro + s, -100, 100);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) 
            hombro = clamp(hombro - s, -100, 100);
        
        if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) 
            codo = clamp(codo + s, -120, 0);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) 
            codo = clamp(codo - s, -120, 0);
        
        if (glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS) 
            muneca1 = clamp(muneca1 + s, -90, 90);
        if (glfwGetKey(window, GLFW_KEY_F) == GLFW_PRESS) 
            muneca1 = clamp(muneca1 - s, -90, 90);
        
        if (glfwGetKey(window, GLFW_KEY_T) == GLFW_PRESS) 
            muneca2 = clamp(muneca2 + s, -45, 45);
        if (glfwGetKey(window, GLFW_KEY_G) == GLFW_PRESS) 
            muneca2 = clamp(muneca2 - s, -45, 45);
        
        if (glfwGetKey(window, GLFW_KEY_Y) == GLFW_PRESS) 
            pinzaApertura = clamp(pinzaApertura + 0.02f, 0f, 1f);
        if (glfwGetKey(window, GLFW_KEY_H) == GLFW_PRESS) 
            pinzaApertura = clamp(pinzaApertura - 0.02f, 0f, 1f);
    }
    
    private float clamp(float v, float min, float max) {
        if (v < min) 
            return min; 
        if (v > max) return max; 
        return v;
    }*/
}