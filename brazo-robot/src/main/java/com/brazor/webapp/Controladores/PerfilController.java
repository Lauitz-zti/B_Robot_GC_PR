package com.brazor.webapp.Controladores;

import com.brazor.webapp.DAOs.PerfilUsuarioDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Base64;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil")
@CrossOrigin(origins = "*")
public class PerfilController {

    private final PerfilUsuarioDAO perfilDAO;

    @Value("${imgbb.api.key}")
    private String imgbbApiKey;

    public PerfilController(PerfilUsuarioDAO perfilDAO) {
        this.perfilDAO = perfilDAO;
    }

    //Obtiene el perfil de un usuario por su ID (Para mostrarlo en la vista del perfil.html)
    @GetMapping("/obtener/{idUsuario}")
    public ResponseEntity<Map<String, Object>> obtenerPerfil(@PathVariable int idUsuario) {
        try {
            Map<String, Object> perfil = perfilDAO.obtenerPerfil(idUsuario);

            if (perfil != null) {
                perfil.put("status", "success");
                return ResponseEntity.ok(perfil);
            } else {
                return respuestaError(HttpStatus.NOT_FOUND, "No se encontro el perfil de este usuario.");
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
            //Validamos que id_usuario exista en el payload(los datos mandados por perfil.html) antes de intentar parsearlo
            Object idObj = payload.get("id_usuario");
            if (idObj == null || idObj.toString().isBlank()) {
                return respuestaError(HttpStatus.BAD_REQUEST, "El campo id_usuario es obligatorio.");
            }
            int idUsuario;
            try {
                idUsuario = Integer.parseInt(idObj.toString());
            } catch (NumberFormatException e) {
                return respuestaError(HttpStatus.BAD_REQUEST, "El id_usuario no es un numero valido: " + idObj);
            }
            if (idUsuario <= 0) {
                return respuestaError(HttpStatus.BAD_REQUEST, "El id_usuario debe ser un número positivo.");
            }

            String nombreCompleto = (String) payload.get("nombre_completo");
            String fotoUrl        = (String) payload.get("foto_url");
            String bio            = (String) payload.get("bio");

            boolean exito = perfilDAO.actualizarPerfil(idUsuario, nombreCompleto, fotoUrl, bio);
            if (exito) {
                return respuestaExito("Perfil actualizado correctamente.");
            } else {
                return respuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudieron guardar los cambios en la base de datos.");
            }

        } catch (Exception e) {
            System.err.println("Error inesperado en actualizarPerfil: " + e.getMessage());
            return respuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al actualizar el perfil.");
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

    // Puente seguro hacia ImgBB
    @PostMapping("/subir-foto")
    public ResponseEntity<Map<String, Object>> subirFotoAImgBB(@RequestParam("image") MultipartFile file) {
        try {
            //Validamos que el archvo no sea vacio
            if (file == null || file.isEmpty()) {
                return respuestaError(HttpStatus.BAD_REQUEST, "No se recibio ninguna imagen.");
            }

            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("key", imgbbApiKey);
            body.add("image", base64Image);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity("https://api.imgbb.com/1/upload", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                //Verificamos que "data" y "url" existan en la respuesta de ImgBB
                if (data == null || data.get("url") == null) {
                    return respuestaError(HttpStatus.BAD_GATEWAY, "ImgBB no devolvio una URL valida.");
                }

                String url = (String) data.get("url");
                System.out.println("Imagen subida exitosamente a ImgBB: " + url);

                return ResponseEntity.ok(Map.of("status", "success", "url", url));
            } else {
                return respuestaError(HttpStatus.BAD_GATEWAY, "El servidor de imagenes rechazo la peticion.");
            }
        } catch (Exception e) {
            System.err.println("Error al subir imagen a ImgBB: " + e.getMessage());
            return respuestaError(HttpStatus.INTERNAL_SERVER_ERROR, "Fallo interno al procesar la imagen.");
        }
    }

    //Metodo auxiliar 
    // Usamos Map<String, Object> para mantener la compatibilidad con los otros metodos
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