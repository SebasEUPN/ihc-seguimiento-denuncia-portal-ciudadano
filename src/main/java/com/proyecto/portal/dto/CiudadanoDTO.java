package com.proyecto.portal.dto;

import com.proyecto.portal.model.Ciudadano;

/**
 * Datos personales que se muestran al ciudadano ya verificado.
 * Se usa un DTO (objeto de transferencia) para que nunca salgan del
 * servidor el PIN cifrado ni los contadores de intentos.
 */
public record CiudadanoDTO(String nombreCompleto, String dni, String correo, String telefono) {

    public static CiudadanoDTO de(Ciudadano c) {
        return new CiudadanoDTO(c.getNombreCompleto(), c.getDni(), c.getCorreo(), c.getTelefono());
    }
}
