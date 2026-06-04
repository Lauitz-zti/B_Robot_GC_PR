package com.brazor.webapp.Controladores;

import com.brazor.webapp.DAOs.LoggerDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logger")
@CrossOrigin(origins = "*")
public class LoggerController {

    private final LoggerDAO loggerDAO;

    public LoggerController(LoggerDAO loggerDAO) {
        this.loggerDAO = loggerDAO;
    }

    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarLog(@RequestBody Map<String, Object> payload) {
        try {
            // Extraemos y validamos los datos
            Integer idBrazo = payload.get("id_brazo") != null ? Integer.parseInt(payload.get("id_brazo").toString()) : null;
            String codigoError = (String) payload.get("codigo_error");
            String mensaje = (String) payload.get("mensaje");

            // codigoError es obligatorio para que no falle la Foreign Key
            if (idBrazo == null || codigoError == null || mensaje == null || codigoError.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", "error", "mensaje", "Faltan parámetros obligatorios."));
            }

            // Pasamos los datos al DAO
            loggerDAO.registrarEvento(idBrazo, codigoError, mensaje);

            return ResponseEntity.ok(Map.of("status", "success", "mensaje", "Evento registrado correctamente."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "mensaje", "Fallo interno al procesar la bitácora."));
        }
    }
}