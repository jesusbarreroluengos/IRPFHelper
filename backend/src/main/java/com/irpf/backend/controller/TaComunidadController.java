package com.irpf.backend.controller;

import com.irpf.backend.entidades.TaComunidad;
import com.irpf.backend.repository.TaComunidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para consulta de comunidades autonomas disponibles.
 */
@RestController
@RequestMapping("/api/comunidades")
public class TaComunidadController {

    @Autowired
    private TaComunidadRepository taComunidadRepository;

    /**
     * Recupera el catalogo completo de comunidades.
     *
     * @return listado de comunidades registradas en base de datos
     */
    @GetMapping("/all")
    public List<TaComunidad> getAllComunidades() {
        return taComunidadRepository.findAll();
    }
}
