package com.brazor.webapp.Controladores;

import com.brazor.webapp.DAOs.RecorridoDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recorridos")
@CrossOrigin(origins = "*")
public class RecorridoController {

    private final RecorridoDAO recorridoDAO;
    private final ObjectMapper objectMapper;

    public RecorridoController(RecorridoDAO recorridoDAO) {
        this.recorridoDAO = recorridoDAO;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/guardar")
    public ResponseEntity<Map<String, String>> guardar(@RequestBody Map<String, Object> payload) {
        try {
            int idBrazo = Integer.parseInt(payload.get("id_brazo").toString());
            String nombreEstudio = (String) payload.get("nombre_estudio");
            
            // Convertimos la lista de posiciones que manda JS a un String JSON para la BD
            Object secuencia = payload.get("secuencia");
            String secuenciaJson = objectMapper.writeValueAsString(secuencia);

            recorridoDAO.guardarRecorrido(idBrazo, nombreEstudio, secuenciaJson);

            return ResponseEntity.ok(Map.of("status", "success", "mensaje", "Secuencia grabada con éxito."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "mensaje", "Error al guardar el recorrido."));
        }
    }

@GetMapping("/listar/{idBrazo}")
    public ResponseEntity<?> listar(@PathVariable int idBrazo) {
        try {
            List<Map<String, Object>> lista = recorridoDAO.listarRecorridos(idBrazo);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "mensaje", "Fallo interno al listar: " + e.getMessage()));
        }
    }

    @GetMapping("/reproducir/{idRecorrido}")
    public ResponseEntity<String> reproducir(@PathVariable int idRecorrido) {
            String secuenciaJson = recorridoDAO.obtenerSecuencia(idRecorrido);
            return ResponseEntity.ok(secuenciaJson);
       
    }
}