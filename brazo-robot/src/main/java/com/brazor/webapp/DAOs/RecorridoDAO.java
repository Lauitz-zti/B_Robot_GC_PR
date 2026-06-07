package com.brazor.webapp.DAOs;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class RecorridoDAO {

    private final JdbcTemplate jdbcTemplate;

    public RecorridoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Guardar una nueva macro
    public void guardarRecorrido(int idBrazo, String nombreEstudio, String secuenciaJson) {
        String sql = "INSERT INTO recorridos_save (id_brazo, nombre_estudio, secuencia_jsonb) VALUES (?, ?, ?::jsonb)";
        jdbcTemplate.update(sql, idBrazo, nombreEstudio, secuenciaJson);
    }

    //Lisar los recorridos guardados para un brazo 
    public List<Map<String, Object>> listarRecorridos(int idBrazo) {
        String sql = "SELECT id_recorrido, nombre_estudio FROM recorridos_save WHERE id_brazo = ? ORDER BY id_recorrido DESC";
        return jdbcTemplate.queryForList(sql, idBrazo);
    }

    //recuperar la secuencia para reproducirla
    public String obtenerSecuencia(int idRecorrido) {
        // Usamos ::text para extraer el JSONB como String hacia Java
        String sql = "SELECT secuencia_jsonb::text FROM recorridos_save WHERE id_recorrido = ?";
        return jdbcTemplate.queryForObject(sql, String.class, idRecorrido);
    }
}