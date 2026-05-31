package com.brazor.webapp.Controladores;

import com.brazor.webapp.DAOs.BrazoDAO;
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

    public BrazoController(BrazoDAO brazoDAO) {
        this.brazoDAO = brazoDAO;
    }

    @PostMapping("/mover")
    public ResponseEntity<Map<String, String>> moverBrazo(@RequestBody Map<String, Object> payload) {
        Map<String, String> respuesta = new HashMap<>();

        try {
            //Datos que esperamos recibir 
            int idBrazo = (int) payload.get("id_brazo");
            
            //construimos el JSON de angulos a partir de los datos recibidos
            int base = (int) payload.get("base");
            int hombro = (int) payload.get("hombro");
            int codo = (int) payload.get("codo");
            
            String jsonAngulos = String.format("{\"base\": %d, \"hombro\": %d, \"codo\": %d}", base, hombro, codo);

            //enviamos al DAO para actualizar la base de datos con los nuevos angulos del brazo
            boolean exito = brazoDAO.actualizarPosicion(idBrazo, jsonAngulos);

            if (exito) {
                respuesta.put("status", "success");
                respuesta.put("mensaje", "Actualizado...");
                return ResponseEntity.ok(respuesta);
            } else {
                respuesta.put("status", "error");
                respuesta.put("mensaje", "No encontramos el brazo con el ID especificado.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
            }

        } catch (Exception e) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "Formato de datos incorrecto.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }
}