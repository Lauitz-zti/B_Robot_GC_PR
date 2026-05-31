package com.brazor.webapp.DAOs;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository // Le dice a Spring Boot que esta clase es un DAO componente de persistencia
public class UsuarioDAO {
    
    private final JdbcTemplate jdbcTemplate;

    // Spring Boot detecta este constructor e inyecta el JdbcTemplate de forma automática
    public UsuarioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Verifica si el usuario ya existe por email, si no existe lo inserta con el UID de Firebase como password_hash
    public boolean registrarOVerificar(String firebaseUid, String username, String email) {
        String sqlBuscar = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        
        Integer cuenta = jdbcTemplate.queryForObject(sqlBuscar, Integer.class, email);
        
        if (cuenta != null && cuenta > 0) {
            return true; // El usuario ya existe, no es necesario insertarlo de nuevo
        }
        
        // Si no existe, se inserta con el UID de Firebase como password_hash
        String sqlInsert = "INSERT INTO usuarios (username, email, password_hash, rol) VALUES (?, ?, ?, 'OPERATOR'::rol_usuario)";
        int filasAfectadas = jdbcTemplate.update(sqlInsert, username, email, firebaseUid);
        return filasAfectadas > 0;
    }
}