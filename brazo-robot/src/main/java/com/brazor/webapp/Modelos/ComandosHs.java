package com.brazor.webapp.Modelos;

public class ComandosHs {
    private long idComando;
    private int idBrazo;
    private int idUsuario;
    private String comandoJson;
    private boolean ejecutadoEnFisico;
    private java.sql.Timestamp fechaUp;

    public ComandosHs() {}

    public long getIdComando() {
        return idComando;
    }

    public void setIdComando(long idComando) {
        this.idComando = idComando;
    }

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

    public String getComandoJson() {
        return comandoJson;
    }

    public void setComandoJson(String comandoJson) {
        this.comandoJson = comandoJson;
    }

    public boolean isEjecutadoEnFisico() {
        return ejecutadoEnFisico;
    }

    public void setEjecutadoEnFisico(boolean ejecutadoEnFisico) {
        this.ejecutadoEnFisico = ejecutadoEnFisico;
    }

    public java.sql.Timestamp getFechaUp() {
        return fechaUp;
    }

    public void setFechaUp(java.sql.Timestamp fechaUp) {
        this.fechaUp = fechaUp;
    }

}