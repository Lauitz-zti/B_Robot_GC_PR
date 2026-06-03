package com.brazor.Comunicacion.WebServer;

import com.brazor.webapp.DAOs.BrazoDAO;
import com.brazor.webapp.DTOs.Estado;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class BrazoSocketHandler extends TextWebSocketHandler {

    // Lista sincronizada para llevar el control de todos los navegadores conectados
    private static final Set<WebSocketSession> sesionesActivas = Collections.synchronizedSet(new HashSet<>());
    
    private final BrazoDAO brazoDAO;
    private final ObjectMapper objectMapper;

    public BrazoSocketHandler(BrazoDAO brazoDAO) {
        this.brazoDAO = brazoDAO;
        this.objectMapper = new ObjectMapper();
    }

    //Cuando inicia la conexión con el navegador (equivalente al TCP que teniamos antes "Cliente conectado desde IP: ...")
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sesionesActivas.add(session);
        System.out.println("[WebSocket] Nuevo Gemelo Digital conectado. ID Sesion: " + session.getId());
    }

    //Cuando se cierra la conexion con el navegador (equivalente al TCP que teniamos antes "Cliente desconectado desde IP: ...")
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sesionesActivas.remove(session);
        System.out.println("[WebSocket] Gemelo Digital desconectado. ID Sesion: " + session.getId());
    }

    // Cuando recibimos un comando de movimiento (Equivalente a tu antiguo TCP "MOVE_BASE:1.5")
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // 1. Desempaquetamos el JSON usando tu Modelo estricto
            String payloadJson = message.getPayload();
            Estado payload = objectMapper.readValue(payloadJson, Estado.class);

            //gurdamos el nuevo estado del robot en la base de datos
            int idBrazo = payload.getId_brazo();
            String jsonAngulos = objectMapper.writeValueAsString(payload.getAngulos());
            brazoDAO.actualizarPosicion(idBrazo, jsonAngulos);

            //BROADCAST: (Equivalente a tu antiguo UdpSender)
            // Le avisamos a TODOS los demas que el robot se movio para que actualicen su pantalla 3D
            for (WebSocketSession sesionDestino : sesionesActivas) {
                // Filtramos para NO rebotarle el mensaje a quien lo origino
                if (sesionDestino.isOpen() && !sesionDestino.getId().equals(session.getId())) {
                    sesionDestino.sendMessage(new TextMessage(payloadJson));
                }
            }

        } catch (Exception e) {
            System.err.println("[WebSocket] Error procesando telemetría: " + e.getMessage());
        }
    }
}