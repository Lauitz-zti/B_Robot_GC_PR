package com.brazor.webapp.Modelos;

public class Logger {
    private int idLog;
    private Integer idBrazo; // Puede ser null si el error es general de la app
    private String codigoError;
    private String mensaje;
    private java.sql.Timestamp fecha_error;

    public Logger() {}

    public int getIdLog() {
        return idLog;
    }

    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }

    public Integer getIdBrazo() {
        return idBrazo;
    }

    public void setIdBrazo(Integer idBrazo) {
        this.idBrazo = idBrazo;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public void setCodigoError(String codigoError) {
        this.codigoError = codigoError;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    public java.sql.Timestamp getFecha_error() {
        return fecha_error;
    }
    public void setFecha_error(java.sql.Timestamp fecha_error) {
        this.fecha_error = fecha_error;
    }

    // Getters y Setters...
}