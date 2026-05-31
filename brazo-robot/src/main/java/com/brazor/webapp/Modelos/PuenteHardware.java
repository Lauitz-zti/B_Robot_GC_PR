package com.brazor.webapp.Modelos;

public class PuenteHardware {
    private int idPuente;
    private int idBrazo;
    private String ipMaquinaJava;
    private boolean estadoConexion;
    private int latenciaMs;
    private java.sql.Timestamp lastPing;

    public PuenteHardware() {}

    public int getIdPuente() {
        return idPuente;
    }

    public void setIdPuente(int idPuente) {
        this.idPuente = idPuente;
    }

    public int getIdBrazo() {
        return idBrazo;
    }

    public void setIdBrazo(int idBrazo) {
        this.idBrazo = idBrazo;
    }

    public String getIpMaquinaJava() {
        return ipMaquinaJava;
    }

    public void setIpMaquinaJava(String ipMaquinaJava) {
        this.ipMaquinaJava = ipMaquinaJava;
    }

    public boolean isEstadoConexion() {
        return estadoConexion;
    }

    public void setEstadoConexion(boolean estadoConexion) {
        this.estadoConexion = estadoConexion;
    }

    public int getLatenciaMs() {
        return latenciaMs;
    }

    public void setLatenciaMs(int latenciaMs) {
        this.latenciaMs = latenciaMs;
    }

    public java.sql.Timestamp getLastPing() {
        return lastPing;
    }

    public void setLastPing(java.sql.Timestamp lastPing) {
        this.lastPing = lastPing;
    }
}
