package com.brazor.Configuraciones;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.brazor.Comunicacion.WebServer.BrazoSocketHandler;

@Configuration
@EnableWebSocket
public class ConfiSockets implements WebSocketConfigurer {

    private final BrazoSocketHandler brazoSocketHandler;

    public ConfiSockets(BrazoSocketHandler brazoSocketHandler) {
        this.brazoSocketHandler = brazoSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Exponemos la ruta de conexión para el frontend web
        registry.addHandler(brazoSocketHandler, "/ws/brazo")
                .setAllowedOrigins("*"); // Permite que tu HTML local se conecte sin bloqueos de seguridad
    }
}