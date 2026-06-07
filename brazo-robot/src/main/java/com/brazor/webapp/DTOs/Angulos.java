package com.brazor.webapp.DTOs;

public class Angulos {
    private double base;
    private double shoulder;
    private double elbow;
    private double wrist1;
    private double wrist2;
    private double wrist3;

    public Angulos() {}

    // Getters
    public double getBase() {
        return base; 
    }
    public double getShoulder() {
        return shoulder;
    }
    public double getElbow() {
        return elbow; 
    }
    public double getWrist1() {
        return wrist1; 
    }
    public double getWrist2() {
        return wrist2; 
    }
    public double getWrist3() {
        return wrist3; 
    }

    // Setters
    public void setBase(double base) {
        this.base = base;
    }
    public void setShoulder(double shoulder) {
        this.shoulder = shoulder;
    }
    public void setElbow(double elbow) {
        this.elbow = elbow;
    }
    public void setWrist1(double wrist1) {
        this.wrist1 = wrist1;
    }
    public void setWrist2(double wrist2) {
        this.wrist2 = wrist2;
    }
    public void setWrist3(double wrist3) {
        this.wrist3 = wrist3;
    }
}