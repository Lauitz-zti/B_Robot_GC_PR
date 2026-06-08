package com.brazor.Configuraciones;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credenciales.ruta}")
    private Resource rutaCredenciales;

    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        InputStream credencialesStream;
        
        // Buscamos la variable de entorno secreta en Railway
        String firebaseEnv = System.getenv("FIREBASE_CREDENTIALS");

        if (firebaseEnv != null && !firebaseEnv.trim().isEmpty()) {
            // 1. MODO NUBE (Railway): Convierte el texto JSON de la variable en un archivo virtual
            credencialesStream = new ByteArrayInputStream(firebaseEnv.getBytes(StandardCharsets.UTF_8));
            System.out.println("Cargando credenciales de Firebase desde variable de entorno (Railway)");
        } else {
            // 2. MODO LOCAL (Tu laptop): Usa el Resource de tu application.properties
            credencialesStream = rutaCredenciales.getInputStream();
            System.out.println("Cargando credenciales de Firebase desde archivo local");
        }

        FirebaseOptions opciones = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(credencialesStream))
                .build();

        // Inicializamos la app solo si no existe una previa
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(opciones);
            System.out.println("Firebase inicializado via Spring Bean");
        }

        // Retornamos la instancia de Autenticación lista para usarse
        return FirebaseAuth.getInstance();
    }
}