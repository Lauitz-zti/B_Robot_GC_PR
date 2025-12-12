package com.brazor.Server;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) throws IOException {
        System.out.println("Iniciando Servidor...");

        //Inicializar bd
        DataBaseService db = new DataBaseService();
        db.init();

        // Iniciar el Gestor de Estado (Cerebro)
        RobotStateManager stateManager = new RobotStateManager(db);

        //Iniciar Servidor Sockets
        TcpServer tcpServer = new TcpServer(5000, stateManager); 
        tcpServer.start();
        
        //Iniciar Servidor Web (Puerto 8080)
        RestServer webServer = new RestServer(8080, stateManager);
        try {
            webServer.start();
            System.out.println("Servidor REST escuchando en puerto 8080");
        } catch (IOException e) {
            System.out.println("Error iniciando REST: " + e.getMessage());
        }
    }
}