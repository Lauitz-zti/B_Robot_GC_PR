package com.brazor.webapp.Controladores;

import com.brazor.webapp.DAOs.BrazoDAO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/brazo")
@CrossOrigin(origins = "*")
public class BrazoController {

    private final BrazoDAO brazoDAO;
    private final ObjectMapper objectMapper;

    public BrazoController(BrazoDAO brazoDAO) {
        this.brazoDAO = brazoDAO;
        this.objectMapper = new ObjectMapper();
    }

    //Cambiamos el endpoint separando el estado del brazo (GET) de la actualizacion (PUT), para seguir buenas practicas REST
    //Accede al estado actual del brazo robot (6 DOF) para mostrarlo en la interfaz al cargar la pagina
    @GetMapping("/estado/{idBrazo}")
    public ResponseEntity<Map<String, Object>> obtenerEstado(@PathVariable int idBrazo) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            // Le pedimos al DAO el texto JSON exacto que está en la base de datos
            String jsonPostgres = brazoDAO.obtenerEstadoActual(idBrazo);
            
            if (jsonPostgres != null) {
                // Convertimos el String de PostgreSQL a un Mapa para que viaje hacia la web como JSON puro
                Map<String, Object> angulos = objectMapper.readValue(jsonPostgres, new TypeReference<Map<String, Object>>() {});
                respuesta.put("status", "success");
                respuesta.put("angulos", angulos); 
                return ResponseEntity.ok(respuesta);
            } else {
                return respuestaError(HttpStatus.NOT_FOUND, "No hay telemetria previa para este brazo.");
            }
        } catch (Exception e) {
            System.err.println("Error al parsear estado: " + e.getMessage());
            return respuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Error al leer el estado del brazo.");
        }
    }

    //Actualiza la posicion del brazo robot (6 DOF) y guarda el historial del movimiento en la caja negra (historial_comandos)
    @PutMapping("/actualizar")
    public ResponseEntity<Map<String, Object>> actualizarEstado(@RequestBody Map<String, Object> payload) {
        try {
            //Obtienemos el id del brazo y el bloque de angulos del JSON enviado por la web
            int idBrazo = (int) payload.get("id_brazo");
            
            //Tomamos el bloque "angulos" completo
            // y usamos ObjectMapper para que lo convierta en texto seguro para PostgreSQL
            Object angulosObj = payload.get("angulos");
            String jsonAngulos = objectMapper.writeValueAsString(angulosObj);

            //Enviamos al DAO
            boolean exito = brazoDAO.actualizarPosicion(idBrazo, jsonAngulos);

            if (exito) {
                Map<String, Object> respuestaExito = new HashMap<>();
                respuestaExito.put("status", "success");
                respuestaExito.put("mensaje", "registro actualizado correctamente");
                return ResponseEntity.ok(respuestaExito);
            } else {
                return respuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "No encontramos el brazo o falló el guardado.");
            }
        } catch (Exception e) {
            System.err.println("Error en controlador al actualizar: " + e.getMessage());
            return respuestaError(HttpStatus.BAD_REQUEST, "Formato de datos incorrecto");
        }
    }

    //METODOS AUXILIARES PARA RESPUESTAS UNIFICADAS
    private ResponseEntity<Map<String, Object>> respuestaError(HttpStatus status, String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(error);
    }
}
