package com.brazor.webapp.Controladores;

import com.brazor.webapp.DAOs.PuenteHDAO;
import com.brazor.webapp.Modelos.PuenteHardware;
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

    //Ruata para recibir el ping del brazo (envia los datos de movimiento y latencia)
    @PostMapping("/ping")
    public ResponseEntity<Map<String, Object>> recibirPing(@RequestBody PuenteHardware telemetriaEntrante) {
        Map<String, Object> respuesta = new HashMap<>();
        try {
            // Extraemos los datos usando tu Modelo
            int idBrazo = telemetriaEntrante.getIdBrazo();
            int latencia = telemetriaEntrante.getLatenciaMs();
            
            // Le pedimos al DAO que haga el trabajo sucio
            boolean exito = telemetriaDAO.actualizarPing(idBrazo, latencia);

            if (exito) {
                respuesta.put("status", "success");
                return ResponseEntity.ok(respuesta);
            } else {
                respuesta.put("status", "error");
                respuesta.put("mensaje", "No se encontró el puente de hardware.");
                return ResponseEntity.badRequest().body(respuesta);
            }
        } catch (Exception e) {
            respuesta.put("status", "error");
            respuesta.put("mensaje", "Datos de telemetría inválidos.");
            return ResponseEntity.badRequest().body(respuesta);
        }
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