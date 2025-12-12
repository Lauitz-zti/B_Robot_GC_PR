package com.brazor.Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class TcpServer {
    private int port;
    private RobotStateManager stateManager;

    public TcpServer(int port, RobotStateManager stateManager) {
        this.port = port;
        this.stateManager = stateManager;
    }

     //Comunicacion con un solo cliente
    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            String line;
            String ip = socket.getInetAddress().getHostAddress(); //obtener IP del cliente
            
            while ((line = in.readLine()) != null) {//Lee linea por linea
                //Logica de procesamiento de angulos del brazo 
                stateManager.processCommand(line, ip);
                out.println(stateManager.getCsvState());// Respondemos con el estado actual
            }
        } catch (IOException e) {
            //Cliente desconectado
        }
    }

    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Servidor Socket escuchando en puerto " + port); //PRUEBAS DE DEPURACION jeje :)
                ExecutorService pool = Executors.newCachedThreadPool(); //Cada cliente se maneja en un hilo independiente.

                while (true) {
                    Socket client = serverSocket.accept();//se queda esperando hasta que un cliente se conecta
                    pool.execute(() -> handleClient(client));//Cuando un cliente se conecta
                                                            //  se delega el manejo al metodo handleClient, ejecutado en un hilo del pool
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}