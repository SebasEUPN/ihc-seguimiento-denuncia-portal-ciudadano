package com.proyecto.portal.service;

import com.proyecto.portal.dto.CiudadanoDTO;
import com.proyecto.portal.dto.DenunciaDetalleDTO;
import com.proyecto.portal.dto.DenunciaDetalleDTO.DocumentoItem;
import com.proyecto.portal.dto.DenunciaDetalleDTO.Evento;
import com.proyecto.portal.dto.DenunciaDetalleDTO.PasoFiscal;
import com.proyecto.portal.dto.DenunciaDetalleDTO.Responsable;
import com.proyecto.portal.dto.DenunciaResumenDTO;
import com.proyecto.portal.dto.NotificacionDTO;
import com.proyecto.portal.exception.ApiException;
import com.proyecto.portal.model.Actuacion;
import com.proyecto.portal.model.Conversacion;
import com.proyecto.portal.model.Denuncia;
import com.proyecto.portal.model.Mensaje;
import com.proyecto.portal.model.SeguimientoDenuncia;
import com.proyecto.portal.model.Usuario;
import com.proyecto.portal.repository.ActuacionRepository;
import com.proyecto.portal.repository.CiudadanoRepository;
import com.proyecto.portal.repository.ConversacionRepository;
import com.proyecto.portal.repository.DenunciaRepository;
import com.proyecto.portal.repository.DocumentoRepository;
import com.proyecto.portal.repository.MensajeRepository;
import com.proyecto.portal.repository.SeguimientoDenunciaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Consultas de seguimiento del ciudadano: sus denuncias, el detalle de cada
 * una y las notificaciones. Todo es de solo lectura.
 */
@Service
public class SeguimientoService {

    private final CiudadanoRepository ciudadanoRepository;
    private final DenunciaRepository denunciaRepository;
    private final SeguimientoDenunciaRepository seguimientoRepository;
    private final ActuacionRepository actuacionRepository;
    private final DocumentoRepository documentoRepository;
    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;

    /** Indica si el ciudadano ve las observaciones de los funcionarios (app.portal.mostrar-observaciones). */
    private final boolean mostrarObservaciones;

    public SeguimientoService(CiudadanoRepository ciudadanoRepository, DenunciaRepository denunciaRepository,
                              SeguimientoDenunciaRepository seguimientoRepository,
                              ActuacionRepository actuacionRepository, DocumentoRepository documentoRepository,
                              ConversacionRepository conversacionRepository, MensajeRepository mensajeRepository,
                              @Value("${app.portal.mostrar-observaciones:true}") boolean mostrarObservaciones) {
        this.ciudadanoRepository = ciudadanoRepository;
        this.denunciaRepository = denunciaRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.actuacionRepository = actuacionRepository;
        this.documentoRepository = documentoRepository;
        this.conversacionRepository = conversacionRepository;
        this.mensajeRepository = mensajeRepository;
        this.mostrarObservaciones = mostrarObservaciones;
    }

    /** Datos personales del ciudadano de la sesión. */
    @Transactional(readOnly = true)
    public CiudadanoDTO ciudadano(Long idCiudadano) {
        return ciudadanoRepository.findById(idCiudadano).map(CiudadanoDTO::de)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Tu consulta ya no es válida. Ingresa nuevamente."));
    }

    /**
     * Busca una denuncia y comprueba que pertenezca al ciudadano de la sesión.
     * Si es de otra persona se responde igual que si no existiera (404), para
     * que nadie pueda ver expedientes ajenos cambiando el código en la dirección.
     */
    @Transactional(readOnly = true)
    public Denuncia denunciaPropia(String codigo, Long idCiudadano) {
        Denuncia denuncia = denunciaRepository.findByCodigo(codigo == null ? "" : codigo.trim().toUpperCase()).orElse(null);
        if (denuncia == null || !denuncia.getCiudadano().getId().equals(idCiudadano)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No encontramos esa denuncia entre tus registros.");
        }
        return denuncia;
    }

    /** "Mis denuncias registradas": todas las denuncias del ciudadano. */
    @Transactional(readOnly = true)
    public List<DenunciaResumenDTO> listar(Long idCiudadano) {
        return denunciaRepository.findByCiudadanoIdOrderByFechaRegistroDesc(idCiudadano).stream()
                .map(DenunciaResumenDTO::de)
                .toList();
    }

    /** Detalle completo de una denuncia del ciudadano. */
    @Transactional(readOnly = true)
    public DenunciaDetalleDTO detalle(String codigo, Long idCiudadano) {
        Denuncia d = denunciaPropia(codigo, idCiudadano);
        List<SeguimientoDenuncia> seguimientos = seguimientoRepository.findByIdDenunciaOrderByFechaHoraAsc(d.getId());

        // Funcionario responsable: solo nombre, cargo y dependencia
        Usuario u = d.getResponsable();
        Responsable responsable = u == null ? null : new Responsable(u.getNombreCompleto(), u.getRango(), u.getDependencia());

        // Documentos: se consideran disponibles los que ya fueron validados
        List<DocumentoItem> documentos = documentoRepository.findByCodigoDenunciaOrderByFechaAscIdAsc(d.getCodigo()).stream()
                .map(doc -> new DocumentoItem(doc.getNombre(), doc.getFecha(), "Validado".equals(doc.getEstado())))
                .toList();

        return new DenunciaDetalleDTO(
                d.getCodigo(), d.getTipoDelito(), d.getDescripcion(), d.getLugarHechos(),
                d.getFechaRegistro(), d.getUltimaActualizacion(),
                d.getEstadoPolicial(), explicar(d.getEstadoPolicial()),
                d.getEstadoFiscal(), d.getCarpetaFiscal(), d.getDespachoFiscal(), d.getFiscalResponsable(),
                responsable, documentos, historial(d, seguimientos), avanceFiscal(d, seguimientos));
    }

    /**
     * Avisos del ciudadano. No se guardan en una tabla propia: se calculan a
     * partir de lo que ya existe (registros, cambios de estado y mensajes sin leer).
     */
    @Transactional(readOnly = true)
    public List<NotificacionDTO> notificaciones(Long idCiudadano) {
        List<NotificacionDTO> avisos = new ArrayList<>();
        for (Denuncia d : denunciaRepository.findByCiudadanoIdOrderByFechaRegistroDesc(idCiudadano)) {
            // Aviso de registro
            avisos.add(new NotificacionDTO(d.getFechaRegistro(), "REGISTRO", "Registro confirmado · " + d.getCodigo(),
                    "Tu denuncia fue registrada. Conserva el código para futuras consultas."));

            // Un aviso por cada cambio de estado
            for (SeguimientoDenuncia s : seguimientoRepository.findByIdDenunciaOrderByFechaHoraAsc(d.getId())) {
                avisos.add(new NotificacionDTO(s.getFechaHora(), "ESTADO", "Se actualizó el estado de tu denuncia " + d.getCodigo(),
                        (s.esFiscal() ? "La Fiscalía" : "La PNP") + " registró el estado «" + s.getEstado()
                                + "». Consulta el historial para revisar el detalle disponible."));
            }

            // Mensajes del funcionario que el ciudadano todavía no ha leído
            Conversacion conversacion = conversacionRepository
                    .findFirstByTipoAndReferenciaAndIdCiudadanoOrderByIdAsc(Conversacion.TIPO_CIUDADANO, d.getCodigo(), idCiudadano)
                    .orElse(null);
            if (conversacion != null) {
                List<Mensaje> sinLeer = mensajeRepository.findByIdConversacionOrderByFechaHoraAscIdAsc(conversacion.getId()).stream()
                        .filter(m -> !m.esDelCiudadano() && !m.isLeido())
                        .toList();
                if (!sinLeer.isEmpty()) {
                    avisos.add(new NotificacionDTO(sinLeer.get(sinLeer.size() - 1).getFechaHora(), "MENSAJE",
                            "Tienes " + sinLeer.size() + " mensaje(s) nuevo(s) · " + d.getCodigo(),
                            "El funcionario responsable respondió tu consulta. Abre la denuncia para leer el chat."));
                }
            }
        }
        // Del aviso más reciente al más antiguo; se muestran como máximo 30
        avisos.sort(Comparator.comparing(NotificacionDTO::fechaHora).reversed());
        return avisos.size() > 30 ? avisos.subList(0, 30) : avisos;
    }

    // ------------------------------------------------------------------
    // Métodos de apoyo
    // ------------------------------------------------------------------

    /** Línea de tiempo: registro + cambios de estado + actuaciones, en orden cronológico. */
    private List<Evento> historial(Denuncia d, List<SeguimientoDenuncia> seguimientos) {
        List<Evento> eventos = new ArrayList<>();
        eventos.add(new Evento(d.getFechaRegistro(), true, "Denuncia registrada", "Se generó la constancia de recepción."));

        for (SeguimientoDenuncia s : seguimientos) {
            String detalle = (s.esFiscal() ? "Actualización registrada por la Fiscalía." : "Actualización registrada por la PNP.");
            // La observación del funcionario solo se muestra si está habilitado en la configuración
            if (mostrarObservaciones && s.getObservacion() != null && !s.getObservacion().isBlank()) {
                detalle += " " + s.getObservacion();
            }
            eventos.add(new Evento(s.getFechaHora(), true, "Estado: " + s.getEstado(), detalle));
        }

        for (Actuacion a : actuacionRepository.findByCodigoDenunciaOrderByFechaAsc(d.getCodigo())) {
            // De las actuaciones solo se conoce el día, no la hora
            eventos.add(new Evento(a.getFecha().atStartOfDay(), false, a.getDescripcion(),
                    "Actuación " + ("FISCAL".equals(a.getPerfil()) ? "fiscal" : "policial") + " · " + a.getEstado()));
        }

        eventos.sort(Comparator.comparing(Evento::fechaHora));
        return eventos;
    }

    /**
     * Etapas de la investigación fiscal, deducidas del estado que registró la Fiscalía:
     * En investigación -> Diligencias -> Con disposición / Archivado.
     */
    private List<PasoFiscal> avanceFiscal(Denuncia d, List<SeguimientoDenuncia> seguimientos) {
        String estado = d.getEstadoFiscal();
        boolean fiscalAsignado = d.getFiscalResponsable() != null && !d.getFiscalResponsable().isBlank();
        boolean enInvestigacion = "En investigación".equals(estado);
        boolean enDiligencias = "Diligencias".equals(estado);
        boolean conDecision = "Con disposición".equals(estado) || "Archivado".equals(estado);

        // Fecha de la decisión: el último cambio de estado que registró la Fiscalía
        LocalDateTime fechaDecision = null;
        if (conDecision) {
            fechaDecision = seguimientos.stream().filter(SeguimientoDenuncia::esFiscal)
                    .map(SeguimientoDenuncia::getFechaHora).reduce((primero, ultimo) -> ultimo).orElse(null);
        }

        return List.of(
                new PasoFiscal("Denuncia recibida", "COMPLETADO", d.getFechaRegistro()),
                new PasoFiscal("Asignación de fiscal", fiscalAsignado ? "COMPLETADO" : "PENDIENTE", null),
                new PasoFiscal("Investigación preliminar",
                        enInvestigacion ? "EN_PROCESO" : (enDiligencias || conDecision) ? "COMPLETADO" : "PENDIENTE", null),
                new PasoFiscal("Diligencias y evaluación de evidencias",
                        enDiligencias ? "EN_PROCESO" : conDecision ? "COMPLETADO" : "PENDIENTE", null),
                new PasoFiscal("Decisión fiscal", conDecision ? "COMPLETADO" : "PENDIENTE", fechaDecision));
    }

    /** Explica en lenguaje sencillo qué significa cada estado. */
    private String explicar(String estado) {
        return switch (estado == null ? "" : estado) {
            case "En proceso" -> "La denuncia continúa su proceso. No significa que exista una decisión final.";
            case "En investigación" -> "La autoridad está reuniendo información sobre los hechos denunciados.";
            case "Por resolver" -> "Las principales diligencias ya se realizaron y la denuncia está pendiente de una decisión.";
            case "Con disposición" -> "La Fiscalía emitió una disposición sobre la denuncia. Puedes pedir orientación al funcionario responsable.";
            case "Resuelto" -> "La atención policial de la denuncia ha concluido.";
            case "Archivado" -> "La denuncia fue archivada. Puedes pedir orientación al funcionario responsable.";
            default -> "La denuncia se encuentra registrada y en seguimiento.";
        };
    }
}
