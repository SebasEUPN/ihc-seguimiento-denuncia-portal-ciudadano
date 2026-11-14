package com.proyecto.portal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Convierte las excepciones de los controladores en una respuesta JSON
 * uniforme: { "mensaje": "texto para el ciudadano" }.
 */
@RestControllerAdvice
public class ManejadorErrores {

    /** Errores controlados (código inexistente, PIN incorrecto, sin sesión...). */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> manejarApi(ApiException e) {
        return ResponseEntity.status(e.getEstado()).body(Map.of("mensaje", e.getMessage()));
    }

    /** El cuerpo de la petición no es un JSON válido. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "Los datos enviados no tienen un formato válido."));
    }
}
