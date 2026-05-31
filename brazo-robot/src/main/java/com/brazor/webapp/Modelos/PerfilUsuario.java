package com.brazor.webapp.Modelos;

public class PerfilUsuario {
    private int idPerfil;
    private int idUsuario;
    private String nombreCompleto;
    private String fotoUrl;
    private String temaInterfaz;
    private String bio;

    public PerfilUsuario() {}

    public int getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(int idPerfil) {
        this.idPerfil = idPerfil;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getTemaInterfaz() {
        return temaInterfaz;
    }

    public void setTemaInterfaz(String temaInterfaz) {
        this.temaInterfaz = temaInterfaz;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

}