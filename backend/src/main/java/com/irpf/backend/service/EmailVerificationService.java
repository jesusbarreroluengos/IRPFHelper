package com.irpf.backend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.irpf.backend.entidades.TokenVerificacionEmail;
import com.irpf.backend.entidades.Usuario;
import com.irpf.backend.repository.EmailVerificationTokenRepository;
import com.irpf.backend.repository.UsuarioRepository;

/**
 * Servicio para gestionar verificacion de cuentas mediante correo electronico.
 */

@Service
public class EmailVerificationService {

    private final JavaMailSender mailSender;
    private final EmailVerificationTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.verification.base-url:http://localhost:4200}")
    private String verificationBaseUrl;

    @Value("${app.verification.token-hours:24}")
    private long tokenDurationHours;

    @Value("${app.mail.from:}")
    private String fromAddress;

    public EmailVerificationService(JavaMailSender mailSender,
            EmailVerificationTokenRepository tokenRepository,
            UsuarioRepository usuarioRepository) {
        this.mailSender = mailSender;
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Envia un correo de verificacion usando la URL base por defecto configurada.
     *
     * @param usuario usuario al que se enviara el correo
     */
    public void sendVerificationEmail(Usuario usuario) {
        sendVerificationEmail(usuario, null);
    }

    /**
     * Envia un correo de verificacion, generando un token unico y enlace de activacion.
     * Antes de emitir un nuevo token invalida los activos del usuario.
     *
     * @param usuario usuario destinatario
     * @param requestedBaseUrl URL base solicitada por cliente para construir el enlace
     */
    public void sendVerificationEmail(Usuario usuario, String requestedBaseUrl) {
        if (usuario == null || usuario.getId() == null || usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("Usuario o correo no valido para verificacion");
        }

        invalidateActiveTokens(usuario.getId());

        String token = UUID.randomUUID().toString();
        TokenVerificacionEmail verificationToken = new TokenVerificacionEmail();
        verificationToken.setToken(token);
        verificationToken.setUsuario(usuario);
        verificationToken.setFechaCaducidad(LocalDateTime.now().plusHours(tokenDurationHours));
        verificationToken.setIndUsado("N");
        tokenRepository.saveAndFlush(verificationToken);

        String verificationLink = buildVerificationLink(token, requestedBaseUrl);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(usuario.getEmail());
        email.setSubject("Verificación de correo de Usuario de IRPF Helper");
        email.setText("Pro favor, pulse en el siguiente link para verificar la cuenta de correo:\n" + verificationLink);
        if (fromAddress != null && !fromAddress.isBlank()) {
            email.setFrom(fromAddress);
        }

        mailSender.send(email);
    }

    /**
     * Verifica un token de correo y actualiza el estado de verificacion del usuario.
     *
     * @param token token recibido desde el enlace de verificacion
     * @return resultado con exito/fallo y mensaje explicativo
     */

    @Transactional
    public VerificationOutcome verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return new VerificationOutcome(false, "El enlace de verificacion no es valido.");
        }

        TokenVerificacionEmail verificationToken = tokenRepository.findByToken(token.trim()).orElse(null);
        if (verificationToken == null) {
            return new VerificationOutcome(false, "El enlace de verificacion no es valido.");
        }

        if ("S".equalsIgnoreCase(verificationToken.getIndUsado())) {
            if ("S".equalsIgnoreCase(verificationToken.getUsuario().getEsverificado())) {
                return new VerificationOutcome(true, "La direccion de correo ya estaba verificada.");
            }
            return new VerificationOutcome(false,
                    "El enlace de verificacion ya no es valido. Solicite un nuevo correo de verificacion.");
        }

        if (verificationToken.getFechaCaducidad().isBefore(LocalDateTime.now())) {
            return new VerificationOutcome(false,
                    "El enlace de verificacion ha caducado. Solicite un nuevo correo de verificacion.");
        }

        Usuario usuario = verificationToken.getUsuario();
        usuario.setEsverificado("S");
        usuarioRepository.save(usuario);

        verificationToken.setIndUsado("S");
        tokenRepository.save(verificationToken);

        invalidateActiveTokens(usuario.getId());

        return new VerificationOutcome(true, "Correo verificado correctamente.");
    }

    /**
     * Invalida todos los tokens no usados asociados a un usuario.
     *
     * @param usuarioId identificador de usuario
     */
    private void invalidateActiveTokens(Long usuarioId) {
        List<TokenVerificacionEmail> tokens = tokenRepository.findByUsuarioIdAndIndUsado(usuarioId, "N");
        for (TokenVerificacionEmail activeToken : tokens) {
            activeToken.setIndUsado("S");
        }
        if (!tokens.isEmpty()) {
            tokenRepository.saveAll(tokens);
        }
    }

    /**
     * Construye la URL de verificacion con el token codificado.
     *
     * @param token token de verificacion
     * @param requestedBaseUrl URL base solicitada por cliente
     * @return enlace completo de verificacion
     */
    private String buildVerificationLink(String token, String requestedBaseUrl) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String baseUrl = resolveBaseUrl(requestedBaseUrl);
        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return normalizedBaseUrl + "/verificar-email?token=" + encodedToken;
    }

    /**
     * Resuelve la URL base aplicable para construir enlaces de verificacion.
     *
     * @param requestedBaseUrl URL enviada por el cliente
     * @return URL valida solicitada o la configurada por defecto
     */
    private String resolveBaseUrl(String requestedBaseUrl) {
        if (requestedBaseUrl != null) {
            String trimmed = requestedBaseUrl.trim();
            if (!trimmed.isBlank() && (trimmed.startsWith("http://") || trimmed.startsWith("https://"))) {
                return trimmed;
            }
        }
        return verificationBaseUrl;
    }

    /**
     * Resultado de una operacion de verificacion de correo.
     *
     * @param success indica si la verificacion se completo correctamente
     * @param message detalle textual del resultado
     */
    public record VerificationOutcome(boolean success, String message) {
    }
}
