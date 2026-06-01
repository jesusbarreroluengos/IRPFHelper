package com.irpf.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

/**
 * Configuracion global de CORS para permitir la comunicacion del frontend
 * con los endpoints REST del backend durante la ejecucion de la aplicacion.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    /**
     * Registra un configurador MVC que habilita origenes, metodos y cabeceras CORS.
     *
     * @return instancia de {@link WebMvcConfigurer} con el mapeo CORS aplicado
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins.split(","))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
