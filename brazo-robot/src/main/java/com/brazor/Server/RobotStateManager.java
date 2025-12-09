package com.brazor.Server;


public class RobotStateManager {
    // ESTADO DEL ROBOT
    private float base = 0, hombro = 45, codo = -45, m1 = 0, m2 = 0, pinza = 0.5f;
    
    //Concurrencia 
    private String clienteIPcont = null; //IP del cliente que actualmente tiene el control exclusivo
    private long cmdAccepTime = 0; //ultimo comando aceptado

    private DataBaseService db;//A donde va diirigir los datos
    public RobotStateManager(DataBaseService db) { //Almacena los dats para persistir el estado 
        this.db = db;
    }

    // Metodo sincronizado para evitar condiciones de carrera
    public synchronized String processCommand(String cmd, String ip) {
        // Timeout de 30 segundos
        if (System.currentTimeMillis() - cmdAccepTime > 30000) clienteIPcont = null;

        //Gestion del Token
        if (cmd.startsWith("Control")) {
            if (clienteIPcont == null || clienteIPcont.equals(ip)) {
                clienteIPcont = ip;
                cmdAccepTime = System.currentTimeMillis();
                return "Control aceptado";
            }
            return "Ocupado por: " + clienteIPcont;
        }

        //Asignacion del cliente si se encuentra libre
        if (clienteIPcont == null) clienteIPcont = ip;

        // Verificar permiso
        if (!clienteIPcont.equals(ip)) return "Accso denegado. Controlado por: " + clienteIPcont;

        cmdAccepTime = System.currentTimeMillis();

        //Procesar movimiento

        //Utilizamos lo que teniamos en 
        String[] parts = cmd.split(":");
        if (parts.length < 2) 
            return "error";
        
        float val = Float.parseFloat(parts[1]);
        boolean cambio = false;

        if (parts[0].equals("Mover_Base")){ 
            base = clamp(base + val, -90, 90); cambio = true; 
        }
        if (parts[0].equals("Mover_Hombro")) {
            hombro = clamp(hombro + val, -10, 135); cambio = true; 
        }
        if (parts[0].equals("Mover_Codo")){ 
            codo = clamp(codo + val, -120, 0); cambio = true; 
        }
        if (parts[0].equals("Mover_Mun1")){
            m1 = clamp(m1 + val, -90, 90); cambio = true; 
        }
        if (parts[0].equals("Mover_Mun2")){
            m2 = clamp(m2 + val, -45, 45); cambio = true; 
        }
        if (parts[0].equals("Mover_Pinza")){
            pinza = clamp(pinza + val, 0, 1); cambio = true; 
        }

        //Guardar en BD si hubo cambio
        if (cambio) {
            db.SaveEstados(base, hombro, codo, m1, m2, pinza);
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