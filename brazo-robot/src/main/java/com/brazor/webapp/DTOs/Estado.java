package com.brazor.webapp.DTOs;

import java.util.List;

public class Estado {
    private String tipo; // "MANUAL" o "MACRO"
    private int id_brazo;
    private Angulos angulos; // Se llena si es MANUAL
    private List<Angulos> secuencia; // Se llena si es MACRO

    public Estado() {}

    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getId_brazo() {
        return id_brazo;
    }
    public void setId_brazo(int id_brazo) {
        this.id_brazo = id_brazo;
    }

    public Angulos getAngulos() {
        return angulos;
    }
    public void setAngulos(Angulos angulos) {
        this.angulos = angulos;
    }

    public List<Angulos> getSecuencia() {
        return secuencia;
    }
    public void setSecuencia(List<Angulos> secuencia) {
        this.secuencia = secuencia;
    }
}