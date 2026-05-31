package com.brazor.webapp.Modelos;

public class CofiguracionArticulaciones {
    private int idConfig;
    private int idBrazo;
    private int articulacionNum;
    private float anguloMin;
    private float anguloMax;

    public CofiguracionArticulaciones() {}
    // Getters y Setters

    public int getIdConfig() {
        return idConfig;
    }

    public void setIdConfig(int idConfig) {
        this.idConfig = idConfig;
    }

    public int getIdBrazo() {
        return idBrazo;
    }

    public void setIdBrazo(int idBrazo) {
        this.idBrazo = idBrazo;
    }

    public int getArticulacionNum() {
        return articulacionNum;
    }

    public void setArticulacionNum(int articulacionNum) {
        this.articulacionNum = articulacionNum;
    }

    public float getAnguloMin() {
        return anguloMin;
    }

    public void setAnguloMin(float anguloMin) {
        this.anguloMin = anguloMin;
    }

    public float getAnguloMax() {
        return anguloMax;
    }

    public void setAnguloMax(float anguloMax) {
        this.anguloMax = anguloMax;
    }
}
