package com.proyecto.portal.dto;

import java.time.LocalDateTime;

/** Aviso de la pantalla "Notificaciones y novedades". tipo: REGISTRO, ESTADO o MENSAJE. */
public record NotificacionDTO(LocalDateTime fechaHora, String tipo, String titulo, String detalle) {
}
