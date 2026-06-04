package com.brazor.webapp.DAOs;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoggerDAO {

    private final JdbcTemplate jdbcTemplate;

    public LoggerDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void registrarEvento(Integer idBrazo, String codigoError, String mensaje) {
        //Genera el log en la base de datos, si falla no colapsa la app pero se imprime el error en consola para revision
        String sql = "INSERT INTO logger (id_brazo, codigo_error, mensaje) VALUES (?, ?, ?)";
        
        try {
            jdbcTemplate.update(sql, idBrazo, codigoError, mensaje);
        } catch (Exception e) {
            // Imprimimos en consola para que el servidor no colapse si falla la base de datos
            System.err.println("Error al registrar evento (" + codigoError + "): " + e.getMessage());
        }
    }
}