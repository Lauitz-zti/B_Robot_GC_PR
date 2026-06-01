package com.brazor.Comunicacion.Server;

import java.net.*;

public class UdpSender {
    private DatagramSocket socket;
    private InetAddress broadcastAddress;
    private int port;

    public UdpSender(int port) {
        this.port = port;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            // Usamos la dirección especial "255.255.255.255" para enviarlo a TODOS en la red
            broadcastAddress = InetAddress.getByName("255.255.255.255");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcastState(String csvState) {
        try {
            byte[] buffer = csvState.getBytes(); //Cuando el estado cambie, enviamos el nuevo estado
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, port); //creamos el paquete UDP
            socket.send(packet); // Enviamos el paquete
            // System.out.println("[UDP] Estado enviado: " + csvState); // Descomentar para depurar
        } catch (Exception e) {
            System.err.println("[UDP] Error enviando: " + e.getMessage());
        }
    }
}