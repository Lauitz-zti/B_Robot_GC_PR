package com.brazor.Comunicacion.WebServer;

import java.io.OutputStream;
import java.net.Socket;
import org.springframework.stereotype.Service; //Crea un microservicio de red

@Service
public class RobotCliente {

    private final String ipBrazo = "192.168.100.251"; //ip del rob ur3e 
    private final int puerto = 30003;  // puerto para enviar comandos de movimiento directamente al controlador

    private Socket socket; //conexion tcp
    private OutputStream out; //envio de datos
    
    // Variable para registrar la hora de los fallos y evitar saturar el hilo
    private long ultimoIntento = 0;

    public synchronized void conectar() {
        try {
            // Si y tenemos un socket abierto, retornamos
            if (socket != null && !socket.isClosed()) 
                {
                return; 
            }
            
            //COOLDOWN: Si fallamos hace menos de 5 segundos, abortamos al instante.
            if (System.currentTimeMillis() - ultimoIntento < 5000) {
                return;
            }
            
            ultimoIntento = System.currentTimeMillis(); 
            
            socket = new Socket();
            //Si el robot esta apagado o desconectado, el intento de conexion, 2 segundos en fallar
            socket.connect(new java.net.InetSocketAddress(ipBrazo, puerto), 2000); // 2 segundos de timeout
            
            out = socket.getOutputStream();
            System.out.println("[RobotCliente] Conexion establecida con el brazo UR3e en " + ipBrazo + ":" + puerto);
            
        } catch (Exception e) {
            //el robot no se encuentra conectado, seguimos en la simulacion (la web)
            System.out.println("[Modo Simulacion] Hardware offline. Siguiente chequeo de red en 5s.");
            socket = null;
            out = null;
        }
    }

    /* traducimos los comandos de la aplicacioon web al lenguaje que entiende el robot,
    */
    public synchronized void enviarComando(double[] angulos) {
        //evaluamos la conexion 
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            conectar(); 
        }
        //Si seguimo sin conexion al ur3e cancelamos la operacion 
        if (socket == null || !socket.isConnected()) {
            return; 
        }

        //Logica matematica para convertir angulos a radianes (que son lo que utilizan en robotica)
        try {
            String comando = String.format("movej([%f,%f,%f,%f,%f,%f], a=1.2, v=0.25)\n",
                Math.toRadians(angulos[0]), Math.toRadians(angulos[1]),
                Math.toRadians(angulos[2]), Math.toRadians(angulos[3]),
                Math.toRadians(angulos[4]), Math.toRadians(angulos[5])
            );

            out.write(comando.getBytes());
            out.flush();

        } catch (Exception e) {
            System.err.println("[RobotCliente] Error enviando comando: " + e.getMessage());
            try { if (socket != null) socket.close(); } catch (Exception ignored) {}
            socket = null;
            out = null;
        }
    }

    public synchronized void desconectar() {
        try {
            if (socket != null) {
                socket.close();
                System.out.println("[RobotCliente] Conexión cerrada correctamente.");
            }
        } catch (Exception e) {
            System.err.println("[RobotCliente] Error al cerrar conexion: " + e.getMessage());
        } finally {
            socket = null;
            out = null;
        }
    }
}