package com.brazor.webapp.DTOs;

public class Angulos {
    private int base;
    private int hombro;
    private int codo;
    private int pitch;
    private int roll;
    private int yaw;

    public Angulos() {}

    // Getters
    public int getBase(){
        return base; 
    }
    public int getHombro(){
        return hombro;
    }
    public int getCodo() {
        return codo; 
    }
    public int getPitch(){
        return pitch; 
    }
    public int getRoll(){
        return roll;
    }
    public int getYaw() {
        return yaw; 
    }

    // Setters
    public void setBase(int base) {
        this.base = base;
    }
    public void setHombro(int hombro) {
        this.hombro = hombro; 
    }
    public void setCodo(int codo) {
        this.codo = codo; 
    }
    public void setPitch(int pitch) {
        this.pitch = pitch; 
    }
    public void setRoll(int roll) {
        this.roll = roll; 
    }
    public void setYaw(int yaw) {
        this.yaw = yaw;
    }
}