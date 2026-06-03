package com.brazor.Comunicacion.WebServer;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final BrazoSocketHandler brazoSocketHandler;


    public WebSocketConfig(BrazoSocketHandler brazoSocketHandler) {
        this.brazoSocketHandler = brazoSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Exponemos la ruta del WebSocket para el frontend web
        registry.addHandler(brazoSocketHandler, "/ws/brazo")
                .setAllowedOrigins("*"); 
    }
}