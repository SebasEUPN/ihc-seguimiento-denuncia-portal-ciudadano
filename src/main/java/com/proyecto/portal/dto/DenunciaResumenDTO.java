package com.proyecto.portal.dto;

import com.proyecto.portal.model.Denuncia;

import java.time.LocalDateTime;

/** Fila de la lista "Mis denuncias registradas". */
public record DenunciaResumenDTO(String codigo, String tipoDelito, String estado, LocalDateTime fechaRegistro) {

    public static DenunciaResumenDTO de(Denuncia d) {
        return new DenunciaResumenDTO(d.getCodigo(), d.getTipoDelito(), d.getEstadoPolicial(), d.getFechaRegistro());
    }
}
