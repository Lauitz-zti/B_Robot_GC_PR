package com.brazor.Comunicacion.WebServer;

import com.brazor.webapp.DAOs.BrazoDAO;
import com.brazor.webapp.DTOs.Angulos;
import com.brazor.webapp.DTOs.Estado;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;

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

    private static final Set<WebSocketSession> sesionesActivas = Collections.synchronizedSet(new HashSet<>());

    private final BrazoDAO brazoDAO;
    private final ObjectMapper objectMapper;
    private final FirebaseAuth firebaseAuth;
    private final RobotCliente robotCliente;

    public BrazoSocketHandler(BrazoDAO brazoDAO, FirebaseAuth firebaseAuth, RobotCliente robotCliente) {
        this.brazoDAO = brazoDAO;
        this.objectMapper = new ObjectMapper();
        this.firebaseAuth = firebaseAuth;
        this.robotCliente = robotCliente;
        this.robotCliente.conectar();
    }


    //Valida las sesiones activas 
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        String tokenRecibido = null;

        if (query != null && query.contains("token=")) {
            tokenRecibido = query.split("token=")[1];
        }

        try {
            if (tokenRecibido == null) {
                throw new Exception("Sin token en la URL.");
            }
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(tokenRecibido);
            sesionesActivas.add(session);
            System.out.println("[WebSocket] Gemelo Digital autorizado para: " + decodedToken.getEmail());

        } catch (Exception e) {
            System.err.println("[Seguridad] Conexión WebSocket rechazada: " + e.getMessage());
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sesionesActivas.remove(session);
        System.out.println("[WebSocket] Gemelo Digital desconectado. ID Sesion: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payloadJson = message.getPayload();
            Estado payload = objectMapper.readValue(payloadJson, Estado.class);
            int idBrazo = payload.getId_brazo();

            //MOVIMIENTO MANUAL PUNTO A PUNTO
            if ("MANUAL".equals(payload.getTipo())) {
                double b  = payload.getAngulos().getBase();
                double s  = payload.getAngulos().getShoulder();
                double e  = payload.getAngulos().getElbow();
                double w1 = payload.getAngulos().getWrist1();
                double w2 = payload.getAngulos().getWrist2();
                double w3 = payload.getAngulos().getWrist3();

                if (esMovimientoSeguro(b, s, e)) {
                    //Guarda la posicion en la que se encientre el brazo en la bd
                    brazoDAO.actualizarPosicion(idBrazo, objectMapper.writeValueAsString(payload.getAngulos()));
                    
                    // Verificamos si hay un rboto conectado
                    try {
                        robotCliente.enviarComando(new double[]{b, s, e, w1, w2, w3});
                    } catch (Exception ex) {
                        // Si no hay robot, solo avisamos que continuamos en modo simulacion 
                        System.out.println("[Modo Simulacion] Movimiento virtual guardado. Hardware offline: " + ex.getMessage());
                    }

                    //Sincronixar en tiempo real la web
                    broadcast(session, payloadJson);
                } else {
                    System.err.println("[Seguridad] Movimiento bloqueado. Hombro=" + s + ", Codo=" + e);
                }
            }

            //TRAYECTORIA COMPLETA
            else if ("MACRO".equals(payload.getTipo())) {
                /*cada paso de la secuencia se broadcast
                individualmente con su ángulo real, sincronizando las
                pantallas 3D en tiempo real durante toda la trayectoria.*/
                new Thread(() -> {
                    try {
                        for (Angulos paso : payload.getSecuencia()) {
                            double[] angulosArray = {
                                paso.getBase(), paso.getShoulder(), paso.getElbow(),
                                paso.getWrist1(), paso.getWrist2(), paso.getWrist3()
                            };
                            robotCliente.enviarComando(angulosArray);

                            // Construir el JSON de este paso para sincronizar otras pantallas
                            Estado estadoPaso = new Estado();
                            estadoPaso.setTipo("MANUAL"); //Para mandar a llamar lo que guardamos en la bd es MANUAL 
                            estadoPaso.setId_brazo(idBrazo);
                            estadoPaso.setAngulos(paso);
                            String pasojson = objectMapper.writeValueAsString(estadoPaso);
                            broadcast(session, pasojson);

                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } catch (Exception ex) {
                        System.err.println("[WebSocket] Error durante ejecucion MACRO: " + ex.getMessage());
                    }
                }).start();
            }

        } catch (Exception e) {
            System.err.println("[WebSocket] Error procesando telemetría: " + e.getMessage());
        }
    }

//METODS AUXILIARES 
    private void broadcast(WebSocketSession origen, String json) {
        synchronized (sesionesActivas) {
            for (WebSocketSession destino : sesionesActivas) {
                try {
                    if (destino.isOpen() && !destino.getId().equals(origen.getId())) {
                        destino.sendMessage(new TextMessage(json));
                    }
                } catch (Exception e) {
                    System.err.println("[WebSocket] Error en broadcast a sesión " + destino.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    private boolean esMovimientoSeguro(double base, double shoulder, double elbow) {
        if (shoulder < -180.0 || shoulder > 0.0) 
            return false;
        if (elbow < -150.0 || elbow > 150.0) 
            return false;
        return true;
    }
}