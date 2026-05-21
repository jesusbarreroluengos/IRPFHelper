package com.irpf.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuracion del serializador JSON principal de la aplicacion.
 */
@Configuration
public class JacksonConfig {

    /**
     * Crea el {@link ObjectMapper} principal usado por Spring MVC para serializar
     * y deserializar payloads JSON.
     *
     * @return mapper configurado con soporte de fechas Java Time y tolerante a proxies vacios
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Deshabilitar fallo en beans vacíos (para proxies de Hibernate)
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        return mapper;
    }



}
