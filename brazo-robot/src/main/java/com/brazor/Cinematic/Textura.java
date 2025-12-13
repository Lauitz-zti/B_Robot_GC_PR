package com.brazor.Cinematic; // Asegúrate de que el package coincida

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;

public class Textura { 

    public static int loadTexture(String path) {
        BufferedImage image = null;
        
        // Busca dentro de la carpeta 'resources' la imagen
        try (InputStream is = Textura.class.getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("ERROR: No se encuentra: " + path);
                return 0;
            }
            image = ImageIO.read(is);
            
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        int ancho = image.getWidth();
        int alto = image.getHeight();
        int[] pixels = new int[ancho * alto];
        image.getRGB(0, 0, ancho, alto, pixels, 0, ancho);

        ByteBuffer buffer = BufferUtils.createByteBuffer(ancho * alto * 4);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int pixel = pixels[y * ancho + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();

        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, ancho, alto, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        return textureID;
    }
}