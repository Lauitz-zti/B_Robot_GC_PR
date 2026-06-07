package com.brazor.webapp.Modelos;

import java.sql.Timestamp;

public class RecorridosSave {
    private int idRecorrido;
    private int idBrazo;
    private String nombreEstudio;
    private String secuenciaJsonb; // Se almacena como String para mapear el JSONB de Postgres
    private Timestamp fechaCreacion;

    public RecorridosSave() {}

    public int getIdRecorrido() {
        return idRecorrido;
    }

    public void setIdRecorrido(int idRecorrido) {
        this.idRecorrido = idRecorrido;
    }

    public int getIdBrazo() {
        return idBrazo;
    }

    public void setIdBrazo(int idBrazo) {
        this.idBrazo = idBrazo;
    }

    public String getNombreEstudio() {
        return nombreEstudio;
    }

    public void setNombreEstudio(String nombreEstudio) {
        this.nombreEstudio = nombreEstudio;
    }

    public String getSecuenciaJsonb() {
        return secuenciaJsonb;
    }

    public void setSecuenciaJsonb(String secuenciaJsonb) {
        this.secuenciaJsonb = secuenciaJsonb;
    }

    public java.sql.Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(java.sql.Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}