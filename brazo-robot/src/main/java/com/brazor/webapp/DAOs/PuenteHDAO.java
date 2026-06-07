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
    

    // Registra la IP real al momento de conectarse
    public boolean registrarConexion(int idBrazo, String ipCliente) {
        String sql = "INSERT INTO puente_hardware (id_brazo, ip_maquina_java, estado_conexion) " +
                     "VALUES (?, ?, TRUE) " +
                     "ON CONFLICT (id_brazo) " +
                     "DO UPDATE SET ip_maquina_java = EXCLUDED.ip_maquina_java, estado_conexion = TRUE";
        try {
            return jdbcTemplate.update(sql, idBrazo, ipCliente) > 0;
        } catch (Exception e) {
            System.err.println("Error al registrar conexión: " + e.getMessage());
            return false;
        }
    }

    //Mide el tiempo de inactividad para mostrarlo en la pagina web
    public Map<String, Object> obtenerEstadoConexion(int idBrazo) {
        String sql = "SELECT estado_conexion, EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - last_ping)) AS segundos_inactivo FROM puente_hardware WHERE id_brazo = ?";
        try {
            return jdbcTemplate.queryForMap(sql, idBrazo);
        } catch (Exception e) {
            // Retorna null si el brazo no tiene registro de telemetriia o si hubo un error
            return null; 
        }
    }

    //Apagamos la conexion si no hay respuesta 
    public void marcarDesconectado(int idBrazo) {
        String sql = "UPDATE puente_hardware SET estado_conexion = FALSE WHERE id_brazo = ?";
        try {
            jdbcTemplate.update(sql, idBrazo);
        } catch (Exception e) {
            System.err.println("Error en TelemetriaDAO al desconectar: " + e.getMessage());
        }
    }
} 
