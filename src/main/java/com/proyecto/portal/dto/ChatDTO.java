package com.proyecto.portal.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Conversación del ciudadano con el funcionario responsable de una denuncia. */
public record ChatDTO(
        boolean disponible,        // false si la denuncia aún no tiene funcionario asignado
        String funcionario,        // nombre del funcionario que atiende
        boolean enLinea,           // true si ingresó al sistema de gestión hace poco
        List<MensajeItem> mensajes
) {

    /** Mensaje del chat. mio = true si lo escribió el ciudadano. */
    public record MensajeItem(Long id, boolean mio, String texto, LocalDateTime fechaHora, Adjunto adjunto) {
    }

    /** Archivo que el funcionario adjuntó al mensaje. */
    public record Adjunto(String nombre, long tamano, String url) {
    }
}
