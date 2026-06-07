package com.brazor.webapp.Controladores;

import com.brazor.webapp.DAOs.PuenteHDAO;
import com.brazor.webapp.Modelos.PuenteHardware;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/puente")
@CrossOrigin(origins = "*")
public class PuenteController {

    private final PuenteHDAO telemetriaDAO;

    public PuenteController(PuenteHDAO telemetriaDAO) {
        this.telemetriaDAO = telemetriaDAO;
    }

    @PostMapping("/conectar")
    public ResponseEntity<Map<String, Object>> conectarCliente(@RequestBody Map<String, Integer> payload, HttpServletRequest request) {
        int idBrazo = payload.get("id_brazo");
        String ipCliente = request.getRemoteAddr(); // IP 100% real
            
        telemetriaDAO.registrarConexion(idBrazo, ipCliente);
            
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", "success");
        return ResponseEntity.ok(respuesta);
        }

    @PostMapping("/desconectar")
    public ResponseEntity<Map<String, Object>> desconectarCliente(@RequestBody Map<String, Integer> payload) {
        int idBrazo = payload.get("id_brazo");
        telemetriaDAO.marcarDesconectado(idBrazo);
            
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("status", "success");
        return ResponseEntity.ok(respuesta);
        }

    //Verifica el estado de conexion del brazo y el tiempo de inactividad para mostrarlo en la pagina web
    @GetMapping("/estado/{idBrazo}")
    public ResponseEntity<Map<String, Object>> verificarConexion(@PathVariable int idBrazo) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            // Le pedimos al DAO el tiempo de inactividad
            Map<String, Object> datos = telemetriaDAO.obtenerEstadoConexion(idBrazo);
            
            if (datos != null) {
                boolean conectado = (boolean) datos.get("estado_conexion");
                double segundosInactivo = ((Number) datos.get("segundos_inactivo")).doubleValue();

                //REGLA DE NEGOCIO: Si pasaron mas de 20 segundos, mandamos a desconectar
                if (conectado && segundosInactivo > 20.0) {
                    telemetriaDAO.marcarDesconectado(idBrazo);
                    conectado = false;
                }

                respuesta.put("conectado", conectado);
                respuesta.put("inactividad_segundos", Math.round(segundosInactivo));
            } else {
                respuesta.put("conectado", false);
            }
            return ResponseEntity.ok(respuesta);
            
        } catch (Exception e) {
            respuesta.put("conectado", false);
            return ResponseEntity.ok(respuesta);
        }
    }
}