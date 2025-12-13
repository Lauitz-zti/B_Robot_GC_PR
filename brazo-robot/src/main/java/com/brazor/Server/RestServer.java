package com.brazor.Server;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;

public class RestServer {
    private int port;
    private RobotStateManager stateRobot; 

    public RestServer(int port, RobotStateManager stateRobot) {
        this.port = port; //Puerto en el que escucha el servidor
        this.stateRobot = stateRobot; //Gestionamos del estado del robot
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);// "0.0.0.0" permite conexiones externas (WiFi)
        
        server.createContext("/api/move", this::handleMove); //Recibe comandos de movimiento
        server.createContext("/api/control", this::handleControl); //Solicita control del robot
        
        server.start();
        System.out.println("[WEB] API REST escuchando en http://0.0.0.0:" + port + "/api/control");
    }

    private void sendResponse(HttpExchange ex, String resp, int code) throws IOException {
        byte[] bytes = resp.getBytes("UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    // Configura las cabeceras para que el navegador del celular no bloquee la peticion
    private void handleCORS(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    //ENDPOINT: /api/move (Requiere PUT) ---
    private void handleMove(HttpExchange ex) throws IOException {
        handleCORS(ex); // Permitir conexion desde otros dispositivos
        
        //Responder al "Saludo" de seguridad del navegador
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return;
        }
        //Solo aceptamos PUT para "Actualizar" posicion
        if (!ex.getRequestMethod().equalsIgnoreCase("PUT")) {
            System.err.println("Web Error: Metodo incorrecto. Se esperaba PUT, llego " + ex.getRequestMethod());
            sendResponse(ex, "ERROR: METODO DEBE SER PUT", 405); // 405 Method Not Allowed
            return;
        }

        String ip = ex.getRemoteAddress().getAddress().getHostAddress();
        URI uri = ex.getRequestURI();
        String query = uri.getQuery(); 

        try {
            if (query == null || query.isEmpty()) throw new Exception("Query vacía");

            //Evitar errores en el index
            String axis = null;
            String val = null;
            
            String[] pares = query.split("&");
            for (String par : pares) {
                // Separar por el primer '=' encontrado
                int idx = par.indexOf("=");
                if (idx > 0) {
                    String clave = par.substring(0, idx).trim();
                    String valor = par.substring(idx + 1).trim();
                    
                    if (clave.equalsIgnoreCase("axis")) axis = valor;
                    if (clave.equalsIgnoreCase("val")) val = valor;
                }
            }

            if (axis == null || val == null) {
                sendResponse(ex, "ERROR_MISSING_PARAMS", 400);
                return;
            }

            //Procesar comando de movimiento
            String res = stateRobot.processCommand("MOVE_" + axis.toUpperCase() + ":" + val, ip);
            sendResponse(ex, res, 200);

        } catch(Exception e) { 
            System.err.println("Web Error: " + e.getMessage());
            sendResponse(ex, "ERROR_SERVER", 500); 
        }
    }

    // /api/control (Requiere POST)
    private void handleControl(HttpExchange ex) throws IOException {
        handleCORS(ex);
        
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            ex.sendResponseHeaders(204, -1);
            return;
        }

        String ip = ex.getRemoteAddress().getAddress().getHostAddress();//La peticion llego al servidor
        String method = ex.getRequestMethod();
        
        //CASO 1: POST (SOLICITAR CONTROL)
        if (method.equalsIgnoreCase("POST")) {
            System.out.println("Web-Control pedido por: " + ip);
            String res = stateRobot.processCommand("TAKE_CONTROL", ip); // Verificamos si puede tener el control

            //CAMBIO DE PANTALLA DE CARGA
            // Si el control fue concedido, gritamos por UDP a la PC
            if (res.equals("CONTROL_GRANTED") || res.equals("DEMO_STARTED")) {
                //System.out.println("ACCESO CONCEDIDO A " + ip); //depuracion en consola
                stateRobot.broadcastEvent("SESSION_START"); 
            }
            sendResponse(ex, res, 200);
        }

        //CASO 2: DELETE (SOLTAR CONTROL)
        else if (method.equalsIgnoreCase("DELETE")) {
            System.out.println("   [WEB] DELETE - Control liberado por: " + ip);
            String res = stateRobot.processCommand("RELEASE_CONTROL", ip);
            sendResponse(ex, res, 200);
        }

        //CASO 3: ERROR
        else {
            sendResponse(ex, "ERROR: METODO DEBE SER POST O DELETE", 405);
        }
    }

}