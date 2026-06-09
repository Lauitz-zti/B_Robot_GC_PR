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

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class BrazoSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sesionesActivas = Collections.synchronizedSet(new HashSet<>());

    private static final Map<Integer, String> brazoOcupado = new ConcurrentHashMap<>(); //para la concurrencia 

    private final BrazoDAO brazoDAO;
    private final ObjectMapper objectMapper;
    private final FirebaseAuth firebaseAuth;
    private final RobotCliente robotCliente;

    public BrazoSocketHandler(BrazoDAO brazoDAO, FirebaseAuth firebaseAuth, RobotCliente robotCliente) {
        this.brazoDAO = brazoDAO;
        this.objectMapper = new ObjectMapper();
        this.firebaseAuth = firebaseAuth;
        this.robotCliente = robotCliente;
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
            System.err.println("[Seguridad] Conexion WebSocket rechazada: " + e.getMessage());
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sesionesActivas.remove(session);

        //Si un usuario se desconecta, buscamos que brazo tenia y lo liberamos
        brazoOcupado.entrySet().removeIf(entry -> entry.getValue().equals(session.getId())); 
        System.out.println("[WebSocket] Gemelo Digital desconectado. ID Sesion: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payloadJson = message.getPayload();
            Estado payload = objectMapper.readValue(payloadJson, Estado.class);
            int idBrazo = payload.getId_brazo();

        //Verificacion inicial de red al cargar la pagina
            if ("INIT".equals(payload.getTipo())) {

                if (brazoOcupado.containsKey(idBrazo) && !brazoOcupado.get(idBrazo).equals(session.getId())){
                    System.out.println();

                    session.sendMessage(new TextMessage("{\"tipo\":\"ERROR\", \"mensaje\":\"OCUPADO\"}"));
                    session.close(CloseStatus.NOT_ACCEPTABLE);
                    return;    
                }
                brazoOcupado.put(idBrazo, session.getId());//Si el ur esta libre, damos acceso y bloqueamos acceso a otras sesiones
                
                try {
                    // Intentamos conectar con el hardware fisico
                    robotCliente.conectar(idBrazo);
                    
                    System.out.println("[Hardware] Robot UR3e detectado. Ejecutando postura HOME.");
                    
                    double[] homeFisico = {0.0, -90.0, 90.0, -90.0, -90.0, 0.0}; //establecemos la posicion segura en la visualizacion
                    robotCliente.enviarComando(idBrazo, homeFisico);
                    
                    Angulos angulosHome = new Angulos();
                    angulosHome.setBase(homeFisico[0]);
                    angulosHome.setShoulder(homeFisico[1]);
                    angulosHome.setElbow(homeFisico[2]);
                    angulosHome.setWrist1(homeFisico[3]);
                    angulosHome.setWrist2(homeFisico[4]);
                    angulosHome.setWrist3(homeFisico[5]);
                    
                    String jsonHome = objectMapper.writeValueAsString(angulosHome);// Guardamos la postura HOME en la base de datos
                    brazoDAO.actualizarPosicion(idBrazo, jsonHome);
                    
                    //Forzamos a la web a actualizar sus sliders y el modelo 3D
                    Estado estadoHome = new Estado();
                    estadoHome.setTipo("MANUAL"); 
                    estadoHome.setId_brazo(idBrazo);
                    estadoHome.setAngulos(angulosHome);
                    
                    String payloadHome = objectMapper.writeValueAsString(estadoHome);
                    session.sendMessage(new TextMessage(payloadHome)); 
                    broadcast(session, payloadHome);

                } catch (Exception ex) {
                    System.out.println("[Modo Simulacion] Hardware offline: " + ex.getMessage());
                }
            }
            //MOVIMIENTO MANUAL PUNTO A PUNTO
            else if ("MANUAL".equals(payload.getTipo())) {

                if(!session.getId().equals(brazoOcupado.get(idBrazo))){
                    return;
                }
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
                        robotCliente.enviarComando(idBrazo,new double[]{b, s, e, w1, w2, w3});
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
                            robotCliente.enviarComando(idBrazo,angulosArray);

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