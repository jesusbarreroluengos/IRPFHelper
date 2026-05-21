package com.irpf.backend.service;

import com.irpf.backend.entidades.ContratoPersona;
import com.irpf.backend.entidades.PuestoTipo;
import com.irpf.backend.entidades.Simulacion;
import com.irpf.backend.model.ApiResponse;
import com.irpf.backend.repository.ContratoPersonaRepository;
import com.irpf.backend.repository.PuestoTipoRepository;
import com.irpf.backend.repository.SimulacionRepository;
import com.irpf.backend.repository.PersonaSimuladaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Servicio de orquestacion para ejecutar simulaciones de retribuciones e IRPF.
 */
@Service
public class SimulacionRetribucionesService {
    private static final Logger logger = LogManager.getLogger(SimulacionRetribucionesService.class);

    private final PersonaSimuladaRepository personaSimuladaRepository;
    private final PuestoTipoRepository puestoTipoRepository;
    private final ContratoPersonaRepository contratoPersonaRepository;
    private final SimulacionRepository simulacionRepository;
    private final CalculoRetribucionesService calculoRetribucionesService;
    private final SimulacionIrpfCalculator simulacionIrpfCalculator;

    public SimulacionRetribucionesService(PersonaSimuladaRepository personaSimuladaRepository,
                                          PuestoTipoRepository puestoTipoRepository,
                                          ContratoPersonaRepository contratoPersonaRepository,
                                          SimulacionRepository simulacionRepository,
                                          CalculoRetribucionesService calculoRetribucionesService,
                                          SimulacionIrpfCalculator simulacionIrpfCalculator) {
        this.personaSimuladaRepository = personaSimuladaRepository;
        this.puestoTipoRepository = puestoTipoRepository;
        this.contratoPersonaRepository = contratoPersonaRepository;
        this.simulacionRepository = simulacionRepository;
        this.calculoRetribucionesService = calculoRetribucionesService;
        this.simulacionIrpfCalculator = simulacionIrpfCalculator;
    }

    /**
     * Ejecuta una simulacion completa para persona y ejercicio, persistiendo resultados.
     *
     * @param idPersona identificador de persona simulada
     * @param ejercicio ejercicio fiscal objetivo
     * @param impBrutoAbonado importe bruto ya abonado en el ejercicio
     * @param impRetencionesPracticadas retenciones IRPF ya practicadas
     * @param impGastosRealizados gastos ya realizados
     * @param fechaHastaAbonado fecha hasta la que existen importes abonados
     * @return respuesta con detalle mensual, agregados e IRPF calculado
     */
    public ApiResponse simular(Long idPersona,
                               Integer ejercicio,
                               BigDecimal impBrutoAbonado,
                               BigDecimal impRetencionesPracticadas,
                               BigDecimal impGastosRealizados,
                               LocalDate fechaHastaAbonado) {
        validarPersona(idPersona);
        validarPuestos(idPersona);
        List<ContratoPersona> contratos = contratoPersonaRepository.findByIdPersonaOrderByFechaDesdeAsc(idPersona);
        validarContratosGenerales(contratos);
        validarContratosEjercicio(contratos, ejercicio);

        int mesInicio = calcularMesInicio(fechaHastaAbonado, ejercicio);
        List<ResultadoMensualDTO> mesesSimulados = new ArrayList<>();
        BigDecimal totalBrutoPend = BigDecimal.ZERO;
        BigDecimal totalCotizPend = BigDecimal.ZERO;

        for (int mes = mesInicio; mes <= 12; mes++) {
            LocalDate mesCalculo = LocalDate.of(ejercicio, mes, 1);
            CalculoRetribucionesService.ResultadoMensual res = calculoRetribucionesService.calcularRetribucionesMensuales(idPersona, mesCalculo);
            ResultadoMensualDTO dto = new ResultadoMensualDTO(
                    mes,
                    nombreMes(mes),
                    res.getImporteBrutoTotalMes().setScale(2, RoundingMode.HALF_UP),
                    res.getImporteCotizadoTotalMes().setScale(2, RoundingMode.HALF_UP)
            );
            mesesSimulados.add(dto);
            totalBrutoPend = totalBrutoPend.add(dto.importeBrutoMes());
            totalCotizPend = totalCotizPend.add(dto.importeCotizadoMes());
        }

        Simulacion simulacion = new Simulacion();
        simulacion.setIdPersona(idPersona);
        simulacion.setEjercicio(ejercicio);
        simulacion.setImpBrutoAbonado(impBrutoAbonado != null ? impBrutoAbonado : BigDecimal.ZERO);
        simulacion.setImpRetencionesPracticadas(impRetencionesPracticadas != null ? impRetencionesPracticadas : BigDecimal.ZERO);
        simulacion.setImpGastosRealizados(impGastosRealizados != null ? impGastosRealizados : BigDecimal.ZERO);
        simulacion.setImpBrutoPendiente(totalBrutoPend.setScale(2, RoundingMode.HALF_UP));
        simulacion.setImpGastosPendiente(totalCotizPend.setScale(2, RoundingMode.HALF_UP));
        simulacion.setFechaHastaAbonado(fechaHastaAbonado);
        simulacion.setPorcIrpf(BigDecimal.ZERO);
        Simulacion guardada = simulacionRepository.save(simulacion);

        SimulacionIrpfCalculator.CalculoIrpfResultado resultadoIrpf =
                simulacionIrpfCalculator.calcularYActualizarIrpf(guardada.getIdSimulacion());
        BigDecimal porcIrpf = resultadoIrpf.tipoRetencion();
        logger.info("Simulacion IRPF ejecutada: idSimulacion={}, idPersona={}, ejercicio={}, porcentajeIrpf={}",
                guardada.getIdSimulacion(), idPersona, ejercicio, porcIrpf);

        Map<String, Object> payload = new HashMap<>();
        payload.put("meses", mesesSimulados);
        payload.put("totalBrutoPendiente", totalBrutoPend.setScale(2, RoundingMode.HALF_UP));
        payload.put("totalGastosPendiente", totalCotizPend.setScale(2, RoundingMode.HALF_UP));
        payload.put("porcIrpf", porcIrpf);
        payload.put("importeBrutoAnual", resultadoIrpf.importeBrutoAnual());
        payload.put("importeRetencionesAnual", resultadoIrpf.importeRetencionesAnual());
        payload.put("idSimulacion", guardada.getIdSimulacion());
        return new ApiResponse(true, "Simulación ejecutada", payload);
    }

    /**
     * Verifica que la persona simulada exista y sea valida.
     *
     * @param idPersona identificador de persona
     */
    private void validarPersona(Long idPersona) {
        if (idPersona == null || idPersona <= 0 || personaSimuladaRepository.findById(idPersona).isEmpty()) {
            throw new IllegalArgumentException("Debe introducir los datos de la persona simulada para realizar la simulación, crear el puesto o puestos tipos y los contratos previstos para el ejercicio");
        }
    }

    /**
     * Verifica que la persona tenga al menos un puesto tipo configurado.
     *
     * @param idPersona identificador de persona simulada
     */
    private void validarPuestos(Long idPersona) {
        List<PuestoTipo> puestos = puestoTipoRepository.findByIdPersonaOrderByNomPuestoAsc(idPersona);
        if (puestos == null || puestos.isEmpty()) {
            throw new IllegalArgumentException("Debe crear el puesto o puestos tipos y los contratos previstos para el ejercicio");
        }
    }

    /**
     * Verifica que existan contratos para la simulacion.
     *
     * @param contratos contratos recuperados para la persona
     */
    private void validarContratosGenerales(List<ContratoPersona> contratos) {
        if (contratos == null || contratos.isEmpty()) {
            throw new IllegalArgumentException("Debe crear los contratos previstos para el ejercicio");
        }
    }

    /**
     * Verifica que exista al menos un contrato con solape en el ejercicio indicado.
     *
     * @param contratos contratos de la persona
     * @param ejercicio ejercicio a simular
     */
    private void validarContratosEjercicio(List<ContratoPersona> contratos, Integer ejercicio) {
        boolean hayContratoEnEjercicio = contratos.stream()
                .anyMatch(c -> solapaConEjercicio(c, ejercicio));
        if (!hayContratoEnEjercicio) {
            throw new IllegalArgumentException("No se han introducido contratos en el ejercicio seleccionado");
        }
    }

    /**
     * Comprueba si un contrato solapa con el ejercicio fiscal indicado.
     *
     * @param contrato contrato evaluado
     * @param ejercicio ejercicio fiscal
     * @return true si el contrato tiene dias dentro del ejercicio
     */
    private boolean solapaConEjercicio(ContratoPersona contrato, Integer ejercicio) {
        LocalDate inicio = contrato.getFechaDesde();
        LocalDate fin = contrato.getFechaHasta() != null ? contrato.getFechaHasta() : LocalDate.of(ejercicio, 12, 31);
        LocalDate inicioEj = LocalDate.of(ejercicio, 1, 1);
        LocalDate finEj = LocalDate.of(ejercicio, 12, 31);
        return !fin.isBefore(inicioEj) && !inicio.isAfter(finEj);
    }

    /**
     * Calcula el primer mes a simular segun la fecha de importes ya abonados.
     *
     * @param fechaHastaAbonado fecha tope de importes abonados
     * @param ejercicio ejercicio fiscal
     * @return numero de mes de inicio de simulacion
     */
    private int calcularMesInicio(LocalDate fechaHastaAbonado, Integer ejercicio) {
        if (fechaHastaAbonado == null) {
            return 1;
        }
        if (!Objects.equals(fechaHastaAbonado.getYear(), ejercicio)) {
            return 1;
        }
        int mes = fechaHastaAbonado.getMonthValue() + 1;
        return Math.max(1, Math.min(mes, 12));
    }

    /**
     * Convierte un numero de mes a su nombre en espanol.
     *
     * @param mes numero de mes entre 1 y 12
     * @return nombre del mes en locale es-ES
     */
    private String nombreMes(int mes) {
        return Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
    }

    /**
     * Resultado mensual de la simulacion para exponer en la respuesta.
     *
     * @param mesNumero numero de mes
     * @param mesNombre nombre de mes
     * @param importeBrutoMes importe bruto del mes
     * @param importeCotizadoMes importe cotizado del mes
     */
    public record ResultadoMensualDTO(int mesNumero, String mesNombre, BigDecimal importeBrutoMes, BigDecimal importeCotizadoMes) {}
}


