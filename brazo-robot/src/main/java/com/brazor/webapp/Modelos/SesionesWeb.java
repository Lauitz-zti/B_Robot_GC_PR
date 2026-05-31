package com.brazor.webapp.Modelos;
import java.util.UUID;
import java.sql.Timestamp;


public class SesionesWeb {
    private UUID idSesion;
    private int idUsuario;
    private String tokenAuth;
    private Timestamp fechaExpiracion;

    public SesionesWeb() {}

    public UUID getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(UUID idSesion) {
        this.idSesion = idSesion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getTokenAuth() {
        return tokenAuth;
    }

    public void setTokenAuth(String tokenAuth) {
        this.tokenAuth = tokenAuth;
    }

    public Timestamp getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(Timestamp fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

}