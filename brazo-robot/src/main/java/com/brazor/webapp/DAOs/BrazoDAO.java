package com.brazor.webapp.DAOs;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

//Hablamos con la tabla estado_actual
@Repository
public class BrazoDAO {

    private final JdbcTemplate jdbcTemplate;

    public BrazoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Actualiza la fila del brazo con los nuevos angulos y la fecha de actualizacion
    // Usamos @Transactional para asegurar que si falla el historial, no se actualice el estado a medias
    @Transactional
    public boolean actualizarPosicion(int idBrazo, String jsonAngulos) {

        try {
            //Actualizamos la posicion actual (El tiempo real)
            String sqlUpdate = "UPDATE estado_actual SET angulos_jsonb = ?::jsonb, fecha_update = CURRENT_TIMESTAMP WHERE id_brazo = ?";
            int filasAfectadas = jdbcTemplate.update(sqlUpdate, jsonAngulos, idBrazo);
            
            // Si el brazo es nuevo, lo insertamos
            if (filasAfectadas == 0) {
                System.out.println("Creando estado inicial para brazo: " + idBrazo);
                String sqlInsert = "INSERT INTO estado_actual (id_brazo, angulos_jsonb, fuente, fecha_update) VALUES (?, CAST(? AS jsonb), 'WEB', CURRENT_TIMESTAMP)";
                jdbcTemplate.update(sqlInsert, idBrazo, jsonAngulos);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar telemetria: " + e.getMessage());
            return false;
        }
    }

    //Sincronizacion inicial: Trae el estado actual del brazo para mostrarlo en la interfaz al cargar la pagina
    public String obtenerEstadoActual(int idBrazo) {
        String sql = "SELECT angulos_jsonb FROM estado_actual WHERE id_brazo = ?";
        try {
            // Retorna directamente el JSON que esta guardado en la base de datos
            return jdbcTemplate.queryForObject(sql, String.class, idBrazo);
        } catch (Exception e) {
            System.err.println("Error al obtener el estado inicial del brazo: " + e.getMessage());
            return null;
        }
    }
}