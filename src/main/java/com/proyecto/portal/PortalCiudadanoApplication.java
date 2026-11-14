package com.proyecto.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal del Portal Ciudadano (segundo prototipo).
 * Al ejecutarla se publica la aplicación en http://localhost:8081 y se
 * conecta a la misma base de datos del Sistema de Gestión (BD_Denuncias).
 */
@SpringBootApplication
@EnableScheduling
public class PortalCiudadanoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalCiudadanoApplication.class, args);
    }
}
