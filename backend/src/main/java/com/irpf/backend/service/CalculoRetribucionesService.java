package com.irpf.backend.service;

import com.irpf.backend.entidades.ContratoPersona;
import com.irpf.backend.entidades.PuestoTipo;
import com.irpf.backend.entidades.TaJornada;
import com.irpf.backend.entidades.TaPorcCotiz;
import com.irpf.backend.repository.ContratoPersonaRepository;
import com.irpf.backend.repository.PuestoTipoRepository;
import com.irpf.backend.repository.TaDestinoRepository;
import com.irpf.backend.repository.TaEspecificoRepository;
import com.irpf.backend.repository.TaJornadaRepository;
import com.irpf.backend.repository.TaPorcCotizRepository;
import com.irpf.backend.repository.TaSexenioRepository;
import com.irpf.backend.repository.TaSueldoExtraRepository;
import com.irpf.backend.repository.TaSueldoRepository;
import com.irpf.backend.repository.TaTrienioExtraRepository;
import com.irpf.backend.repository.TaTrienioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Servicio de calculo de retribuciones mensuales y bases de cotizacion.
 */
@Service
public class CalculoRetribucionesService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal SEIS = BigDecimal.valueOf(6);
    private static final BigDecimal TREINTA = BigDecimal.valueOf(30);
    private static final BigDecimal EXTRA_DIVISOR = BigDecimal.valueOf(182);

    private final TaPorcCotizRepository taPorcCotizRepository;
    private final ContratoPersonaRepository contratoPersonaRepository;
    private final PuestoTipoRepository puestoTipoRepository;
    private final TaSueldoRepository taSueldoRepository;
    private final TaSueldoExtraRepository taSueldoExtraRepository;
    private final TaTrienioRepository taTrienioRepository;
    private final TaTrienioExtraRepository taTrienioExtraRepository;
    private final TaDestinoRepository taDestinoRepository;
    private final TaSexenioRepository taSexenioRepository;
    private final TaEspecificoRepository taEspecificoRepository;
    private final TaJornadaRepository taJornadaRepository;

    public CalculoRetribucionesService(TaPorcCotizRepository taPorcCotizRepository,
                                       ContratoPersonaRepository contratoPersonaRepository,
                                       PuestoTipoRepository puestoTipoRepository,
                                       TaSueldoRepository taSueldoRepository,
                                       TaSueldoExtraRepository taSueldoExtraRepository,
                                       TaTrienioRepository taTrienioRepository,
                                       TaTrienioExtraRepository taTrienioExtraRepository,
                                       TaDestinoRepository taDestinoRepository,
                                       TaSexenioRepository taSexenioRepository,
                                       TaEspecificoRepository taEspecificoRepository,
                                       TaJornadaRepository taJornadaRepository) {
        this.taPorcCotizRepository = taPorcCotizRepository;
        this.contratoPersonaRepository = contratoPersonaRepository;
        this.puestoTipoRepository = puestoTipoRepository;
        this.taSueldoRepository = taSueldoRepository;
        this.taSueldoExtraRepository = taSueldoExtraRepository;
        this.taTrienioRepository = taTrienioRepository;
        this.taTrienioExtraRepository = taTrienioExtraRepository;
        this.taDestinoRepository = taDestinoRepository;
        this.taSexenioRepository = taSexenioRepository;
        this.taEspecificoRepository = taEspecificoRepository;
        this.taJornadaRepository = taJornadaRepository;
    }

    /**
     * Calcula importes mensuales brutos y cotizados para una persona en un mes concreto.
     *
     * @param idPersona identificador de persona simulada
     * @param mesCalculo fecha del mes a calcular
     * @return resultado agregado de importes del mes
     */
    public ResultadoMensual calcularRetribucionesMensuales(Long idPersona, LocalDate mesCalculo) {
        YearMonth ym = YearMonth.from(mesCalculo);
        int anioCalculo = ym.getYear();

        BigDecimal porcentajeCotizacion = taPorcCotizRepository.findByAnio(anioCalculo)
                .map(TaPorcCotiz::getPorcentaje)
                .orElseThrow(() -> new IllegalStateException("No existe porcentaje de cotización para el año " + anioCalculo));

        LocalDate inicioMes = ym.atDay(1);
        LocalDate finMes = ym.atEndOfMonth();

        BigDecimal importeBrutoTotal = BigDecimal.ZERO;
        BigDecimal importeCotizadoTotal = BigDecimal.ZERO;

        List<ContratoPersona> contratos = contratoPersonaRepository.findByIdPersonaOrderByFechaDesdeAsc(idPersona);

        for (ContratoPersona contrato : contratos) {
            if (!estaActivoEnMes(contrato, inicioMes, finMes)) {
                continue;
            }

            long numDiasMesCompleto = ym.lengthOfMonth();
            long numDiasActivo = diasSolapados(contrato.getFechaDesde(), contrato.getFechaHasta(), inicioMes, finMes);
            if (numDiasActivo == 0) {
                continue;
            }

            PuestoTipo puesto = puestoTipoRepository.findById(contrato.getIdPuestoTipo())
                    .orElseThrow(() -> new IllegalStateException("No existe puesto tipo con id " + contrato.getIdPuestoTipo()));

            ImportesPuesto importes = obtenerImportesPuestoTipo(anioCalculo, puesto);

            BigDecimal brutoProporcional = prorratear(importes.getImporteBrutoMes(), numDiasActivo, numDiasMesCompleto);
            importeBrutoTotal = importeBrutoTotal.add(brutoProporcional);

            BigDecimal baseRed = numDiasActivo != numDiasMesCompleto
                    ? prorratear(importes.getImporteBaseCotizacion(), numDiasActivo, 30)
                    : importes.getImporteBaseCotizacion();

            importeCotizadoTotal = importeCotizadoTotal.add(
                    baseRed.multiply(porcentajeCotizacion).divide(CIEN, 6, RoundingMode.HALF_UP)
            );

            if (esMesExtra(ym)) {
                long diasSemestreExtra = calcularDiasSemestreExtra(contrato, ym);
                if (diasSemestreExtra > 0) {
                    BigDecimal importeExtraMes = prorratear(importes.getImporteExtra(), diasSemestreExtra, EXTRA_DIVISOR.longValue());
                    importeBrutoTotal = importeBrutoTotal.add(importeExtraMes);
                }
            }

            if (contrato.getFechaHasta() != null
                    && !contrato.getFechaHasta().isBefore(inicioMes)
                    && !contrato.getFechaHasta().isAfter(finMes)) {

                long numDiasLiq = calcularDiasLiquidacion(contrato, ym);
                if (numDiasLiq > 0) {
                    BigDecimal importeLiquidacion = prorratear(importes.getImporteExtra(), numDiasLiq, EXTRA_DIVISOR.longValue());
                    importeBrutoTotal = importeBrutoTotal.add(importeLiquidacion);
                }

                if ("S".equalsIgnoreCase(contrato.getIndVacNoDisfrutadas())) {
                    long numDiasVac = calculoDiasVacacionesNoDisfrutadas(contrato.getFechaDesde(), contrato.getFechaHasta(), anioCalculo);
                    if (numDiasVac > 0) {
                        BigDecimal importeVacaciones = prorratear(importes.getImporteBrutoMes(), numDiasVac, TREINTA.longValue());
                        importeBrutoTotal = importeBrutoTotal.add(importeVacaciones);
                        importeCotizadoTotal = importeCotizadoTotal.add(
                                importeVacaciones.multiply(porcentajeCotizacion).divide(CIEN, 6, RoundingMode.HALF_UP)
                        );
                    }
                }
            }
        }

        return new ResultadoMensual(
                importeBrutoTotal.setScale(2, RoundingMode.HALF_UP),
                importeCotizadoTotal.setScale(2, RoundingMode.HALF_UP)
        );
    }

    /**
     * Obtiene los importes retributivos de un puesto tipo para un ejercicio.
     *
     * @param ejercicio anio de referencia
     * @param puesto datos del puesto tipo
     * @return importes mensuales, extraordinarios y base de cotizacion
     */
    public ImportesPuesto obtenerImportesPuestoTipo(int ejercicio, PuestoTipo puesto) {
        BigDecimal importeBrutoMes = BigDecimal.ZERO;
        BigDecimal importeExtra = BigDecimal.ZERO;

        String codEstudio = puesto.getCodEstudio();
        Integer idComunidad = defaultComunidadId(puesto.getIdComunidad());

        BigDecimal sueldoBruto = BigDecimal.ZERO;
        BigDecimal sueldoExtraBruto = BigDecimal.ZERO;
        BigDecimal destinoBruto = BigDecimal.ZERO;

        if (codEstudio != null) {
            sueldoBruto = taSueldoRepository.findByAnioAndCodEstudio(ejercicio, codEstudio)
                    .map(s -> safeImporte(s.getImporte()))
                    .orElse(BigDecimal.ZERO);
            importeBrutoMes = importeBrutoMes.add(sueldoBruto);

            sueldoExtraBruto = taSueldoExtraRepository.findByAnioAndCodEstudio(ejercicio, codEstudio)
                    .map(s -> safeImporte(s.getImporte()))
                    .orElse(BigDecimal.ZERO);
            importeExtra = importeExtra.add(sueldoExtraBruto);

            destinoBruto = taDestinoRepository.findByAnioAndCodEstudio(ejercicio, codEstudio)
                    .map(d -> safeImporte(d.getImporte()))
                    .orElse(BigDecimal.ZERO);
            importeBrutoMes = importeBrutoMes.add(destinoBruto);
            importeExtra = importeExtra.add(destinoBruto);
        }

        BigDecimal trienioS = taTrienioRepository.findByAnioAndCodEstudio(ejercicio, "S")
                .map(t -> safeImporte(t.getImporte()))
                .orElse(BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(defaultInt(puesto.getNumTrieniosA1())));
        importeBrutoMes = importeBrutoMes.add(trienioS);

        BigDecimal trienioSExtra = taTrienioExtraRepository.findByAnioAndCodEstudio(ejercicio, "S")
                .map(t -> safeImporte(t.getImporte()))
                .orElse(BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(defaultInt(puesto.getNumTrieniosA1())));
        importeExtra = importeExtra.add(trienioSExtra);

        BigDecimal trienioP = taTrienioRepository.findByAnioAndCodEstudio(ejercicio, "P")
                .map(t -> safeImporte(t.getImporte()))
                .orElse(BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(defaultInt(puesto.getNumTrieniosA2())));
        importeBrutoMes = importeBrutoMes.add(trienioP);

        BigDecimal trienioPExtra = taTrienioExtraRepository.findByAnioAndCodEstudio(ejercicio, "P")
                .map(t -> safeImporte(t.getImporte()))
                .orElse(BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(defaultInt(puesto.getNumTrieniosA2())));
        importeExtra = importeExtra.add(trienioPExtra);

        BigDecimal trieniosBruto = trienioS.add(trienioP);
        BigDecimal trieniosExtraBruto = trienioSExtra.add(trienioPExtra);

        BigDecimal sexeniosBruto = BigDecimal.ZERO;
        int numSexenios = Math.min(defaultInt(puesto.getNumSexenios()), 5);
        if (numSexenios > 0) {
            List<com.irpf.backend.entidades.TaSexenio> sexenios =
                    taSexenioRepository.findByAnioAndIdComunidadAndNumSexenioLessThanEqualOrderByNumSexenioAsc(
                            ejercicio, idComunidad, String.valueOf(numSexenios));
            for (com.irpf.backend.entidades.TaSexenio sexenio : sexenios) {
                BigDecimal importeSexenio = safeImporte(sexenio.getImporte());
                sexeniosBruto = sexeniosBruto.add(importeSexenio);
                importeBrutoMes = importeBrutoMes.add(importeSexenio);
                importeExtra = importeExtra.add(importeSexenio);
            }
        }

        BigDecimal importeEspecDocente = safeImporte(puesto.getImporteEspecDocente());
        if (importeEspecDocente.compareTo(BigDecimal.ZERO) == 0 && codEstudio != null) {
            BigDecimal especifico = taEspecificoRepository.findByAnioAndCodEstudioAndIdComunidad(ejercicio, codEstudio, idComunidad)
                    .map(e -> safeImporte(e.getImporte()))
                    .orElse(BigDecimal.ZERO);
            importeBrutoMes = importeBrutoMes.add(especifico);
            importeExtra = importeExtra.add(especifico);
        }

        BigDecimal porcentajeJornada = taJornadaRepository.findById(defaultInt(puesto.getCodJornada()))
                .map(TaJornada::getPorcentaje)
                .orElse(CIEN);

        importeBrutoMes = importeBrutoMes.multiply(porcentajeJornada).divide(CIEN, 6, RoundingMode.HALF_UP);
        importeExtra = importeExtra.multiply(porcentajeJornada).divide(CIEN, 6, RoundingMode.HALF_UP);

        BigDecimal importeSueldo = sueldoBruto.multiply(porcentajeJornada).divide(CIEN, 6, RoundingMode.HALF_UP);
        BigDecimal importeComplementoDestino = destinoBruto.multiply(porcentajeJornada).divide(CIEN, 6, RoundingMode.HALF_UP);
        BigDecimal importeTrienios = trieniosBruto.multiply(porcentajeJornada).divide(CIEN, 6, RoundingMode.HALF_UP);
        BigDecimal importeSexenios = sexeniosBruto.multiply(porcentajeJornada).divide(CIEN, 6, RoundingMode.HALF_UP);
        BigDecimal importeSueldoExtra = sueldoExtraBruto.multiply(porcentajeJornada).divide(CIEN, 6, RoundingMode.HALF_UP);
        BigDecimal importeTrieniosExtra = trieniosExtraBruto.multiply(porcentajeJornada).divide(CIEN, 6, RoundingMode.HALF_UP);

        if (importeEspecDocente.compareTo(BigDecimal.ZERO) != 0) {
            importeBrutoMes = importeBrutoMes.add(importeEspecDocente);
            importeExtra = importeExtra.add(importeEspecDocente);
        }

        BigDecimal otrosAbonos = safeImporte(puesto.getImporteOtrosAbonosMes());
        if (otrosAbonos.compareTo(BigDecimal.ZERO) != 0) {
            importeBrutoMes = importeBrutoMes.add(otrosAbonos);
        }

        BigDecimal importeBaseCotizacion = importeBrutoMes.add(importeExtra.divide(SEIS, 6, RoundingMode.HALF_UP));

        return new ImportesPuesto(
                importeBrutoMes.setScale(6, RoundingMode.HALF_UP),
                importeExtra.setScale(6, RoundingMode.HALF_UP),
                importeBaseCotizacion.setScale(6, RoundingMode.HALF_UP),
                importeSueldo.setScale(6, RoundingMode.HALF_UP),
                importeComplementoDestino.setScale(6, RoundingMode.HALF_UP),
                importeTrienios.setScale(6, RoundingMode.HALF_UP),
                importeSexenios.setScale(6, RoundingMode.HALF_UP),
                importeSueldoExtra.setScale(6, RoundingMode.HALF_UP),
                importeTrieniosExtra.setScale(6, RoundingMode.HALF_UP)
        );
    }

    /**
     * Calcula dias de vacaciones no disfrutadas liquidados a fin de contrato.
     *
     * @param fechaDesde inicio de contrato
     * @param fechaHasta fin de contrato
     * @param ejercicio ejercicio de referencia del curso escolar
     * @return numero de dias a liquidar
     */
    public long calculoDiasVacacionesNoDisfrutadas(LocalDate fechaDesde, LocalDate fechaHasta, int ejercicio) {
        LocalDate inicioCursoPrevio = LocalDate.of(ejercicio - 1, 9, 1);
        LocalDate fechaDesdeVacaciones = fechaDesde.isBefore(inicioCursoPrevio) ? inicioCursoPrevio : fechaDesde;

        LocalDate finCurso = LocalDate.of(ejercicio, 8, 31);
        if (fechaHasta.isAfter(finCurso)) {
            return 0;
        }
        LocalDate fechaHastaVacaciones = fechaHasta;

        long diasPeriodo = ChronoUnit.DAYS.between(fechaDesdeVacaciones, fechaHastaVacaciones.plusDays(1));
        BigDecimal diasVac = BigDecimal.valueOf(diasPeriodo)
                .multiply(TREINTA)
                .divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        return diasVac.setScale(0, RoundingMode.CEILING).longValue();
    }

    /**
     * Verifica si un contrato esta activo en el mes objetivo.
     *
     * @param contrato contrato evaluado
     * @param inicioMes primer dia del mes
     * @param finMes ultimo dia del mes
     * @return true cuando existe solape con el periodo mensual
     */
    private boolean estaActivoEnMes(ContratoPersona contrato, LocalDate inicioMes, LocalDate finMes) {
        LocalDate fechaDesde = contrato.getFechaDesde();
        LocalDate fechaHasta = contrato.getFechaHasta();
        boolean empiezaAntesDeFin = !fechaDesde.isAfter(finMes);
        boolean terminaDespuesDeInicio = fechaHasta == null || !fechaHasta.isBefore(inicioMes);
        return empiezaAntesDeFin && terminaDespuesDeInicio;
    }

    /**
     * Indica si el mes es de paga extraordinaria.
     *
     * @param ym anio-mes evaluado
     * @return true para junio y diciembre
     */
    private boolean esMesExtra(YearMonth ym) {
        int mes = ym.getMonthValue();
        return mes == 6 || mes == 12;
    }

    /**
     * Calcula dias de solape entre un rango de contrato y un rango de referencia.
     *
     * @param desde fecha de inicio del contrato
     * @param hasta fecha de fin del contrato
     * @param inicio inicio del rango de referencia
     * @param fin fin del rango de referencia
     * @return numero de dias solapados
     */
    private long diasSolapados(LocalDate desde, LocalDate hasta, LocalDate inicio, LocalDate fin) {
        LocalDate realInicio = desde.isBefore(inicio) ? inicio : desde;
        LocalDate realFin = (hasta == null || hasta.isAfter(fin)) ? fin : hasta;
        if (realFin.isBefore(realInicio)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(realInicio, realFin.plusDays(1));
    }

    /**
     * Calcula dias devengados para paga extra semestral de un contrato.
     *
     * @param contrato contrato evaluado
     * @param ym mes de calculo
     * @return dias devengados del semestre aplicable
     */
    private long calcularDiasSemestreExtra(ContratoPersona contrato, YearMonth ym) {
        int year = ym.getYear();
        LocalDate inicio;
        LocalDate fin;
        if (ym.getMonthValue() == 6) {
            inicio = LocalDate.of(year - 1, 12, 1);
            fin = LocalDate.of(year, 5, 31);
        } else {
            inicio = LocalDate.of(year, 6, 1);
            fin = LocalDate.of(year, 11, 30);
        }
        return diasSolapados(contrato.getFechaDesde(), contrato.getFechaHasta(), inicio, fin);
    }

    /**
     * Calcula dias de liquidacion de pagas extra al finalizar un contrato.
     *
     * @param contrato contrato evaluado
     * @param ym mes de calculo
     * @return dias a liquidar
     */
    private long calcularDiasLiquidacion(ContratoPersona contrato, YearMonth ym) {
        int year = ym.getYear();
        int mes = ym.getMonthValue();
        LocalDate inicio;
        if (mes >= 1 && mes <= 5) {
            inicio = LocalDate.of(year - 1, 12, 1);
        } else if (mes >= 6 && mes <= 11) {
            inicio = LocalDate.of(year, 6, 1);
        } else {
            inicio = LocalDate.of(year, 12, 1);
        }
        LocalDate fechaHasta = contrato.getFechaHasta() != null ? contrato.getFechaHasta() : ym.atEndOfMonth();
        return diasSolapados(contrato.getFechaDesde(), fechaHasta, inicio, fechaHasta);
    }

    /**
     * Prorratea un importe por dias aplicando el divisor especificado.
     *
     * @param importe importe base
     * @param dias dias computables
     * @param divisor divisor de prorrateo
     * @return importe prorrateado
     */
    private BigDecimal prorratear(BigDecimal importe, long dias, long divisor) {
        if (dias <= 0 || importe == null) {
            return BigDecimal.ZERO;
        }
        return importe.multiply(BigDecimal.valueOf(dias))
                .divide(BigDecimal.valueOf(divisor), 6, RoundingMode.HALF_UP);
    }

    /**
     * Sustituye importes nulos por cero.
     *
     * @param importe importe origen
     * @return importe o cero si es null
     */
    private BigDecimal safeImporte(BigDecimal importe) {
        return Optional.ofNullable(importe).orElse(BigDecimal.ZERO);
    }

    /**
     * Devuelve cero cuando el entero de entrada es nulo.
     *
     * @param valor valor origen
     * @return valor o cero
     */
    private int defaultInt(Integer valor) {
        return valor != null ? valor : 0;
    }

    /**
     * Devuelve comunidad 1 por defecto cuando no se informa identificador.
     *
     * @param valor identificador de comunidad
     * @return identificador informado o valor por defecto
     */
    private int defaultComunidadId(Integer valor) {
        return valor != null ? valor : 1;
    }

    /**
     * DTO interno con importes agregados del mes.
     */
    public static class ResultadoMensual {
        private final BigDecimal importeBrutoTotalMes;
        private final BigDecimal importeCotizadoTotalMes;

        /**
         * Crea una instancia con importes mensuales calculados.
         *
         * @param importeBrutoTotalMes importe bruto del mes
         * @param importeCotizadoTotalMes importe cotizado del mes
         */
        public ResultadoMensual(BigDecimal importeBrutoTotalMes, BigDecimal importeCotizadoTotalMes) {
            this.importeBrutoTotalMes = importeBrutoTotalMes;
            this.importeCotizadoTotalMes = importeCotizadoTotalMes;
        }

        /**
         * Obtiene el importe bruto total del mes.
         *
         * @return importe bruto mensual
         */
        public BigDecimal getImporteBrutoTotalMes() {
            return importeBrutoTotalMes;
        }

        /**
         * Obtiene el importe cotizado total del mes.
         *
         * @return importe cotizado mensual
         */
        public BigDecimal getImporteCotizadoTotalMes() {
            return importeCotizadoTotalMes;
        }
    }

    /**
     * DTO interno con detalle de importes calculados para un puesto.
     */
    public static class ImportesPuesto {
        private final BigDecimal importeBrutoMes;
        private final BigDecimal importeExtra;
        private final BigDecimal importeBaseCotizacion;
        private final BigDecimal importeSueldo;
        private final BigDecimal importeComplementoDestino;
        private final BigDecimal importeTrienios;
        private final BigDecimal importeSexenios;
        private final BigDecimal importeSueldoExtra;
        private final BigDecimal importeTrieniosExtra;

        /**
         * Crea una instancia de importes del puesto.
         *
         * @param importeBrutoMes importe mensual ordinario
         * @param importeExtra importe de pagas extra
         * @param importeBaseCotizacion base de cotizacion mensual
         * @param importeSueldo importe de sueldo reducido por jornada
         * @param importeComplementoDestino importe de complemento de destino reducido por jornada
         * @param importeTrienios importe de trienios reducido por jornada
         * @param importeSexenios importe de sexenios reducido por jornada
         * @param importeSueldoExtra importe de sueldo para extra reducido por jornada
         * @param importeTrieniosExtra importe de trienios para extra reducido por jornada
         */
        public ImportesPuesto(BigDecimal importeBrutoMes, BigDecimal importeExtra, BigDecimal importeBaseCotizacion,
                BigDecimal importeSueldo, BigDecimal importeComplementoDestino, BigDecimal importeTrienios,
                BigDecimal importeSexenios, BigDecimal importeSueldoExtra, BigDecimal importeTrieniosExtra) {
            this.importeBrutoMes = importeBrutoMes;
            this.importeExtra = importeExtra;
            this.importeBaseCotizacion = importeBaseCotizacion;
            this.importeSueldo = importeSueldo;
            this.importeComplementoDestino = importeComplementoDestino;
            this.importeTrienios = importeTrienios;
            this.importeSexenios = importeSexenios;
            this.importeSueldoExtra = importeSueldoExtra;
            this.importeTrieniosExtra = importeTrieniosExtra;
        }

        /**
         * Obtiene el importe bruto mensual del puesto.
         *
         * @return importe bruto mensual
         */
        public BigDecimal getImporteBrutoMes() {
            return importeBrutoMes;
        }

        /**
         * Obtiene el importe de pagas extraordinarias.
         *
         * @return importe de extras
         */
        public BigDecimal getImporteExtra() {
            return importeExtra;
        }

        /**
         * Obtiene la base de cotizacion mensual calculada.
         *
         * @return base de cotizacion
         */
        public BigDecimal getImporteBaseCotizacion() {
            return importeBaseCotizacion;
        }

        /**
         * Obtiene el importe de sueldo reducido por jornada.
         *
         * @return importe de sueldo
         */
        public BigDecimal getImporteSueldo() {
            return importeSueldo;
        }

        /**
         * Obtiene el importe de complemento de destino reducido por jornada.
         *
         * @return importe de complemento de destino
         */
        public BigDecimal getImporteComplementoDestino() {
            return importeComplementoDestino;
        }

        /**
         * Obtiene el importe de trienios reducido por jornada.
         *
         * @return importe de trienios
         */
        public BigDecimal getImporteTrienios() {
            return importeTrienios;
        }

        /**
         * Obtiene el importe de sexenios reducido por jornada.
         *
         * @return importe de sexenios
         */
        public BigDecimal getImporteSexenios() {
            return importeSexenios;
        }

        /**
         * Obtiene el importe de sueldo para paga extra reducido por jornada.
         *
         * @return importe de sueldo para extra
         */
        public BigDecimal getImporteSueldoExtra() {
            return importeSueldoExtra;
        }

        /**
         * Obtiene el importe de trienios para paga extra reducido por jornada.
         *
         * @return importe de trienios para extra
         */
        public BigDecimal getImporteTrieniosExtra() {
            return importeTrieniosExtra;
        }
    }
}


