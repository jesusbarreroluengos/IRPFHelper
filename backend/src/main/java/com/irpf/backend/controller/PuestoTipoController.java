package com.irpf.backend.controller;

import com.irpf.backend.entidades.*;
import com.irpf.backend.model.ApiResponse;
import com.irpf.backend.repository.*;
import com.irpf.backend.service.CalculoRetribucionesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Controlador REST para gestionar puestos tipo de personas simuladas,
 * realizar validaciones y calcular importes retributivos asociados.
 */
@RestController
@RequestMapping("/api/puestos-tipo")
public class PuestoTipoController {

    private static final String NOMBRE_REGEX = "^[A-Z0-9ÁÉÍÓÚÜÑ\\-_]+$";
    private static final double IMPORTE_MAXIMO = 25_000.0;
    private static final Logger auditLogger = LogManager.getLogger("BACKEND_AUDIT");

    @Autowired
    private PuestoTipoRepository puestoTipoRepository;

    /**
     * Repositorio de personas simuladas usado para validar existencia y
     * recuperar datos de contexto (por ejemplo, comunidad por defecto).
     */
    @Autowired
    private PersonaSimuladaRepository personaSimuladaRepository;
    @Autowired
    private TaSueldoRepository taSueldoRepository;
    @Autowired
    private TaTrienioRepository taTrienioRepository;
    @Autowired
    private TaSexenioRepository taSexenioRepository;
    @Autowired
    private TaDestinoRepository taDestinoRepository;
    @Autowired
    private TaEspecificoRepository taEspecificoRepository;
    @Autowired
    private TaEstudiosRepository taEstudiosRepository;
    @Autowired
    private TaJornadaRepository taJornadaRepository;
    @Autowired
    private TaComunidadRepository taComunidadRepository;
    @Autowired
    private CalculoRetribucionesService calculoRetribucionesService;

    /**
     * Obtiene todos los puestos tipo de una persona simulada ordenados por nombre.
     */
    @GetMapping("/persona/{idPersona}")
    public ResponseEntity<?> getPuestosByPersona(@PathVariable Long idPersona) {
        if (idPersona == null || idPersona <= 0) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Id de persona inválido"));
        }
        List<PuestoTipo> puestos = puestoTipoRepository.findByIdPersonaOrderByNomPuestoAsc(idPersona);
        return ResponseEntity.ok(puestos);
    }

    /**
     * Obtiene un puesto tipo por su id.
     */
    @GetMapping("/{idPuesto}")
    public ResponseEntity<?> getPuestoById(@PathVariable Long idPuesto) {
        return puestoTipoRepository.findById(idPuesto)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Puesto tipo no encontrado")));
    }

    /**
     * Devuelve los tipos de estudio disponibles.
     */
    @GetMapping("/estudios")
    public List<TaEstudios> getEstudios() {
        return taEstudiosRepository.findAll();
    }

    /**
     * Devuelve las jornadas configuradas.
     */
    @GetMapping("/jornadas")
    public List<TaJornada> getJornadas() {
        return taJornadaRepository.findAll();
    }

    /**
     * Devuelve el importe específico general docente por año actual y código de estudio.
     */
    @GetMapping("/especifico-default/{codEstudio}")
    public ResponseEntity<?> getImporteEspecifico(@PathVariable String codEstudio,
                                                  @RequestParam(name = "idComunidad", required = false) Integer idComunidad,
                                                  @RequestParam(name = "anio", required = false) Integer anio) {
        String cod = normalizarCodEstudio(codEstudio);
        if (cod == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Código de estudio inválido"));
        }
        int anioCalculo = anio != null ? anio : LocalDate.now().getYear();
        return taEspecificoRepository.findByAnioAndCodEstudioAndIdComunidad(anioCalculo, cod, idComunidad != null ? idComunidad : 1)
                .<ResponseEntity<?>>map(v -> ResponseEntity.ok(new ApiResponse(true, "Importe encontrado", v.getImporte())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "No se encontró importe específico para el año " + anioCalculo)));
    }

    /**
     * Calcula el importe bruto con los datos enviados sin persistirlos.
     */
    @PostMapping("/calcular")
    public ResponseEntity<ApiResponse> calcularImporte(@RequestBody Map<String, Object> data) {
        try {
            Boolean validarDuplicado = false;

            PuestoTipo puesto = construirPuestoDesdeMapa(data, null);
            String error = validarPuesto(puesto, null,validarDuplicado);
            if (error != null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, error));
            }
            double importe = calcularImporteBruto(puesto);
            return ResponseEntity.ok(new ApiResponse(true, "Importe calculado", importe));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "No se pudo calcular el importe: " + e.getMessage()));
        }
    }

    /**
     * Calcula los importes detallados (bruto mes, paga extra, base de cotización) usando el servicio de retribuciones.
     */
    @PostMapping("/calcular-detalle")
    public ResponseEntity<ApiResponse> calcularImportesDetalle(@RequestBody Map<String, Object> data) {
        try {
            PuestoTipo puesto = construirPuestoDesdeMapa(data, null);
            String error = validarPuesto(puesto, null, false);
            if (error != null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, error));
            }
            int ejercicio = data.containsKey("ejercicio") ? parseInteger(data.get("ejercicio"), LocalDate.now().getYear()) : LocalDate.now().getYear();
            CalculoRetribucionesService.ImportesPuesto importes = calculoRetribucionesService.obtenerImportesPuestoTipo(ejercicio, puesto);
            Map<String, Object> payload = new HashMap<>();
            payload.put("importeBrutoMes", importes.getImporteBrutoMes());
            payload.put("importeExtra", importes.getImporteExtra());
            payload.put("importeBaseCotizacion", importes.getImporteBaseCotizacion());
            payload.put("importeSueldo", importes.getImporteSueldo());
            payload.put("importeComplementoDestino", importes.getImporteComplementoDestino());
            payload.put("importeTrienios", importes.getImporteTrienios());
            payload.put("importeSexenios", importes.getImporteSexenios());
            payload.put("importeSueldoExtra", importes.getImporteSueldoExtra());
            payload.put("importeTrieniosExtra", importes.getImporteTrieniosExtra());
            return ResponseEntity.ok(new ApiResponse(true, "Importes calculados", payload));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "No se pudo calcular los importes: " + e.getMessage()));
        }
    }

    /**
     * Crea un nuevo puesto tipo.
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPuesto(@RequestBody Map<String, Object> data) {
        try {
            PuestoTipo puesto = construirPuestoDesdeMapa(data, new PuestoTipo());
            String errorValidacion = validarPuesto(puesto, null,true);
            if (errorValidacion != null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, errorValidacion));
            }

            puestoTipoRepository.saveAndFlush(puesto);
            auditLogger.info("CREAR_PUESTO_TIPO | idPuesto={} | idPersona={} | nomPuesto={}",
                    puesto.getIdPuestoTipo(), puesto.getIdPersona(), puesto.getNomPuesto());
            return ResponseEntity.ok(new ApiResponse(true, "Puesto tipo creado exitosamente", puesto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al crear el puesto tipo: " + e.getMessage()));
        }
    }

    /**
     * Actualiza un puesto tipo existente.
     */
    @PutMapping("/update/{idPuesto}")
    public ResponseEntity<ApiResponse> updatePuesto(@PathVariable Long idPuesto, @RequestBody Map<String, Object> data) {
        try {
            Optional<PuestoTipo> puestoOpt = puestoTipoRepository.findById(idPuesto);
            if (puestoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Puesto tipo no encontrado"));
            }
            PuestoTipo existente = puestoOpt.get();
            PuestoTipo puestoActualizado = construirPuestoDesdeMapa(data, existente);
            puestoActualizado.setIdPuestoTipo(idPuesto);

            String error = validarPuesto(puestoActualizado, idPuesto, false);
            if (error != null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, error));
            }

            puestoTipoRepository.save(puestoActualizado);
            return ResponseEntity.ok(new ApiResponse(true, "Puesto tipo actualizado exitosamente", puestoActualizado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al actualizar el puesto tipo: " + e.getMessage()));
        }
    }

    /**
     * Elimina un puesto tipo.
     */
    @DeleteMapping("/delete/{idPuesto}")
    public ResponseEntity<ApiResponse> deletePuesto(@PathVariable Long idPuesto) {
        try {
            Optional<PuestoTipo> puestoOpt = puestoTipoRepository.findById(idPuesto);
            if (puestoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Puesto tipo no encontrado"));
            }
            PuestoTipo puesto = puestoOpt.get();
            puestoTipoRepository.deleteById(idPuesto);
            auditLogger.info("ELIMINAR_PUESTO_TIPO | idPuesto={} | idPersona={} | nomPuesto={}",
                    idPuesto, puesto.getIdPersona(), puesto.getNomPuesto());
            return ResponseEntity.ok(new ApiResponse(true, "Puesto tipo eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al eliminar el puesto tipo: " + e.getMessage()));
        }
    }

    // ===== Métodos privados de soporte =====

    /**
     * Construye o completa una entidad PuestoTipo a partir de un mapa de datos.
     *
     * @param data datos de entrada recibidos en la peticion
     * @param base entidad base para actualizar, o null para crear una nueva
     * @return entidad PuestoTipo con los campos informados aplicados
     */
    private PuestoTipo construirPuestoDesdeMapa(Map<String, Object> data, PuestoTipo base) {
        PuestoTipo puesto = base != null ? base : new PuestoTipo();

        if (data.containsKey("idPersona")) {
            puesto.setIdPersona(parseLong(data.get("idPersona")));
        }
        if (data.containsKey("nomPuesto")) {
            Object nombreObj = data.get("nomPuesto");
            puesto.setNomPuesto(nombreObj != null ? nombreObj.toString() : null);
        }
        if (data.containsKey("codEstudio")) {
            puesto.setCodEstudio(normalizarCodEstudio(asString(data.get("codEstudio"))));
        }
        if (data.containsKey("codJornada")) {
            puesto.setCodJornada(parseInteger(data.get("codJornada"), puesto.getCodJornada()));
        } else if (puesto.getCodJornada() == null) {
            puesto.setCodJornada(1);
        }
        if (data.containsKey("numTrieniosA1")) {
            puesto.setNumTrieniosA1(parseInteger(data.get("numTrieniosA1"), puesto.getNumTrieniosA1()));
        }
        if (data.containsKey("numTrieniosA2")) {
            puesto.setNumTrieniosA2(parseInteger(data.get("numTrieniosA2"), puesto.getNumTrieniosA2()));
        }
        if (data.containsKey("numSexenios")) {
            puesto.setNumSexenios(parseInteger(data.get("numSexenios"), puesto.getNumSexenios()));
        }
        if (data.containsKey("importeEspecDocente")) {
            puesto.setImporteEspecDocente(parseBigDecimal(data.get("importeEspecDocente"), puesto.getImporteEspecDocente()));
        }
        if (data.containsKey("importeOtrosAbonosMes")) {
            puesto.setImporteOtrosAbonosMes(parseBigDecimal(data.get("importeOtrosAbonosMes"), puesto.getImporteOtrosAbonosMes()));
        }
        if (data.containsKey("idComunidad")) {
            puesto.setIdComunidad(parseInteger(data.get("idComunidad"), puesto.getIdComunidad()));
        }
        return puesto;
    }

    /**
     * Ejecuta validaciones de negocio y consistencia sobre un puesto tipo.
     *
     * @param puesto entidad a validar
     * @param idEditando id del puesto en edicion, null en alta
     * @param validarDuplicado true para comprobar duplicados por persona y nombre
     * @return mensaje de error si la validacion falla, null si es valido
     */
    private String validarPuesto(PuestoTipo puesto, Long idEditando, boolean validarDuplicado) {
        if (puesto.getIdPersona() == null || puesto.getIdPersona() <= 0) {
            return "Debe indicar la persona simulada asociada";
        }
        Optional<PersonaSimulada> personaOpt = personaSimuladaRepository.findById(puesto.getIdPersona());
        if (personaOpt.isEmpty()) {
            return "La persona simulada asociada no existe";
        }
        PersonaSimulada persona = personaOpt.get();

        if (puesto.getIdComunidad() == null) {
            puesto.setIdComunidad(persona.getIdComunidad() != null ? persona.getIdComunidad() : 1);
        }
        if (puesto.getIdComunidad() == null || taComunidadRepository.findById(puesto.getIdComunidad()).isEmpty()) {
            return "La comunidad autonoma seleccionada no existe";
        }

        // Nombre
        if (puesto.getNomPuesto() == null || puesto.getNomPuesto().trim().isEmpty()) {
            return "El nombre del puesto es obligatorio";
        }
        String nombreNormalizado = puesto.getNomPuesto().trim().toUpperCase(Locale.ROOT);
        if (!nombreNormalizado.matches(NOMBRE_REGEX)) {
            return "El nombre solo puede contener letras, números, guion medio o guion bajo";
        }
        puesto.setNomPuesto(nombreNormalizado);

        // Cod estudio
        String cod = normalizarCodEstudio(puesto.getCodEstudio());
        if (cod == null) {
            return "Debe seleccionar el tipo de docencia (Primaria, Secundaria, Catedráticos o Formación Profesional)";
        }
        puesto.setCodEstudio(cod);

        // Jornada
        if (puesto.getCodJornada() == null) {
            puesto.setCodJornada(1);
        }
        Optional<TaJornada> jornada = taJornadaRepository.findById(puesto.getCodJornada());
        if (jornada.isEmpty()) {
            return "La jornada seleccionada no existe";
        }

        // Validaciones numéricas
        if (puesto.getNumTrieniosA1() == null || puesto.getNumTrieniosA1() < 0 || puesto.getNumTrieniosA1() > 15) {
            return "El número de trienios A1 debe estar entre 0 y 15";
        }
        if (puesto.getNumTrieniosA2() == null || puesto.getNumTrieniosA2() < 0 || puesto.getNumTrieniosA2() > 15) {
            return "El número de trienios A2 debe estar entre 0 y 15";
        }
        if (puesto.getNumSexenios() == null || puesto.getNumSexenios() < 0 || puesto.getNumSexenios() > 5) {
            return "El número de sexenios debe estar entre 0 y 5";
        }

        if (puesto.getImporteEspecDocente() == null) {
            puesto.setImporteEspecDocente(BigDecimal.ZERO);
        } else if (puesto.getImporteEspecDocente().doubleValue() < 0 || puesto.getImporteEspecDocente().doubleValue() > IMPORTE_MAXIMO) {
            return "El importe específico docente debe ser positivo y menor o igual a " + IMPORTE_MAXIMO;
        }
        if (puesto.getImporteOtrosAbonosMes() != null) {
            if (puesto.getImporteOtrosAbonosMes().doubleValue() < 0 || puesto.getImporteOtrosAbonosMes().doubleValue() > IMPORTE_MAXIMO) {
                return "El importe de otros abonos debe ser positivo y menor o igual a " + IMPORTE_MAXIMO;
            }
        } else {
            puesto.setImporteOtrosAbonosMes(BigDecimal.ZERO);
        }

        if (validarDuplicado) {
            // Unicidad por persona
            Optional<PuestoTipo> duplicado = puestoTipoRepository.findByIdPersonaAndNomPuestoIgnoreCase(puesto.getIdPersona(), puesto.getNomPuesto());
            if (duplicado.isPresent() && (idEditando == null || !duplicado.get().getIdPuestoTipo().equals(idEditando))) {
                return "Ya existe un puesto con ese nombre para el docente seleccionado";
            }
        }


        return null;
    }

    /**
     * Normaliza y valida el codigo de estudio permitido.
     *
     * @param codEstudio codigo recibido
     * @return codigo normalizado o null si no pertenece al conjunto permitido
     */
    private String normalizarCodEstudio(String codEstudio) {
        if (codEstudio == null) return null;
        String cod = codEstudio.trim().toUpperCase(Locale.ROOT);
        if (!cod.equals("P") && !cod.equals("S") && !cod.equals("C") && !cod.equals("F")) {
            return null;
        }
        return cod;
    }

    /**
     * Convierte un valor a Integer devolviendo un valor por defecto si no es valido.
     *
     * @param value valor de entrada
     * @param defaultValue valor por defecto
     * @return valor convertido o valor por defecto
     */
    private Integer parseInteger(Object value, Integer defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Convierte un valor a Long de forma segura.
     *
     * @param value valor de entrada
     * @return valor convertido o null si no es valido
     */
    private Long parseLong(Object value) {
        if (value == null) return null;
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convierte un valor a BigDecimal devolviendo un valor por defecto si falla.
     *
     * @param value valor de entrada
     * @param defaultValue valor por defecto
     * @return valor convertido o valor por defecto
     */
    private BigDecimal parseBigDecimal(Object value, BigDecimal defaultValue) {
        if (value == null) return defaultValue;
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Convierte un valor a texto si existe.
     *
     * @param value valor de entrada
     * @return representacion textual o null
     */
    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    /**
     * Calcula el importe bruto mensual aproximado para un puesto tipo.
     *
     * @param puesto puesto tipo con los datos necesarios de calculo
     * @return importe bruto mensual calculado
     */
    private double calcularImporteBruto(PuestoTipo puesto) {
        double importeBruto = 0.0;
        Integer anioActual = LocalDate.now().getYear();
        Integer idComunidad = puesto.getIdComunidad() != null ? puesto.getIdComunidad() : 1;

        // Sueldo base
        importeBruto += taSueldoRepository.findByAnioAndCodEstudio(anioActual, puesto.getCodEstudio())
                .map(v -> v.getImporte().doubleValue())
                .orElse(0.0);

        // Trienios
        double importeTrienioP = taTrienioRepository.findByAnioAndCodEstudio(anioActual, "P")
                .map(v -> v.getImporte().doubleValue())
                .orElse(0.0);
        double importeTrienioS = taTrienioRepository.findByAnioAndCodEstudio(anioActual, "S")
                .map(v -> v.getImporte().doubleValue())
                .orElse(0.0);
        importeBruto += importeTrienioP * safeInt(puesto.getNumTrieniosA2());
        importeBruto += importeTrienioS * safeInt(puesto.getNumTrieniosA1());

        // Sexenios
        if (puesto.getNumSexenios() != null && puesto.getNumSexenios() > 0) {
            List<TaSexenio> sexenios =
                    taSexenioRepository.findByAnioAndIdComunidadAndNumSexenioLessThanEqualOrderByNumSexenioAsc(
                            anioActual, idComunidad, puesto.getNumSexenios().toString());
            importeBruto += sexenios.stream().mapToDouble(s -> s.getImporte().doubleValue()).sum();
        }

        // Destino
        importeBruto += taDestinoRepository.findByAnioAndCodEstudio(anioActual, puesto.getCodEstudio())
                .map(v -> v.getImporte().doubleValue())
                .orElse(0.0);

        double importeEspecifico = safeDouble(puesto.getImporteEspecDocente());
        if (importeEspecifico == 0.0) {
            importeEspecifico = taEspecificoRepository.findByAnioAndCodEstudioAndIdComunidad(
                            anioActual, puesto.getCodEstudio(), idComunidad)
                    .map(v -> v.getImporte().doubleValue())
                    .orElse(0.0);
        }
        importeBruto += importeEspecifico;
        importeBruto += safeDouble(puesto.getImporteOtrosAbonosMes());

        double factorJornada = taJornadaRepository.findById(puesto.getCodJornada() != null ? puesto.getCodJornada() : 1)
                .map(j -> safeDouble(j.getPorcentaje())) 
                .orElse(1.0) / 100.0;
        importeBruto *= factorJornada;

        return importeBruto;
    }

    /**
     * Devuelve cero si el entero es nulo.
     *
     * @param value valor de entrada
     * @return valor original o cero
     */
    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    /**
     * Devuelve cero si el decimal es nulo.
     *
     * @param value valor de entrada
     * @return valor original en double o cero
     */
    private double safeDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }
}
