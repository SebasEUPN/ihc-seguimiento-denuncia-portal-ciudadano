package com.proyecto.portal.repository;

import com.proyecto.portal.model.Ciudadano;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a la tabla "ciudadano". Spring Data crea las consultas a partir del nombre de cada método. */
public interface CiudadanoRepository extends JpaRepository<Ciudadano, Long> {

    // Ciudadanos que todavía no tienen PIN asignado
    List<Ciudadano> findByPinHashIsNull();
}
