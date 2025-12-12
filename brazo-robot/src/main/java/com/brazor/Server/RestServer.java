package com.brazor.Server;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;

public class RestServer {
    private int port;
    private RobotStateManager stateRobot;

    public RestServer(int port, RobotStateManager stateRobot) {
        this.port = port; //Puerto en el que escucha el servidor
        this.stateRobot = stateRobot;//Gestionamos del estado del robot
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);// "0.0.0.0" fuerza a escuchar en TODAS las tarjetas de red
        
        server.createContext("/api/move", this::MoveBr); //Recibe comandos de movimiento
        server.createContext("/api/control", this::ControlBR); //Solicia el control del brazo

        server.start();
        System.out.println("   [WEB] API REST escuchando en http://0.0.0.0:" + port + "/api/control");
    }

    private void sendResponse(HttpExchange ex, String resp) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        // Headers extra para evitar problemas en algunos navegadores moviles
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
        ex.sendResponseHeaders(200, resp.length());
        try (OutputStream os = ex.getResponseBody()) { os.write(resp.getBytes()); }
    }

    private void ControlBR(HttpExchange ex) throws IOException {
        String ip = ex.getRemoteAddress().getAddress().getHostAddress();//La peticion llego al servidor
        String res = stateRobot.processCommand("TAKE_CONTROL", ip); //Verificamos si puede tener el control
        sendResponse(ex, res);
    }

    private void MoveBr(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath(); //Recupera la ruta de la peticion
        String ip = ex.getRemoteAddress().getAddress().getHostAddress(); //extrae la IP del cliente

        try {
            String axis = path.split("&")[0].split("=")[1];
            String val = path.split("&")[1].split("=")[1];
            
            String res = stateRobot.processCommand("MOVE_" + axis.toUpperCase() + ":" + val, ip);
            sendResponse(ex, res);
        } catch(Exception e) { 
            System.err.println("Error procesando move: " + e.getMessage());
            sendResponse(ex, "ERROR"); 
        }
    }



}