package com.brazor.webapp.DAOs;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class PerfilUsuarioDAO {

    private final JdbcTemplate jdbcTemplate;

    public PerfilUsuarioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Crea perfil inicial cuando un usuario se registra por primera vez (Con valores por defecto)
    public boolean crearPerfilInicial(int idUsuario, String nombreCompleto) {
        String sql = "INSERT INTO perfil_usuario (id_usuario, nombre_completo, tema_interfaz) VALUES (?, ?, 'dark')";
        try {
            int filas = jdbcTemplate.update(sql, idUsuario, nombreCompleto);
            return filas > 0;
        } catch (Exception e) {
            System.err.println("Error al crear perfil inicial: " + e.getMessage());
            return false;
        }
    }
/*  Aun falta agregar la actualizacion del perfil (Cambiar el tema de interfaz, por ejemplo) pero por ahora con esto 
ya se crea el perfil al registrarse el usuario, lo cual es lo importante para que no haya problemas 
al mostrar la informacion del perfil en la vista del perfil_usuario.html*/
}