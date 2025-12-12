package com.brazor.Cliente;

import java.net.*;
import com.brazor.GC.RobotRenderer;

public class UdpListener {
    private DatagramSocket socket;
    private RobotRenderer robot;
    private boolean running = true;

    public UdpListener(RobotRenderer robot, int port) {
        this.robot = robot;
        try {
            // Escuchar en el puerto 6000 (donde envía el servidor)
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void startListening() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            System.out.println(">>> CLIENTE ESCUCHANDO UDP EN PUERTO 6000 <<<");
            
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // Se bloquea aquí hasta recibir algo
                    
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    
                    // Actualizar el robot inmediatamente
                    String[] parts = msg.split(",");
                    if (parts.length >= 6) {
                        robot.base = Float.parseFloat(parts[0]);
                        robot.hombro = Float.parseFloat(parts[1]);
                        robot.codo = Float.parseFloat(parts[2]);
                        robot.muneca1 = Float.parseFloat(parts[3]);
                        robot.muneca2 = Float.parseFloat(parts[4]);
                        robot.pinza = Float.parseFloat(parts[5]);
                    }
                } catch (Exception e) {
                    if (running) e.printStackTrace();
                }
            }
        }).start();
    }

    public void close() {
        running = false;
        if (socket != null) socket.close();
    }
}