package com.brazor.webapp.Controladores;

import com.brazor.webapp.DAOs.UsuarioDAO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioDAO usuarioDAO;
    private final FirebaseAuth firebaseAuth;

    public UsuarioController(FirebaseAuth firebaseAuth, UsuarioDAO usuarioDAO) {
        this.firebaseAuth = firebaseAuth;
        this.usuarioDAO = usuarioDAO;
    }

    @PostMapping("/verificar-token")
    public ResponseEntity<Map<String, String>> verificarToken(@RequestBody Map<String, Object> payload) {
        String idToken = (String) payload.get("token");

        //Validar que el token no sea nulo o vacio antes de intentar verificarlo con Firebase
        if (idToken == null || idToken.isBlank()) {
            return respuesta(HttpStatus.BAD_REQUEST, "error", "El token no puede estar vacio.");
        }

        try {
            // 2. Verificacion del token con Firebase
            FirebaseToken tokenVerificado = firebaseAuth.verifyIdToken(idToken);
            String uid   = tokenVerificado.getUid();
            String email = tokenVerificado.getEmail();
            String username = (String) payload.get("username");

            // 3. Validacion del email antes de usarlo
            if (email == null || !email.contains("@")) {
                return respuesta(HttpStatus.BAD_REQUEST, "error", "email invalido");
            }

            // 4. Persistencia en base de datos
            Integer idBrazo = usuarioDAO.registrarOVerificar(uid, username, email);

            if (idBrazo != null) {
                return respuestaExito(username, String.valueOf(idBrazo)); //Usamos el método para exito que incluye el id del brazo robot
            } else {
                return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "error",
                        "Error al guardar en la base de datos");
            }

        } catch (FirebaseAuthException e) {
            // 5. Error específico de Firebase (token inválido, expirado, revocado…)
            return respuesta(HttpStatus.UNAUTHORIZED, "error", "Token invalido o expirado: " + e.getAuthErrorCode());

        } catch (Exception e) {
            // 6. Error inesperado — no expones el stack trace al cliente
            return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "error",
                    "Error del servidor");
        }
    }



    /*METODO AUXILIAR */
    // Metodo auxiliar para no repetir new HashMap<>() en cada return
    private ResponseEntity<Map<String, String>> respuesta(HttpStatus status, String statusVal, String mensaje) 
    {
        return ResponseEntity.status(status).body(Map.of("status", statusVal, "mensaje", mensaje));
    }

    // 2. Nuevo método auxiliar para el éxito (Acepta 3 parámetros usando Map.of)
    private ResponseEntity<Map<String, String>> respuestaExito(String username, String idBrazo) {
        return ResponseEntity.ok(Map.of(
            "status", "success", 
            "mensaje", "Bienvenido " + username,
            "id_brazo", idBrazo
        ));
    }
}