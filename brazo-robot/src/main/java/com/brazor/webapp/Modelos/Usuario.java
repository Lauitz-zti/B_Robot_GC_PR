package com.brazor.webapp.Modelos;

public class Usuario {
    private int idUsuario;
    private String username;
    private String email;
    private String firebaseUid; // Este reemplaza al antiguo passwordHash
    private String rol;

    public Usuario() {}

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
        public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

        public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
        public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }
        public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}