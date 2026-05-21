package com.irpf.backend.repository;

import com.irpf.backend.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositorio de usuarios del sistema.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de login.
     *
     * @param usuario nombre de usuario
     * @return usuario encontrado si existe
     */
    Optional<Usuario> findByUsuario(String usuario);

    /**
     * Busca un usuario por direccion de correo electronico.
     *
     * @param email correo del usuario
     * @return usuario encontrado si existe
     */
    Optional<Usuario> findByEmail(String email);
}
