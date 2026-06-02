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
            jdbcTemplate.update(sqlUpdate, jsonAngulos, idBrazo);

            //Busca a quien le pertenece este brazo (Necesitamos el id_usuario para el historial)
            String sqlGetUsuario = "SELECT id_usuario FROM brazo_robot WHERE id_brazo = ?";
            Integer idUsuario = jdbcTemplate.queryForObject(sqlGetUsuario, Integer.class, idBrazo);

            //Guardamos el movimiento en la caja negra (historial_comandos)
            if (idUsuario != null) {
                String sqlInsertHistorial = "INSERT INTO historial_comandos (id_brazo, id_usuario, comando_json, ejecutado_en_fisico) VALUES (?, ?, ?, false)";
                jdbcTemplate.update(sqlInsertHistorial, idBrazo, idUsuario, jsonAngulos);
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