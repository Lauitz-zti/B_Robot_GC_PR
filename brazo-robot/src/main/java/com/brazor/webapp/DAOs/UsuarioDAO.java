package com.brazor.webapp.DAOs;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository // Le dice a Spring Boot que esta clase es un DAO componente de persistencia
public class UsuarioDAO {
    
    private final JdbcTemplate jdbcTemplate;

    // Spring Boot detecta este constructor e inyecta el JdbcTemplate de forma automática
    public UsuarioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    //Verifica si el usuario ya existe por email, si no existe lo inserta con el UID de Firebase como password_hash
    public Integer registrarOVerificar(String firebaseUid, String username, String email) {
        String sqlBuscar = "SELECT id_usuario FROM usuarios WHERE email = ?";

        List<Integer> cuenta = jdbcTemplate.queryForList(sqlBuscar, Integer.class, email);

       if (!cuenta.isEmpty()) {
            // Ya existe, buscamos cual es el ID de su brazo robot
            Integer idUsuario = cuenta.get(0);
            String sqlBuscarBrazo = "SELECT id_brazo FROM brazo_robot WHERE id_usuario = ?";
            
            List<Integer> brazosIds = jdbcTemplate.queryForList(sqlBuscarBrazo, Integer.class, idUsuario);
            return brazosIds.isEmpty() ? null : brazosIds.get(0);
        }
        
        try {
            //Si no existe, creamos el usuario
            String sqlInsert = "INSERT INTO usuarios (username, email, password_hash, rol) VALUES (?, ?, ?, 'OPERATOR'::rol_usuario) RETURNING id_usuario";
            Integer idNuevoUsuario = jdbcTemplate.queryForObject(sqlInsert, Integer.class, username, email, firebaseUid);
            
            if (idNuevoUsuario != null) {
                //Asignamos su brazo robot propio
                String nombreBrazo = "Brazo de " + username;
                String sqlInsertBrazo = "INSERT INTO brazos_roboticos (id_usuario, nombre_instancia, es_simulado) VALUES (?, ?, TRUE) RETURNING id_brazo";
                Integer idNuevoBrazo = jdbcTemplate.queryForObject(sqlInsertBrazo, Integer.class, idNuevoUsuario, nombreBrazo);
                
                //Inicializamos el estado del nuevo brazo robot
                if (idNuevoBrazo != null) {
                    String sqlInsertEstado = "INSERT INTO estado_actual (id_brazo, angulos_jsonb, fuente) VALUES (?, '{\"base\": 0, \"hombro\": 0, \"codo\": 0}'::jsonb, 'WEB')";
                    jdbcTemplate.update(sqlInsertEstado, idNuevoBrazo);
                    return idNuevoBrazo; // Retornamos la llave maestra
                }
            }
        } catch (Exception e) {
            System.err.println("Error fatal al aprovisionar: " + e.getMessage());
        }
        return null;
    }
}