package com.brazor.Server;

public class RobotStateManager {
    // ESTADO DEL ROBOT
    private float base = 0, hombro = 45, codo = -45, m1 = 0, m2 = 0, pinza = 0.5f;
    
    // CONTROL DE CONCURRENCIA
    private String clienteIPcont = null; //IP del cliente que actualmente tiene el control exclusivo
    private long cmdAccepTime = 0;//ultimo comando aceptado
    
    private DataBaseService db; //A donde va diirigir los datos
    private UdpSender udp; // Nuevo

    public RobotStateManager(DataBaseService db, UdpSender udp) { //Almacena los dats para persistir el estado
        this.db = db;
        this.udp = udp;
    }

    public void broadcastEvent(String msg) {
        if (udp != null) {
            udp.broadcastState(msg); // Reutilizamos el sender UDP para avisar que alguien se conecto
        }
    }

    // Metodo sincronizado para evitar condiciones de carrera
    public synchronized String processCommand(String cmd, String ip) {
        // Timeout de 30 segundos
        if (System.currentTimeMillis() - cmdAccepTime > 30000) clienteIPcont = null;

        //ACTUAIZACION DEL ESTADO DEL ROBOT 
        if (cmd.equals("PING")) {
        return getCsvState(); // Solo devuelve el estado, no valida tokens ni mueve nada
    }

        //Gestion del Token (control exclusivo)
        if (cmd.startsWith("TAKE_CONTROL")) {
            if (clienteIPcont == null || clienteIPcont.equals(ip)) {
                clienteIPcont = ip;
                cmdAccepTime = System.currentTimeMillis();
                return "CONTROL_GRANTED";
            }
            return "BUSY_BY_" + clienteIPcont;
        }

        //Asignacion del cliente si se encuentra libre
        if (clienteIPcont == null) clienteIPcont = ip;

        // Verificar permiso
        if (!clienteIPcont.equals(ip)) return "ACCESS_DENIED";

        cmdAccepTime = System.currentTimeMillis();

        //Procesar movimiento
        //Utilizamos lo que teniamos en 
        String[] parts = cmd.split(":");
        if (parts.length < 2) return "BAD_FORMAT";
        
        float val = Float.parseFloat(parts[1]);
        boolean cambio = false;

        if (parts[0].equals("MOVE_BASE")){
             base = clamp(base + val, -90, 90); cambio = true; 
            }
        if (parts[0].equals("MOVE_HOMBRO")){ 
            hombro = clamp(hombro + val, -10, 135); cambio = true; 
        }
        if (parts[0].equals("MOVE_CODO")){
            codo = clamp(codo + val, -120, 0); cambio = true; 
            }
        if (parts[0].equals("MOVE_MUN1")){
            m1 = clamp(m1 + val, -90, 90); cambio = true; 
        }
        if (parts[0].equals("MOVE_MUN2")){
            m2 = clamp(m2 + val, -45, 45); cambio = true;
        }
        if (parts[0].equals("MOVE_PINZA")){
            pinza = clamp(pinza + val, 0, 1); cambio = true;
        }

        //Guardar en BD si hubo cambio
        if (cambio) {
            db.saveState(base, hombro, codo, m1, m2, pinza, ip);

            //Notificar por UDP (Tiempo Real - Requisito Faltante)
            udp.broadcastState(getCsvState());
        }

        return getCsvState();
    }

    public synchronized String getCsvState() {
        return base + "," + hombro + "," + codo + "," + m1 + "," + m2 + "," + pinza;
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(v, max));
    }
}