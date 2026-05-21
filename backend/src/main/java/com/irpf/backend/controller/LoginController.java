package com.irpf.backend.controller;

import com.irpf.backend.entidades.Usuario;
import com.irpf.backend.repository.UsuarioRepository;
import com.irpf.backend.service.EmailVerificationService;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de autenticacion y verificacion de correo de usuarios.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class LoginController {

    private static final int MAX_INTENTOS_ERRONEOS = 10;
    private static final String MENSAJE_USUARIO_BLOQUEADO = "Usuario bloqueado. Contacte con su administrador.";

    private static final Logger logger = LogManager.getLogger(LoginController.class);
    private static final Logger auditLogger = LogManager.getLogger("BACKEND_AUDIT");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    /**
     * Valida credenciales y controla intentos erroneos de acceso.
     *
     * @param credentials mapa con usuario y contrasena
     * @return respuesta de login exitoso o motivo de rechazo
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            logger.warn("Intento de login rechazado: credenciales incompletas");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Credenciales incompletas"));
        }

        if (username.isBlank() || password.isBlank()) {
            logger.warn("Intento de login rechazado: usuario o contrasena vacios. username={}", username);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Usuario o contrasena vacios"));
        }

        username = username.trim();
        password = password.trim();

        Optional<Usuario> userOpt = usuarioRepository.findByUsuario(username);
        if (userOpt.isEmpty()) {
            logger.warn("Login fallido: usuario no encontrado. username={}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Usuario no encontrado"));
        }

        Usuario usuario = userOpt.get();
        int numAccErroneos = normalizeNumAccErroneos(usuario.getNumAccErroneos());
        if (numAccErroneos >= MAX_INTENTOS_ERRONEOS) {
            logger.warn("Login bloqueado: usuario con cuenta bloqueada. idUsuario={}, username={}",
                    usuario.getId(), username);
            return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of("message", MENSAJE_USUARIO_BLOQUEADO));
        }

        if (usuario.getPassword() == null || !usuario.getPassword().equals(password)) {
            int nuevoNumAccErroneos = numAccErroneos + 1;
            usuario.setNumAccErroneos(nuevoNumAccErroneos);
            usuarioRepository.save(usuario);

            logger.warn("Login fallido: contrasena incorrecta. idUsuario={}, username={}", usuario.getId(), username);

            if (nuevoNumAccErroneos >= MAX_INTENTOS_ERRONEOS) {
                logger.warn("Usuario bloqueado por exceso de intentos fallidos. idUsuario={}, username={}, intentos={}",
                        usuario.getId(), username, nuevoNumAccErroneos);
                return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of("message", MENSAJE_USUARIO_BLOQUEADO));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Contrasena incorrecta"));
        }

        if (numAccErroneos > 0) {
            usuario.setNumAccErroneos(0);
            usuarioRepository.save(usuario);
        }

        if (!"S".equalsIgnoreCase(usuario.getEsverificado())) {
            logger.warn("Login bloqueado por correo no verificado. idUsuario={}, username={}", usuario.getId(), username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of(
                            "message", "Debe verificar la direccion de correo antes de acceder.",
                            "code", "EMAIL_NOT_VERIFIED"));
        }

        logger.info("Login correcto: idUsuario={}, username={}", usuario.getId(), username);
        auditLogger.info("LOGIN | idUsuario={} | username={}", usuario.getId(), username);
        return ResponseEntity.ok(Map.of("message", "Login exitoso"));
    }

    /**
     * Reenvia el correo de verificacion tras validar usuario y credenciales.
     *
     * @param data datos de autenticacion y URL base opcional
     * @return estado del reenvio del correo
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");
        String baseUrl = data.get("baseUrl");

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Usuario y contrasena son obligatorios"));
        }

        Optional<Usuario> userOpt = usuarioRepository.findByUsuario(username.trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado"));
        }

        Usuario usuario = userOpt.get();
        if (!password.trim().equals(usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Contrasena incorrecta"));
        }

        if ("S".equalsIgnoreCase(usuario.getEsverificado())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "La direccion de correo ya esta verificada"));
        }

        try {
            emailVerificationService.sendVerificationEmail(usuario, baseUrl);
            return ResponseEntity.ok(Map.of("message",
                    "Se ha realizado el envio de un correo para la verificacion del correo electronico, acceda a su correo y siga las instrucciones para confirmar la direccion"));
        } catch (Exception ex) {
            logger.error("No se pudo reenviar correo de verificacion. idUsuario={}, username={}",
                    usuario.getId(), usuario.getUsuario(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "No se pudo enviar el correo de verificacion. Intentelo mas tarde."));
        }
    }

    /**
     * Verifica un token de correo recibido desde el enlace de activacion.
     *
     * @param token token de verificacion de email
     * @return resultado de verificacion con estado verificado S/N
     */
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        EmailVerificationService.VerificationOutcome outcome = emailVerificationService.verifyToken(token);

        if (outcome.success()) {
            return ResponseEntity.ok(Map.of("message", outcome.message(), "verified", "S"));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", outcome.message(), "verified", "N"));
    }

    /**
     * Normaliza el contador de intentos fallidos para evitar valores nulos.
     *
     * @param numAccErroneos valor de intentos fallidos del usuario
     * @return cero cuando es null o el valor original en otro caso
     */
    private int normalizeNumAccErroneos(Integer numAccErroneos) {
        return numAccErroneos == null ? 0 : numAccErroneos;
    }
}
