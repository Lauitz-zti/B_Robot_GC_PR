package com.brazor.webapp.DAOs;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class PuenteHDAO {

    private final JdbcTemplate jdbcTemplate;

    public PuenteHDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Actualiza el ping del brazo
    public boolean actualizarPing(int idBrazo, int latenciaMs) {
        String sql = "UPDATE hardware_bridge SET estado_conexion = TRUE, latencia_ms = ?, last_ping = CURRENT_TIMESTAMP WHERE id_brazo = ?";
        try {
            int filasAfectadas = jdbcTemplate.update(sql, latenciaMs, idBrazo);
            return filasAfectadas > 0;
        } catch (Exception e) {
            System.err.println("Error en TelemetriaDAO al actualizar ping: " + e.getMessage());
            return false;
        }
    }

    //Mide el tiempo de inactividad para mostrarlo en la pagina web
    public Map<String, Object> obtenerEstadoConexion(int idBrazo) {
        String sql = "SELECT estado_conexion, EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - last_ping)) AS segundos_inactivo FROM hardware_bridge WHERE id_brazo = ?";
        try {
            return jdbcTemplate.queryForMap(sql, idBrazo);
        } catch (Exception e) {
            // Retorna null si el brazo no tiene registro de telemetriia o si hubo un error
            return null; 
        }
    }

    //Apagamos la conexion si no hay respuesta 
    public void marcarDesconectado(int idBrazo) {
        String sql = "UPDATE hardware_bridge SET estado_conexion = FALSE WHERE id_brazo = ?";
        try {
            jdbcTemplate.update(sql, idBrazo);
        } catch (Exception e) {
            System.err.println("Error en TelemetriaDAO al desconectar: " + e.getMessage());
        }
    }
} 
