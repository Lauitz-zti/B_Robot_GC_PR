package com.brazor.Configuraciones;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    // Spring lee automaticamente la ruta del application.properties
    @Value("${firebase.credenciales.ruta}")
    private Resource rutaCredenciales;

    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        FirebaseOptions opciones = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(rutaCredenciales.getInputStream()))
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