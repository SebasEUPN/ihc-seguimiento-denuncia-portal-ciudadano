package com.proyecto.portal.service;

import com.proyecto.portal.exception.ApiException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lee los archivos que el funcionario adjuntó en el chat.
 *
 * Versión en la nube: el primer prototipo guarda el contenido en la tabla
 * archivo_almacenado de la base compartida (ya no en su carpeta "uploads",
 * porque cada prototipo corre en un servidor distinto); aquí solo se lee.
 */
@Service
public class ArchivoService {

    private final JdbcTemplate jdbc;

    public ArchivoService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Devuelve el archivo para enviarlo al navegador. */
    public Resource cargar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "El archivo no está disponible en este momento.");
        }
        List<byte[]> filas = jdbc.query("SELECT contenido FROM archivo_almacenado WHERE nombre = ?",
                (rs, i) -> rs.getBytes(1), nombre);
        if (filas.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "El archivo no está disponible en este momento.");
        }
        return new ByteArrayResource(filas.get(0));
    }
}
