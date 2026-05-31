package com.brazor.webapp.Modelos;

public class EstadoActual {
    private int idEstado;
    private int idBrazo;
    private String angulosJson; //Guardamos los angulos en formato JSON: {"base":0, "codo":45...}
    private String fuente;      // 'WEB' o 'HARDWARE' para saber de donde viene el estado, simulado o un brazo real
    private java.sql.Timestamp fechaUpdate;

    public EstadoActual() {}

    public int getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(int idEstado) {
        this.idEstado = idEstado;
    }

    public int getIdBrazo() {
        return idBrazo;
    }

    public void setIdBrazo(int idBrazo) {
        this.idBrazo = idBrazo;
    }

    public String getAngulosJson() {
        return angulosJson;
    }

    public void setAngulosJson(String angulosJson) {
        this.angulosJson = angulosJson;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public java.sql.Timestamp getFechaUpdate() {
        return fechaUpdate;
    }

    public void setFechaUpdate(java.sql.Timestamp fechaUpdate) {
        this.fechaUpdate = fechaUpdate;
    }

}