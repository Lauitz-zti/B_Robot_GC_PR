package com.brazor.webapp.Controladores;

import com.brazor.webapp.DAOs.AdminDAO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AdminController — Controlador REST exclusivo del panel de administración.
 *
 * Protección: cada endpoint verifica que el token Firebase del header
 * corresponda a un usuario con rol ADMIN antes de ejecutar cualquier lógica.
 * Así el rol del enum en PostgreSQL tiene efecto real en el backend.
 *
 * Todos los endpoints esperan el header:
 *   Authorization: Bearer <firebase-id-token>
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminDAO adminDAO;
    private final FirebaseAuth firebaseAuth;

    public AdminController(AdminDAO adminDAO, FirebaseAuth firebaseAuth) {
        this.adminDAO = adminDAO;
        this.firebaseAuth = firebaseAuth;
    }

    // ─────────────────────────────────────────────
    // DASHBOARD — Métricas generales
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/dashboard
     * Devuelve las 4 métricas de las tarjetas del panel.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestHeader("Authorization") String authHeader) {

        ResponseEntity<Map<String, Object>> error = verificarAdmin(authHeader);
        if (error != null) return error;

        Map<String, Object> metricas = Map.of(
                "total_usuarios",        adminDAO.contarUsuarios(),
                "usuarios_activos",      adminDAO.contarUsuariosActivos(),
                "total_brazos",          adminDAO.contarBrazos(),
                "total_logs",            adminDAO.contarLogs()
        );
        return ResponseEntity.ok(metricas);
    }

    // ─────────────────────────────────────────────
    // USUARIOS
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/usuarios
     * Lista todos los usuarios con su perfil y brazo asignado.
     */
    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios(
            @RequestHeader("Authorization") String authHeader) {

        ResponseEntity<Map<String, Object>> error = verificarAdmin(authHeader);
        if (error != null) return error;

        List<Map<String, Object>> usuarios = adminDAO.listarTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * PUT /api/admin/usuarios/{idUsuario}/rol
     * Cambia el rol de un usuario.
     * Body JSON: { "rol": "ADMIN" | "OPERATOR" | "VIEWER" }
     */
    @PutMapping("/usuarios/{idUsuario}/rol")
    public ResponseEntity<Map<String, String>> cambiarRol(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int idUsuario,
            @RequestBody Map<String, String> body) {

        ResponseEntity<Map<String, Object>> error = verificarAdmin(authHeader);
        if (error != null) return ResponseEntity.status(error.getStatusCode())
                .body(Map.of("status", "error", "mensaje", "No autorizado"));

        String nuevoRol = body.get("rol");

        // Validamos que el rol sea uno de los tres valores del ENUM
        if (nuevoRol == null || !nuevoRol.matches("ADMIN|OPERATOR|VIEWER")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error",
                            "mensaje", "Rol inválido. Valores permitidos: ADMIN, OPERATOR, VIEWER"));
        }

        boolean exito = adminDAO.cambiarRol(idUsuario, nuevoRol);

        if (exito) {
            return ResponseEntity.ok(
                    Map.of("status", "success",
                            "mensaje", "Rol actualizado a " + nuevoRol));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "mensaje", "No se pudo actualizar el rol."));
        }
    }

    /**
     * PUT /api/admin/usuarios/{idUsuario}/baja
     * Da de baja (soft-delete) a un usuario — pone activo = FALSE.
     */
    @PutMapping("/usuarios/{idUsuario}/baja")
    public ResponseEntity<Map<String, String>> darDeBaja(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int idUsuario) {

        ResponseEntity<Map<String, Object>> error = verificarAdmin(authHeader);
        if (error != null) return ResponseEntity.status(error.getStatusCode())
                .body(Map.of("status", "error", "mensaje", "No autorizado"));

        boolean exito = adminDAO.darDeBaja(idUsuario);

        if (exito) {
            return ResponseEntity.ok(
                    Map.of("status", "success", "mensaje", "Usuario dado de baja correctamente."));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "mensaje", "No se pudo dar de baja al usuario."));
        }
    }

    /**
     * PUT /api/admin/usuarios/{idUsuario}/reactivar
     * Reactiva un usuario previamente dado de baja.
     */
    @PutMapping("/usuarios/{idUsuario}/reactivar")
    public ResponseEntity<Map<String, String>> reactivar(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable int idUsuario) {

        ResponseEntity<Map<String, Object>> error = verificarAdmin(authHeader);
        if (error != null) return ResponseEntity.status(error.getStatusCode())
                .body(Map.of("status", "error", "mensaje", "No autorizado"));

        boolean exito = adminDAO.reactivar(idUsuario);

        if (exito) {
            return ResponseEntity.ok(
                    Map.of("status", "success", "mensaje", "Usuario reactivado correctamente."));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "mensaje", "No se pudo reactivar al usuario."));
        }
    }

    // ─────────────────────────────────────────────
    // BRAZOS ROBÓTICOS
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/brazos
     * Lista todos los brazos con su dueño y último estado.
     */
    @GetMapping("/brazos")
    public ResponseEntity<?> listarBrazos(
            @RequestHeader("Authorization") String authHeader) {

        ResponseEntity<Map<String, Object>> error = verificarAdmin(authHeader);
        if (error != null) return error;

        List<Map<String, Object>> brazos = adminDAO.listarTodosLosBrazos();
        return ResponseEntity.ok(brazos);
    }

    // ─────────────────────────────────────────────
    // LOGS GLOBALES
    // ─────────────────────────────────────────────

    /**
     * GET /api/admin/logs?limite=100
     * Devuelve los últimos N eventos de la bitácora global.
     * El parámetro límite es opcional (default 100, máx 500).
     */
    @GetMapping("/logs")
    public ResponseEntity<?> listarLogs(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "100") int limite) {

        ResponseEntity<Map<String, Object>> error = verificarAdmin(authHeader);
        if (error != null) return error;

        // Cap de seguridad para no traer toda la tabla
        if (limite > 500) limite = 500;

        List<Map<String, Object>> logs = adminDAO.listarLogsGlobales(limite);
        return ResponseEntity.ok(logs);
    }

    // ─────────────────────────────────────────────
    // MÉTODO AUXILIAR DE VERIFICACIÓN
    // ─────────────────────────────────────────────

    /**
     * Verifica que el token Firebase sea válido y que el usuario tenga rol ADMIN.
     * @return null si está autorizado, o un ResponseEntity de error si no.
     */
    private ResponseEntity<Map<String, Object>> verificarAdmin(String authHeader) {

        // 1. Validar que el header exista y tenga formato "Bearer <token>"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "mensaje", "Token no proporcionado."));
        }

        String idToken = authHeader.substring(7); // Quitamos el "Bearer "

        try {
            // 2. Verificar el token con Firebase
            FirebaseToken tokenVerificado = firebaseAuth.verifyIdToken(idToken);
            String email = tokenVerificado.getEmail();

            // 3. Consultar el rol del usuario en nuestra BD
            String rol = adminDAO.obtenerRolPorEmail(email);

            if (!"ADMIN".equals(rol)) {
                // El usuario existe pero no es ADMIN (puede ser OPERATOR o VIEWER)
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("status", "error",
                                "mensaje", "Acceso denegado. Se requiere rol ADMIN."));
            }

            // 4. Todo bien — el caller puede proceder
            return null;

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error",
                            "mensaje", "Token inválido o expirado: " + e.getAuthErrorCode()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "mensaje", "Error interno del servidor."));
        }
    }
}