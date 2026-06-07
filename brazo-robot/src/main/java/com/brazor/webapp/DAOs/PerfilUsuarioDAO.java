package com.brazor.webapp.DAOs;

import java.util.Map;

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
        String sql = "INSERT INTO perfil_usuario (id_usuario, nombre_completo) VALUES (?, ?)";
        try {
            int filas = jdbcTemplate.update(sql, idUsuario, nombreCompleto);
            return filas > 0;
        } catch (Exception e) {
            System.err.println("Error al crear perfil inicial: " + e.getMessage());
            return false;
        }
    }

//Obtiene el perfil de un usuario (Para mostrarlo en la vista)
    public Map<String, Object> obtenerPerfil(int idUsuario) {
        String sql = "SELECT nombre_completo, foto_url, bio FROM perfil_usuario WHERE id_usuario = ?";
        try {
            return jdbcTemplate.queryForMap(sql, idUsuario);
        } catch (Exception e) {
            System.err.println("Error al obtener perfil: " + e.getMessage());
            return null; // Retorna null si no lo encuentra
        }
    }

    //Actualiza el perfil (Cuando el usuario guarda cambios en configuracion)
    public boolean actualizarPerfil(int idUsuario, String nombreCompleto, String fotoUrl, String bio) {
        String sql = "UPDATE perfil_usuario SET nombre_completo = ?, foto_url = ?, bio = ? WHERE id_usuario = ?";
        try {
            int filas = jdbcTemplate.update(sql, nombreCompleto, fotoUrl, bio, idUsuario);
            return filas > 0;
        } catch (Exception e) {
            System.err.println("Error al actualizar perfil: " + e.getMessage());
            return false;
        }
    }

    //Muestra el perfil deacuerdo al id del brazo robot que se le asigno al usuario (Para mostrarlo en la vista del perfil_usuario.html)
    public Map<String, Object> obtenerPerfilPorBrazo(int idBrazo) {
        String sql = "SELECT p.id_usuario, p.nombre_completo, p.foto_url, p.bio " +
                     "FROM perfil_usuario p " +
                     "JOIN brazo_robot b ON p.id_usuario = b.id_usuario " +
                     "WHERE b.id_brazo = ?";
        try {
            return jdbcTemplate.queryForMap(sql, idBrazo);
        } catch (Exception e) {
            System.err.println("Error al cruzar perfil por brazo: " + e.getMessage());
            return null;
        }
    }
}