package com.proyecto.portal.repository;

import com.proyecto.portal.model.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Acceso a la tabla "conversacion". */
public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {

    // Conversación de un ciudadano sobre una denuncia (si ya existe)
    Optional<Conversacion> findFirstByTipoAndReferenciaAndIdCiudadanoOrderByIdAsc(String tipo, String referencia, Long idCiudadano);
}
