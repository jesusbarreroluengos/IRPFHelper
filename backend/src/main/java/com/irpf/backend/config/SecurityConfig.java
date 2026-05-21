package com.irpf.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

/**
 * Configuracion de seguridad HTTP para exponer la API REST del proyecto.
 */
@Configuration
public class SecurityConfig {

    /**
     * Construye la cadena de filtros de seguridad con CORS activado y CSRF deshabilitado
     * para llamadas desde el frontend.
     *
     * @param http objeto de configuracion de seguridad de Spring
     * @return cadena de filtros configurada
     * @throws Exception si ocurre un error al construir la configuracion
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) //   activa CORS usando tu CorsConfig
            .csrf(csrf -> csrf.disable())    //   desactiva CSRF para APIs (importante si usas POST desde frontend)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .anyRequest().permitAll() // permitir resto para evitar 403 en nuevos endpoints
            );

        return http.build();
    }
}
