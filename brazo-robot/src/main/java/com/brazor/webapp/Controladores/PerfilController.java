package com.brazor.webapp.Controladores;

import com.brazor.webapp.DAOs.PerfilUsuarioDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil")
@CrossOrigin(origins = "*")
public class PerfilController {

    private final PerfilUsuarioDAO perfilDAO;

    public PerfilController(PerfilUsuarioDAO perfilDAO) {
        this.perfilDAO = perfilDAO;
    }

    //Obtiene el perfil de un usuario por su ID (Para mostrarlo en la vista del perfil_usuario.html)
    @GetMapping("/obtener/{idUsuario}")
    public ResponseEntity<Map<String, Object>> obtenerPerfil(@PathVariable int idUsuario) {
        try {
            Map<String, Object> perfil = perfilDAO.obtenerPerfil(idUsuario);
            
            if (perfil != null) {
                perfil.put("status", "success");
                return ResponseEntity.ok(perfil);
            } else {
                return respuestaError(HttpStatus.NOT_FOUND, "No se encontró el perfil de este usuario.");
            }
        } catch (Exception e) {
            System.err.println("Error en controlador al obtener perfil: " + e.getMessage());
            return respuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor.");
        }
    }

    //Actualizar el perfil ya existente
    @PutMapping("/actualizar")
    public ResponseEntity<Map<String, Object>> actualizarPerfil(@RequestBody Map<String, Object> payload) {
        try {
            // Extraemos los datos del JSON que enviara a pagina web
            int idUsuario = (int) payload.get("id_usuario");
            String nombreCompleto = (String) payload.get("nombre_completo");
            String fotoUrl = (String) payload.get("foto_url");
            String temaInterfaz = (String) payload.get("tema_interfaz");
            String bio = (String) payload.get("bio");

            boolean exito = perfilDAO.actualizarPerfil(idUsuario, nombreCompleto, fotoUrl, temaInterfaz, bio);

            if (exito) {
                return respuestaExito("Perfil actualizado correctamente.");
            } else {
                return respuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudieron guardar los cambios");
            }

        } catch (Exception e) {
            System.err.println("Error en controlador al actualizar perfil: " + e.getMessage());
            return respuestaError(HttpStatus.BAD_REQUEST, "Formato de datos incorrecto o faltan campos.");
        }
    }

    //obtener el perfil con id del brazo robot (Para mostrarlo en la vista del perfil_usuario.html, que se accede desde cada brazo)
    @GetMapping("/obtener-por-brazo/{idBrazo}")
    public ResponseEntity<Map<String, Object>> obtenerPerfilPorBrazo(@PathVariable int idBrazo) {
        try {
            Map<String, Object> perfil = perfilDAO.obtenerPerfilPorBrazo(idBrazo);
            if (perfil != null) {
                return respuestaConDatos(perfil);
            }
            return respuestaError(HttpStatus.NOT_FOUND, "Perfil no encontrado.");
        } catch (Exception e) {
            return respuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor.");
        }
    }

    //METODOS AUXILIARES PARA RESPUESTAS UNIFICADAS (Evitan repetir código y mantienen la consistencia en las respuestas de la API)
    // Usamos Map<String, Object> para mantener la compatibilidad con los otros métodos
    private ResponseEntity<Map<String, Object>> respuestaError(HttpStatus status, String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("mensaje", "Fallo lógico: " + mensaje);
        return ResponseEntity.status(status).body(error);
    }

    private ResponseEntity<Map<String, Object>> respuestaExito(String mensaje) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("mensaje", mensaje);
        return ResponseEntity.ok(res);
    }

    private ResponseEntity<Map<String, Object>> respuestaConDatos(Map<String, Object> datos) {
        datos.put("status", "success");
        return ResponseEntity.ok(datos);
    }
}