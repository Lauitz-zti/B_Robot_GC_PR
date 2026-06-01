package com.brazor.Comunicacion.Cliente;

import java.io.*;
import java.net.Socket;

import com.brazor.Graficos.GC.RobotRenderer;

//Unicamente se encarga de conectar, escuchar al servidor y actualizar los datos del robot
public class NetworkClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private RobotRenderer robotTarget; // Referencia al robot para actualizarlo
    private boolean connected = false;

    public NetworkClient(RobotRenderer robotTarget) {
        this.robotTarget = robotTarget;
    }

    public void ConClientServer(String host, int port) {
        try {
            socket = new Socket(host, port); //Conexion tcp al servidor
            out = new PrintWriter(socket.getOutputStream(), true); //enviar comandos de linea al servidor
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //recibir datos del servidor
            connected = true;
            System.out.println("Conectando al servidor (" + host + ":" + port + ")...");

            // Iniciar hilo de escucha
            new Thread(this::listenLoop).start();
        } catch (IOException e) {
            System.err.println("!!!ERROR DE RED: No se pudo conectar al servidor.");
            System.err.println("ServerMain esta corriendo?"); //Purbas de depuracion :)
        }
    }

    public void sendCommand(String cmd) {
        if (connected && out != null) {
            out.println(cmd);
        }
    }

    public void close() {
        try {
            connected = false;
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenLoop() {
        try {
            String line; //posiciones recibidas del servidor del robot
            while (connected && (line = in.readLine()) != null) {
                // Protocolo: "base,hombro,codo,muneca1,muneca2,pinza"
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    // Actualizamos el robot directamente
                    robotTarget.base = Float.parseFloat(parts[0]);
                    robotTarget.hombro = Float.parseFloat(parts[1]);
                    robotTarget.codo = Float.parseFloat(parts[2]);
                    robotTarget.muneca1 = Float.parseFloat(parts[3]);
                    robotTarget.muneca2 = Float.parseFloat(parts[4]);
                    robotTarget.pinza = Float.parseFloat(parts[5]);
                }
            }
        } catch (Exception e) {
            if (connected) System.out.println("Desconectado del servidor.");
        }
    }
}

/*
ARQUITECTURA DEL CLIENTE DE RED
--------------------------------
NetworkClient → Conecta → TcpServer (RobotStateManager)
       ↓                           ↓
Actualiza RobotRenderer ← Recibe estados CSV
 */