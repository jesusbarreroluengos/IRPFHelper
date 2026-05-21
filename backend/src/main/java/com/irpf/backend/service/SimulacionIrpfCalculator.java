package com.irpf.backend.service;

import com.irpf.backend.entidades.Ascendiente;
import com.irpf.backend.entidades.Descendiente;
import com.irpf.backend.entidades.PersonaSimulada;
import com.irpf.backend.entidades.Simulacion;
import com.irpf.backend.repository.AscendienteRepository;
import com.irpf.backend.repository.DescendienteRepository;
import com.irpf.backend.repository.PersonaSimuladaRepository;
import com.irpf.backend.repository.SimulacionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Service
public class SimulacionIrpfCalculator {

    private static final String RETENEDOR_NIF = "12345678Z";
    private static final String RETENEDOR_NOMBRE = "Retenedor simulado";
    private static final String RETENIDO_NOMBRE = "Retenedor simulado";
    private static final String NIF_CONYUGE_POR_DEFECTO = "11111111H";
    private static final String RETENIDO_NIF_DEFECTO = "22222222J";
    private static final String ROOT_ELEMENT = "AEATRetencionesEntrada";
    private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String SCHEMA_LOCATION = "AEATRetenciones2026.xsd";
    private static final String CAUSA_REGULARIZACION_OTRAS = "11";
    private static final BigDecimal MIN_IMPORTE = new BigDecimal("0.01");
    private static final BigDecimal CERO = BigDecimal.ZERO;
    private static final int EJERCICIO_XSD = 2026;

    private final SimulacionRepository simulacionRepository;
    private final PersonaSimuladaRepository personaSimuladaRepository;
    private final DescendienteRepository descendienteRepository;
    private final AscendienteRepository ascendienteRepository;
    private final RestTemplate restTemplate;

    public SimulacionIrpfCalculator(SimulacionRepository simulacionRepository,
                                    PersonaSimuladaRepository personaSimuladaRepository,
                                    DescendienteRepository descendienteRepository,
                                    AscendienteRepository ascendienteRepository) {
        this.simulacionRepository = simulacionRepository;
        this.personaSimuladaRepository = personaSimuladaRepository;
        this.descendienteRepository = descendienteRepository;
        this.ascendienteRepository = ascendienteRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Calcula el IRPF real invocando al servicio de la AEAT y actualiza la simulacion.
     */
    public CalculoIrpfResultado calcularYActualizarIrpf(Long idSimulacion) {
        Simulacion simulacion = simulacionRepository.findById(idSimulacion)
                .orElseThrow(() -> new IllegalArgumentException("Simulacion no encontrada con id " + idSimulacion));

        PersonaSimulada persona = personaSimuladaRepository.findById(simulacion.getIdPersona())
                .orElseThrow(() -> new IllegalArgumentException("Persona simulada no encontrada con id " + simulacion.getIdPersona()));

        List<Descendiente> descendientes = descendienteRepository.findByIdPersona(simulacion.getIdPersona());
        List<Ascendiente> ascendientes = ascendienteRepository.findByIdPersona(simulacion.getIdPersona());

        String xmlEntrada = construirXmlEntrada(simulacion, persona, descendientes, ascendientes);
        System.out.println("XML Enviado a AEAT: " + xmlEntrada);

        String respuesta = invocarServicioAeat(xmlEntrada, simulacion.getEjercicio());
        CalculoIrpfResultado resultado = extraerResultadoCalculo(respuesta, simulacion);

        System.out.println("Tipo de retencion obtenido: " + resultado.tipoRetencion());

        simulacion.setPorcIrpf(resultado.tipoRetencion());
        simulacionRepository.save(simulacion);
        return resultado;
    }

    private String construirXmlEntrada(Simulacion simulacion,
                                       PersonaSimulada persona,
                                       List<Descendiente> descendientes,
                                       List<Ascendiente> ascendientes) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<").append(ROOT_ELEMENT).append(simulacion.getEjercicio())
                .append(" xsi:noNamespaceSchemaLocation=\"").append(SCHEMA_LOCATION).append("\"")
                .append(" xmlns:xsi=\"").append(XSI_NS).append("\">");

        sb.append("<IdDoc><CodModelo>RET</CodModelo><Ejercicio>").append(simulacion.getEjercicio()).append("</Ejercicio></IdDoc>");

        sb.append("<Retenedor>");
        sb.append("<Nif>").append(RETENEDOR_NIF).append("</Nif>");
        sb.append("<ApellidosNombre>").append(escapeXml(RETENEDOR_NOMBRE)).append("</ApellidosNombre>");

        sb.append("<Retenido>");
        sb.append("<Nif>").append(formatearNifRetenido(persona.getNifFicticio())).append("</Nif>");
        sb.append("<ApellidosNombre>").append(escapeXml(RETENIDO_NOMBRE)).append("</ApellidosNombre>");
        sb.append("<Nacimiento>").append(persona.getAnioNac()).append("</Nacimiento>");

        if ("S".equalsIgnoreCase(persona.getIndCeumelilla())) {
            sb.append("<ResidenciaCeutaMelilla/>");
        }

        sb.append(construirSituacionFamiliar(persona, descendientes.size()));
        agregarDiscapacidad(sb, persona.getCodDiscapacidad());
        sb.append("<SituacionLaboral><TrabajadorActivo>");
        sb.append("<Contrato>").append(mapearContrato(persona.getCodContrato())).append("</Contrato>");
        sb.append("</TrabajadorActivo></SituacionLaboral>");

        for (Descendiente d : descendientes) {
            sb.append("<Descendiente>");
            sb.append("<Nacimiento>").append(d.getAnioNac()).append("</Nacimiento>");
            if (d.getAnioAdopcion() != null) {
                sb.append("<Adopcion>").append(d.getAnioAdopcion()).append("</Adopcion>");
            }
            if ("S".equalsIgnoreCase(d.getIndPorentero())) {
                sb.append("<ComputadoEntero/>");
            }
            agregarDiscapacidad(sb, d.getCodDiscapacidad());
            sb.append("</Descendiente>");
        }

        for (Ascendiente a : ascendientes) {
            sb.append("<Ascendiente>");
            sb.append("<Nacimiento>").append(a.getAnioNac()).append("</Nacimiento>");
            sb.append("<Convivencia>").append(a.getIndCompartido()).append("</Convivencia>");
            agregarDiscapacidad(sb, a.getCodDiscapacidad());
            sb.append("</Ascendiente>");
        }

        BigDecimal retribuciones = suma(simulacion.getImpBrutoAbonado(), simulacion.getImpBrutoPendiente());
        sb.append("<RetribAnuales>").append(formatearImportePositivo(retribuciones)).append("</RetribAnuales>");

        BigDecimal gastos = suma(simulacion.getImpGastosRealizados(), simulacion.getImpGastosPendiente());
        if (esPositivo(gastos)) {
            sb.append("<Cotizaciones>").append(formatearImportePositivo(gastos)).append("</Cotizaciones>");
        }

        if (esPositivo(persona.getImpPensionConyuge())) {
            sb.append("<PensionCompensatoria>").append(formatearImportePositivo(persona.getImpPensionConyuge())).append("</PensionCompensatoria>");
        }

        if (esPositivo(persona.getImpPensionHijos())) {
            sb.append("<AnualidadesHijos>").append(formatearImportePositivo(persona.getImpPensionHijos())).append("</AnualidadesHijos>");
        }

        if ("S".equalsIgnoreCase(persona.getIndCeumelilla())) {
            sb.append("<RdtosObtenidosCeutaMelilla/>");
        }

        if (requiereRegularizacion(simulacion)) {
            sb.append("<Regularizacion>");
            sb.append("<Causa>").append(CAUSA_REGULARIZACION_OTRAS).append("</Causa>");
            sb.append("<RetribSatisfechas>").append(formatearImportePositivo(simulacion.getImpBrutoAbonado())).append("</RetribSatisfechas>");

            if (esPositivo(simulacion.getImpRetencionesPracticadas())) {
                sb.append("<RetencionPracticada>").append(formatearImportePositivo(simulacion.getImpRetencionesPracticadas())).append("</RetencionPracticada>");
            }

            sb.append("<TipoRetencion>").append(formatearRetencion(BigDecimal.ZERO)).append("</TipoRetencion>");
            sb.append("</Regularizacion>");
        }

        sb.append("</Retenido>");
        sb.append("</Retenedor>");
        sb.append("</").append(ROOT_ELEMENT).append(simulacion.getEjercicio()).append(">");
        return sb.toString();
    }

    private String construirSituacionFamiliar(PersonaSimulada persona, int numeroDescendientes) {
        String codSitfam = persona.getCodSitfam() != null ? persona.getCodSitfam().trim() : "";
        StringBuilder sb = new StringBuilder("<SituacionFamiliar>");
        if ("1".equals(codSitfam) && numeroDescendientes > 0) {
            sb.append("<Situacion1/>");
        } else if ("2".equals(codSitfam)) {
            sb.append("<Situacion2>");
            sb.append("<NifConyuge>").append(NIF_CONYUGE_POR_DEFECTO).append("</NifConyuge>");
            sb.append("</Situacion2>");
        } else {
            sb.append("<Situacion3/>");
        }
        sb.append("</SituacionFamiliar>");
        return sb.toString();
    }

    private boolean requiereRegularizacion(Simulacion simulacion) {
        return esPositivo(simulacion.getImpBrutoAbonado()) || esPositivo(simulacion.getImpRetencionesPracticadas());
    }

    private String invocarServicioAeat(String xmlEntrada, Integer ejercicio) {
        String url = seleccionarUrlServicio();
        int periodo = calcularPeriodo(ejercicio);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("EJER", String.valueOf(ejercicio != null ? ejercicio : EJERCICIO_XSD));
        form.add("PER", String.valueOf(periodo));
        form.add("F01", xmlEntrada);

        System.out.println("----------  XML Entrada: ----------");
        System.out.println(xmlEntrada);
        System.out.println("----------  Fin XML Entrada ----------");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.ALL));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("No se pudo obtener respuesta de la AEAT (codigo " + response.getStatusCode() + ")");
        }

        System.out.println("----------  Respuesta AEAT: ----------");
        System.out.println(response.getBody());
        System.out.println("----------  Fin Respuesta AEAT: ----------");
        return response.getBody();
    }

    private String seleccionarUrlServicio() {
        LocalDate hoy = LocalDate.now();
        LocalDate cambio = LocalDate.of(2025, 1, 1);
        return hoy.isBefore(cambio)
                ? "https://www2.agenciatributaria.gob.es/wlpl/PRET-C200/mc"
                : "https://www2.agenciatributaria.gob.es/wlpl/PRET-R200/mc";
    }

    private int calcularPeriodo(Integer ejercicio) {
        int ej = ejercicio != null ? ejercicio : EJERCICIO_XSD;
        LocalDate hoy = LocalDate.now();
        if (ej == 2026) {
            return 0;
        }
        if (ej == 2025) {
            LocalDate corte = LocalDate.of(2025, 11, 27);
            return hoy.isBefore(corte) ? 0 : 1;
        }
        if (ej == 2024) {
            LocalDate corte0 = LocalDate.of(2024, 2, 8);
            LocalDate corte1 = LocalDate.of(2024, 6, 28);
            if (hoy.isBefore(corte0)) {
                return 0;
            }
            if (hoy.isBefore(corte1)) {
                return 1;
            }
            return 2;
        }
        if (ej == 2023) {
            return hoy.getMonthValue() == 1 ? 0 : 1;
        }
        return 0;
    }

    private CalculoIrpfResultado extraerResultadoCalculo(String xmlRespuesta, Simulacion simulacion) {
        Integer ejercicio = simulacion.getEjercicio();
        try {
            System.out.println("Respuesta AEAT: " + xmlRespuesta);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setNamespaceAware(false);

            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new java.io.StringReader(xmlRespuesta)));

            String root = document.getDocumentElement().getNodeName();
            String esperado = "AEATRetencionesSalida" + (ejercicio != null ? ejercicio : EJERCICIO_XSD);
            String rootError = "AEATRetencionesError" + (ejercicio != null ? ejercicio : EJERCICIO_XSD);
            if (!esperado.equals(root) && !rootError.equals(root)) {
                throw new IllegalStateException("Respuesta inesperada de la AEAT: elemento raiz " + root);
            }
            if (rootError.equals(root)) {
                throw new IllegalStateException("La AEAT devolvio un error: " + obtenerDescripcionError(document, ejercicio));
            }

            XPath xpath = XPathFactory.newInstance().newXPath();
            BigDecimal tipoRetencion = extraerBigDecimal(xpath, document, new String[] {
                String.format("/AEATRetencionesSalida%d/Retenedor/Retenido/TipoRetencion", ejercicio != null ? ejercicio : EJERCICIO_XSD),
                "//TipoRetencion",
                "//TIPO",
                "//TIPOA"
            });
            if (tipoRetencion == null) {
                throw new IllegalStateException("No se pudo leer el porcentaje de IRPF en la respuesta");
            }

            BigDecimal importeBrutoAnual = extraerBigDecimal(xpath, document, new String[] {
                String.format("/AEATRetencionesSalida%d/Retenedor/Retenido/RetribucionesAnuales", ejercicio != null ? ejercicio : EJERCICIO_XSD),
                String.format("/AEATRetencionesSalida%d/Retenedor/Retenido/RetribucionAnual", ejercicio != null ? ejercicio : EJERCICIO_XSD),
                String.format("/AEATRetencionesSalida%d/Retenedor/Retenido/RetribAnuales", ejercicio != null ? ejercicio : EJERCICIO_XSD),
                "//RetribucionesAnuales",
                "//RetribucionAnual",
                "//RetribAnuales",
                "//RetribAnual",
                "//RETRIBA"
            });

            BigDecimal importeRetencionesAnual = extraerBigDecimal(xpath, document, new String[] {
                String.format("/AEATRetencionesSalida%d/Retenedor/Retenido/ImpAnualRetencionesIngresosCuenta", ejercicio != null ? ejercicio : EJERCICIO_XSD),
                String.format("/AEATRetencionesSalida%d/Retenedor/Retenido/ImporteAnualRetenciones", ejercicio != null ? ejercicio : EJERCICIO_XSD),
                String.format("/AEATRetencionesSalida%d/Retenedor/Retenido/ImporteRetencionesAnual", ejercicio != null ? ejercicio : EJERCICIO_XSD),
                String.format("/AEATRetencionesSalida%d/Retenedor/Retenido/RetencionAnual", ejercicio != null ? ejercicio : EJERCICIO_XSD),
                "//ImpAnualRetencionesIngresosCuenta",
                "//ImporteAnualRetenciones",
                "//ImporteRetencionesAnual",
                "//RetencionAnual",
                "//RetencionesAnuales",
                "//IMPORTEA",
                "//RETENIDO"
            });

            if (importeBrutoAnual == null) {
                importeBrutoAnual = suma(simulacion.getImpBrutoAbonado(), simulacion.getImpBrutoPendiente());
            }

            return new CalculoIrpfResultado(
                    tipoRetencion.setScale(2, RoundingMode.HALF_UP),
                    importeBrutoAnual.setScale(2, RoundingMode.HALF_UP),
                    importeRetencionesAnual != null ? importeRetencionesAnual.setScale(2, RoundingMode.HALF_UP) : null
            );
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | java.io.IOException e) {
            throw new IllegalStateException("Error al interpretar la respuesta de la AEAT: " + e.getMessage(), e);
        }
    }

    private BigDecimal extraerBigDecimal(XPath xpath, Document document, String[] expresiones) throws XPathExpressionException {
        for (String expresion : expresiones) {
            String valor = xpath.evaluate(expresion, document);
            if (valor == null || valor.isBlank()) {
                continue;
            }
            try {
                return new BigDecimal(valor.trim());
            } catch (NumberFormatException ignored) {
                // Continúa con la siguiente expresión candidata.
            }
        }
        return null;
    }

    private String obtenerDescripcionError(Document document, Integer ejercicio) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expresionEvaluar = String.format("/AEATRetencionesError%d/ErrorGeneral/Descripcion", ejercicio != null ? ejercicio : EJERCICIO_XSD);
        String desc = xpath.evaluate(expresionEvaluar, document);
        if (desc == null || desc.isBlank()) {
            NodeList descripciones = (NodeList) xpath.evaluate(
                    "//Descripcion",
                    document,
                    XPathConstants.NODESET
            );

            List<String> errores = new ArrayList<>();
            for (int i = 0; i < descripciones.getLength(); i++) {
                errores.add(descripciones.item(i).getTextContent().trim());
            }
            desc = String.join("; ", errores);
        }

        return desc == null || desc.isBlank() ? "Sin detalle" : desc;
    }

    private void agregarDiscapacidad(StringBuilder sb, String codigo) {
        if (codigo == null) {
            return;
        }
        switch (codigo) {
            case "4":
                sb.append("<Discapacidad><Grado1><MovilidadReducida/></Grado1></Discapacidad>");
                break;
            case "2":
                sb.append("<Discapacidad><Grado1/></Discapacidad>");
                break;
            case "3":
                sb.append("<Discapacidad><Grado2/></Discapacidad>");
                break;
            default:
                break;
        }
    }

    private String mapearContrato(String codContrato) {
        if ("1".equals(codContrato)) {
            return "2";
        }
        if ("2".equals(codContrato)) {
            return "3";
        }
        return "1";
    }

    private String formatearNifRetenido(String nif) {
        if (nif == null || nif.length() < 8) {
            return RETENIDO_NIF_DEFECTO;
        }
        return nif.trim();
    }

    private String formatearImportePositivo(BigDecimal valor) {
        BigDecimal saneado = valor == null ? MIN_IMPORTE : valor.abs();
        if (saneado.compareTo(MIN_IMPORTE) < 0) {
            saneado = MIN_IMPORTE;
        }
        return decimal(saneado);
    }

    private String formatearRetencion(BigDecimal valor) {
        BigDecimal saneado = valor == null ? CERO : valor;
        return decimal(saneado.setScale(2, RoundingMode.HALF_UP));
    }

    private String decimal(BigDecimal valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("0.00", symbols);
        df.setGroupingUsed(false);
        return df.format(valor);
    }

    private boolean esPositivo(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal suma(BigDecimal a, BigDecimal b) {
        BigDecimal izquierda = a != null ? a : CERO;
        BigDecimal derecha = b != null ? b : CERO;
        return izquierda.add(derecha);
    }

    private String escapeXml(String valor) {
        if (valor == null) {
            return "";
        }
        return valor
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public record CalculoIrpfResultado(
            BigDecimal tipoRetencion,
            BigDecimal importeBrutoAnual,
            BigDecimal importeRetencionesAnual) {
    }
}
