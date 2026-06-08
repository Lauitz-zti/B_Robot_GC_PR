package com.brazor.Comunicacion.WebServer;

import java.io.OutputStream;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service; //Crea un microservicio de red

import com.brazor.webapp.DAOs.PuenteHDAO;

@Service
public class RobotCliente {

    @Value("${ROBOT_HOST:192.168.100.251}")
    private String ipBrazo; 

    @Value("${ROBOT_PORT:30003}")
    private int puerto;

    private Socket socket; //conexion tcp
    private OutputStream out; //envio de datos
    
    // Variable para registrar la hora de los fallos y evitar saturar el hilo
    private long ultimoIntento = 0;

    private final PuenteHDAO puenteHDAO;

    public RobotCliente(PuenteHDAO puenteHDAO) {
        this.puenteHDAO = puenteHDAO;
    }

    public synchronized void conectar(int idBrazo) {
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

            puenteHDAO.registrarConexion(idBrazo, ipBrazo);
            
        } catch (Exception e) {
            //el robot no se encuentra conectado, seguimos en la simulacion (la web)
            System.out.println("[Modo Simulacion] Hardware offline. Siguiente chequeo de red en 5s.");
            socket = null;
            out = null;

            puenteHDAO.marcarDesconectado(idBrazo);
        }
    }

    /* traducimos los comandos de la aplicacioon web al lenguaje que entiende el robot,
    */
    public synchronized void enviarComando(int idBrazo, double[] angulos) {
        //evaluamos la conexion 
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            conectar(idBrazo); 
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

            puenteHDAO.marcarDesconectado(idBrazo);
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