package com.brazor.webapp.DAOs;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public class AdminDAO {

    private final JdbcTemplate jdbcTemplate;

    public AdminDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /* USUARIOS
     * Devuelve todos los usuarios con su perfil y brazo asignado.
     * Se usa en la tabla principal del panel de admin.
     */
    public List<Map<String, Object>> listarTodosLosUsuarios() {
        String sql = """
                SELECT
                    u.id_usuario,
                    u.username,
                    u.email,
                    u.rol,
                    u.activo,
                    u.fecha_registro,
                    COALESCE(p.nombre_completo, u.username) AS nombre_completo,
                    p.foto_url,
                    b.id_brazo,
                    b.nombre_instancia,
                    b.es_simulado
                FROM usuarios u
                LEFT JOIN perfil_usuario p ON u.id_usuario = p.id_usuario
                LEFT JOIN brazo_robot    b ON u.id_usuario = b.id_usuario
                ORDER BY u.fecha_registro DESC
                """;
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            System.err.println("AdminDAO.listarTodosLosUsuarios: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Cambia el rol de un usuario (ADMIN / OPERATOR).
     */
    public boolean cambiarRol(int idUsuario, String nuevoRol) {
        String sql = "UPDATE usuarios SET rol = ?::rol_usuario WHERE id_usuario = ?";
        try {
            int filas = jdbcTemplate.update(sql, nuevoRol, idUsuario);
            return filas > 0;
        } catch (Exception e) {
            System.err.println("AdminDAO.cambiarRol: " + e.getMessage());
            return false;
        }
    }

    /**
     * Da de baja a un usuario poniendo activo = FALSE.
     * No borramos el registro para conservar la integridad referencial
     * con brazo_robot, logger y recorridos_save.
     */
    @Transactional
    public boolean darDeBaja(int idUsuario) {
        String sql = "UPDATE usuarios SET activo = FALSE WHERE id_usuario = ?";
        try {
            int filas = jdbcTemplate.update(sql, idUsuario);
            return filas > 0;
        } catch (Exception e) {
            System.err.println("AdminDAO.darDeBaja: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reactiva un usuario previamente dado de baja.
     */
    public boolean reactivar(int idUsuario) {
        String sql = "UPDATE usuarios SET activo = TRUE WHERE id_usuario = ?";
        try {
            int filas = jdbcTemplate.update(sql, idUsuario);
            return filas > 0;
        } catch (Exception e) {
            System.err.println("AdminDAO.reactivar: " + e.getMessage());
            return false;
        }
    }

    /*
    PARA LOS BRAZOS ROBT
     * Lista todos los brazos con su dueño y estado actual de ángulos.
     */
    public List<Map<String, Object>> listarTodosLosBrazos() {
        String sql = """
                SELECT
                    b.id_brazo,
                    b.nombre_instancia,
                    b.es_simulado,
                    b.fecha_creacion,
                    u.username,
                    u.email,
                    u.activo AS usuario_activo,
                    e.angulos_jsonb,
                    e.fecha_update AS ultima_actualizacion
                FROM brazo_robot b
                JOIN usuarios     u ON b.id_usuario  = u.id_usuario
                LEFT JOIN estado_actual e ON b.id_brazo = e.id_brazo
                ORDER BY b.fecha_creacion DESC
                """;
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            System.err.println("AdminDAO.listarTodosLosBrazos: " + e.getMessage());
            return List.of();
        }
    }


    // LOGS GLOBALES
    public List<Map<String, Object>> listarLogsGlobales(int limite) {
        String sql = """
                SELECT
                    l.id_log,
                    l.id_brazo,
                    b.nombre_instancia,
                    u.username,
                    l.codigo_error,
                    l.mensaje,
                    l.fecha_error
                FROM logger l
                JOIN brazo_robot b ON l.id_brazo = b.id_brazo
                JOIN usuarios    u ON b.id_usuario = u.id_usuario
                ORDER BY l.fecha_error DESC
                LIMIT ?
                """;
        try {
            return jdbcTemplate.queryForList(sql, limite);
        } catch (Exception e) {
            System.err.println("AdminDAO.listarLogsGlobales: " + e.getMessage());
            return List.of();
        }
    }


    /** Total de usuarios registrados */
    public int contarUsuarios() {
        try {
            Integer r = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class);
            return r != null ? r : 0;
        } catch (Exception e) { return 0; }
    }

    /** Total de usuarios activos */
    public int contarUsuariosActivos() {
        try {
            Integer r = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM usuarios WHERE activo = TRUE", Integer.class);
            return r != null ? r : 0;
        } catch (Exception e) { return 0; }
    }

    /** Total de brazos registrados */
    public int contarBrazos() {
        try {
            Integer r = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM brazo_robot", Integer.class);
            return r != null ? r : 0;
        } catch (Exception e) { return 0; }
    }

    /** Total de eventos en la bitacra */
    public int contarLogs() {
        try {
            Integer r = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM logger", Integer.class);
            return r != null ? r : 0;
        } catch (Exception e) { return 0; }
    }

    /**
     * Verifica el rol de un usuario por su Firebase UID.
     * Lo usa el controlador para validar que quien llama sea ADMIN.
     */
    public String obtenerRolPorEmail(String email) {
        String sql = "SELECT rol FROM usuarios WHERE email = ? AND activo = TRUE";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, email);
        } catch (Exception e) {
            return null; // Usuario no existe o inactivo
        }
    }
} 