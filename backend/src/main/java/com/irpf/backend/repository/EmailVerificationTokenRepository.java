package com.irpf.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.irpf.backend.entidades.TokenVerificacionEmail;

/**
 * Repositorio de tokens de verificacion de correo electronico.
 */
public interface EmailVerificationTokenRepository extends JpaRepository<TokenVerificacionEmail, Long> {

    /**
     * Busca un token de verificacion por su valor unico.
     *
     * @param token valor del token
     * @return token encontrado si existe
     */
    Optional<TokenVerificacionEmail> findByToken(String token);

    /**
     * Recupera tokens de un usuario filtrando por estado de uso.
     *
     * @param usuarioId identificador de usuario
     * @param indUsado indicador de uso S/N
     * @return lista de tokens que cumplen el filtro
     */
    List<TokenVerificacionEmail> findByUsuarioIdAndIndUsado(Long usuarioId, String indUsado);
}
