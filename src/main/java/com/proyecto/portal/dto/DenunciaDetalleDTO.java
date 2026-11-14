package com.proyecto.portal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Toda la información de una denuncia que puede ver el ciudadano:
 * datos del expediente, estado en la PNP y en la Fiscalía, funcionario
 * responsable, documentos, historial y avance de la investigación fiscal.
 */
public record DenunciaDetalleDTO(
        String codigo,
        String tipoDelito,
        String descripcion,
        String lugarHechos,
        LocalDateTime fechaRegistro,
        LocalDateTime ultimaActualizacion,
        String estado,                 // estado registrado por la PNP
        String explicacion,            // qué significa ese estado, en lenguaje sencillo
        String estadoFiscal,           // estado registrado por la Fiscalía
        String carpetaFiscal,
        String despachoFiscal,
        String fiscalResponsable,
        Responsable responsable,       // null si aún no hay funcionario asignado
        List<DocumentoItem> documentos,
        List<Evento> historial,
        List<PasoFiscal> avanceFiscal
) {

    /** Funcionario policial responsable: solo datos institucionales. */
    public record Responsable(String nombre, String cargo, String dependencia) {
    }

    /** Documento registrado; "disponible" indica si ya fue validado en el expediente. */
    public record DocumentoItem(String nombre, LocalDate fecha, boolean disponible) {
    }

    /** Un hecho de la línea de tiempo. conHora = false cuando solo se conoce el día. */
    public record Evento(LocalDateTime fechaHora, boolean conHora, String titulo, String detalle) {
    }

    /** Etapa de la investigación fiscal. estado: COMPLETADO, EN_PROCESO o PENDIENTE. */
    public record PasoFiscal(String titulo, String estado, LocalDateTime fecha) {
    }
}
