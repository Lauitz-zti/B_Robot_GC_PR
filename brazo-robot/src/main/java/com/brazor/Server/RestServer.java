package com.brazor.Server;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;

public class RestServer {
    private int port;
    private RobotStateManager stateRobot;

    public RestServer(int port, RobotStateManager stateRobot) {
        this.port = port; //Puerto en el que escucha el servidor
        this.stateRobot = stateRobot; //Gestionamos del estado del robot
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/mover", this::MoveBR); //Recibe comandos de movimiento
        server.createContext("/permiso", this::ControlBR);  //Solicia el control del brazo
        server.start();
    }

    private void sendResponse(HttpExchange ex, String resp) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(200, resp.length());
        try (OutputStream os = ex.getResponseBody()) { 
            os.write(resp.getBytes()); 
        }
    }

    private void ControlBR(HttpExchange ex) throws IOException {
        String ip = ex.getRemoteAddress().getAddress().getHostAddress(); //La peticion llego al servidor
        String res = stateRobot.processCommand("Control", ip); //Verificamos si puede tener el control
        sendResponse(ex, res);
    }
    
    private void MoveBR(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath(); //Recupera la ruta de la peticion
        String ip = ex.getRemoteAddress().getAddress().getHostAddress(); //extrae la IP del cliente

        try {
            //Esto es para dividir la ruta en partes:
            // path = /mover/x/20  →  split → ["", "mover", "x", "20"]
            String[] parts = path.split("/");

            if (parts.length < 4) { //Esperamos al menos 4 partes de la ruta 
                sendResponse(ex, "ERROR URL");
                return;
            }
            String axis = parts[2];   // eje en el que se mueve el brazo
            String val  = parts[3];   // 20 quee seria el valor del movimiento
            String cmd = "Mover" + axis.toUpperCase() + ":" + val;
            String res = stateRobot.processCommand(cmd, ip);
            sendResponse(ex, res);
        } catch (Exception e) {
            sendResponse(ex, "ERROR");
        }
    }
}