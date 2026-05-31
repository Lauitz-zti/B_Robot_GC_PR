package com.brazor.webapp.Modelos;

public class BrazoRobot {
    private int idBrazo;
    private int idUsuario;
    private boolean esSimulado;
    private java.sql.Timestamp fechaCreacion;

    public BrazoRobot() {}

    public int getIdBrazo() {
        return idBrazo;
    }

    public void setIdBrazo(int idBrazo) {
        this.idBrazo = idBrazo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public boolean isEsSimulado() {
        return esSimulado;
    }

    public void setEsSimulado(boolean esSimulado) {
        this.esSimulado = esSimulado;
    }

    public java.sql.Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(java.sql.Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}