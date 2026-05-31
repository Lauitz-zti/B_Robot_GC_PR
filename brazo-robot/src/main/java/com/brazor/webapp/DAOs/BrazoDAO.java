package com.brazor.webapp.DAOs;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

//Hablamos con la tabla estado_actual
@Repository
public class BrazoDAO {

    private final JdbcTemplate jdbcTemplate;

    public BrazoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Actualiza la fila del brazo con los nuevos angulos y la fecha de actualizacion
    public boolean actualizarPosicion(int idBrazo, String jsonAngulos) {
        String sqlUpdate = "UPDATE estado_actual SET angulos_jsonb = ?::jsonb, fecha_update = CURRENT_TIMESTAMP WHERE id_brazo = ?";
        
        try {
            int filasAfectadas = jdbcTemplate.update(sqlUpdate, jsonAngulos, idBrazo);
            return filasAfectadas > 0;
        } catch (Exception e) {
            System.err.println("Error al actualizar telemetria: " + e.getMessage());
            return false;
        }
    }
}