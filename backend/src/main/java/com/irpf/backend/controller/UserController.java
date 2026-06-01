package com.irpf.backend.controller;

import com.irpf.backend.entidades.Usuario;
import com.irpf.backend.repository.UsuarioRepository;
import com.irpf.backend.service.EmailVerificationService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para alta, mantenimiento y baja de usuarios.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final String PASSWORD_ALLOWED_SPECIAL_CHARS = "!@#$%^&*()-_+={}[]|;:«‘<>.,?/";
    private static final String PASSWORD_POLICY_MESSAGE = "La contrasena debe tener al menos 12 caracteres, incluir al menos una mayuscula, una minuscula, un numero y un caracter especial permitido (!, @, #, $, %, ^, &, *, (, ), -, _, +, =, {, }, [, ], |, ;, :, «, ‘, <, >, ,, ., ?, /).";

    private static final Logger logger = LogManager.getLogger(UserController.class);
    private static final Logger auditLogger = LogManager.getLogger("BACKEND_AUDIT");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    /**
     * Recupera todos los usuarios registrados.
     *
     * @return lista de usuarios
     */
    @GetMapping("/all")
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Crea un usuario, valida la contrasena y envia correo de verificacion.
     *
     * @param data datos de alta de usuario
     * @return mensaje de resultado de la operacion
     */
    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody Map<String, String> data) {
        String usuarioNombre = getFirstNonBlank(data.get("usuario"), data.get("name"));
        String email = data.get("email");
        String password = data.get("password");
        String baseUrl = data.get("baseUrl");
        String esadmin = "N";
        String esverificado = "N";
        Integer numAccErroneos = 0;

        if (usuarioNombre == null || email == null || password == null) {
            return ResponseEntity.badRequest().body("Faltan campos requeridos");
        }

        if (!isSecurePassword(password)) {
            return ResponseEntity.badRequest().body(PASSWORD_POLICY_MESSAGE);
        }

        if (usuarioRepository.findByUsuario(usuarioNombre).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuario ya existe");
        }

        if (usuarioRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuario con ese email ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setUsuario(usuarioNombre);
        usuario.setEmail(email);
        usuario.setPassword(password);
        usuario.setEsadmin(esadmin);
        usuario.setEsverificado(esverificado);
        usuario.setNumAccErroneos(numAccErroneos);
        usuarioRepository.saveAndFlush(usuario);

        auditLogger.info("CREAR_USUARIO | idUsuario={} | username={} | email={}",
                usuario.getId(), usuarioNombre, email);
        try {
            emailVerificationService.sendVerificationEmail(usuario, baseUrl);
            return ResponseEntity.ok(
                    "Se ha realizado el envío de un correo para la verificación del correo electrónico, acceda a su correo y siga las instrucciones para confirmar la dirección");
        } catch (Exception ex) {
            logger.error("Usuario creado pero sin envío de correo de verificación. id={}, email={}",
                    usuario.getId(), usuario.getEmail(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Usuario creado, pero no se pudo enviar el correo de verificación. Use la opción de reenvío desde el login.");
        }
    }

    /**
     * Actualiza campos editables de un usuario existente.
     *
     * @param data datos de usuario a modificar
     * @return mensaje de resultado
     */
    @SuppressWarnings("null")
    @PostMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody Map<String, Object> data) {
        Long idLong = parseLongValue(data.get("id"));
        if (idLong == null) {
            return ResponseEntity.badRequest().body("Falta el id del usuario");
        }
        Optional<Usuario> userOpt = usuarioRepository.findById(idLong);
        if (userOpt.isEmpty()) {
            logger.warn("Actualizacion rechazada: usuario no encontrado. id={}", idLong);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuario usuario = userOpt.get();
        if (data.containsKey("usuario")) {
            usuario.setUsuario(parseStringValue(data.get("usuario")));
        }
        if (data.containsKey("name")) {
            usuario.setUsuario(parseStringValue(data.get("name")));
        }
        if (data.containsKey("email")) {
            usuario.setEmail(parseStringValue(data.get("email")));
        }
        if (data.containsKey("nombre")) {
            usuario.setUsuario(parseStringValue(data.get("nombre")));
        }
        if (data.containsKey("password")) {
            usuario.setPassword(parseStringValue(data.get("password")));
        }
        if (data.containsKey("esadmin")) {
            usuario.setEsadmin(parseFlagValue(data.get("esadmin")));
        }
        if (data.containsKey("esverificado")) {
            usuario.setEsverificado(parseFlagValue(data.get("esverificado")));
        }
        if (data.containsKey("esVerificado")) {
            usuario.setEsverificado(parseFlagValue(data.get("esVerificado")));
        }
        if (data.containsKey("numAccErroneos")) {
            Integer parsedNumAccErroneos = parseIntegerValue(data.get("numAccErroneos"));
            if (parsedNumAccErroneos == null || parsedNumAccErroneos < 0) {
                return ResponseEntity.badRequest().body("numAccErroneos debe ser un numero entero mayor o igual a 0");
            }
            usuario.setNumAccErroneos(parsedNumAccErroneos);
        }

        usuarioRepository.save(usuario);
        logger.info("Usuario actualizado: id={}, username={}", usuario.getId(), usuario.getUsuario());
        auditLogger.info("ACTUALIZAR_USUARIO | idUsuario={} | username={}", usuario.getId(), usuario.getUsuario());
        return ResponseEntity.ok("Usuario actualizado");
    }

    /**
     * Convierte un valor generico a {@link Long}.
     *
     * @param value valor de entrada
     * @return valor convertido o null
     */
    private Long parseLongValue(Object value) {
        if (value == null) {
            return null;
        }
        return Long.parseLong(String.valueOf(value).trim());
    }

    /**
     * Convierte un valor generico a texto normalizado.
     *
     * @param value valor de entrada
     * @return cadena recortada o null
     */
    private String parseStringValue(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value).trim();
    }

    /**
     * Devuelve el primer texto informado y no vacio de una lista de candidatos.
     *
     * @param values valores candidatos
     * @return primer valor valido o null si ninguno aplica
     */
    private String getFirstNonBlank(String... values) {
        for (String value : values) {
            if (value != null) {
                String normalized = value.trim();
                if (!normalized.isEmpty()) {
                    return normalized;
                }
            }
        }
        return null;
    }

    /**
     * Convierte un valor generico a {@link Integer} para contadores.
     *
     * @param value valor de entrada
     * @return entero convertido, 0 si viene vacio o null si no es valido
     */
    private Integer parseIntegerValue(Object value) {
        if (value == null) {
            return null;
        }

        String normalized = String.valueOf(value).trim();
        if (normalized.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Normaliza distintos formatos booleanos al indicador S/N.
     *
     * @param value valor de entrada
     * @return S para verdadero; N para falso o no informado
     */
    private String parseFlagValue(Object value) {
        if (value == null) {
            return "N";
        }

        String normalized = String.valueOf(value).trim();
        if ("S".equalsIgnoreCase(normalized) || "TRUE".equalsIgnoreCase(normalized) || "1".equals(normalized)) {
            return "S";
        }
        return "N";
    }

    /**
     * Comprueba la politica de contrasena exigida por la aplicacion.
     *
     * @param password contrasena en texto plano
     * @return true si cumple longitud y complejidad requeridas
     */
    private boolean isSecurePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasAllowedSpecial = false;

        for (char character : password.toCharArray()) {
            if (Character.isUpperCase(character)) {
                hasUppercase = true;
                continue;
            }
            if (Character.isLowerCase(character)) {
                hasLowercase = true;
                continue;
            }
            if (Character.isDigit(character)) {
                hasDigit = true;
                continue;
            }
            if (PASSWORD_ALLOWED_SPECIAL_CHARS.indexOf(character) >= 0) {
                hasAllowedSpecial = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit && hasAllowedSpecial;
    }

    /**
     * Cambia la contrasena de un usuario tras verificar la actual.
     *
     * @param data datos con usuario, contrasena actual y nueva contrasena
     * @return mensaje de resultado del cambio
     */
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String currentPassword = data.get("currentPassword");
        String newPassword = data.get("newPassword");

        if (username == null || currentPassword == null || newPassword == null) {
            logger.warn("Cambio de contrasena rechazado: campos incompletos. username={}", username);
            return ResponseEntity.badRequest().body("Faltan campos requeridos");
        }

        Optional<Usuario> userOpt = usuarioRepository.findByUsuario(username);
        if (userOpt.isEmpty()) {
            logger.warn("Cambio de contrasena rechazado: usuario no encontrado. username={}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuario usuario = userOpt.get();
        if (!usuario.getPassword().equals(currentPassword)) {
            logger.warn("Cambio de contrasena rechazado: password actual incorrecta. id={}, username={}",
                    usuario.getId(), username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Contraseña actual incorrecta");
        }

        usuario.setPassword(newPassword);
        usuarioRepository.save(usuario);
        logger.info("Contrasena actualizada: id={}, username={}", usuario.getId(), username);
        auditLogger.info("CAMBIAR_PASSWORD | idUsuario={} | username={}", usuario.getId(), username);

        return ResponseEntity.ok("Contraseña actualizada exitosamente");
    }

    /**
     * Elimina un usuario por identificador.
     *
     * @param id identificador de usuario
     * @return mensaje de resultado
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        Optional<Usuario> userOpt = usuarioRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuario usuario = userOpt.get();
        usuarioRepository.deleteById(id);
        auditLogger.info("ELIMINAR_USUARIO | idUsuario={} | username={}", id, usuario.getUsuario());
        return ResponseEntity.ok("Usuario eliminado");
    }
}
