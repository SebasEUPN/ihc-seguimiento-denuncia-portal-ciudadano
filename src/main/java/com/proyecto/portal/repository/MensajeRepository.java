package com.proyecto.portal.repository;

import com.proyecto.portal.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a la tabla "mensaje". */
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    // Mensajes de una conversación en orden cronológico
    List<Mensaje> findByIdConversacionOrderByFechaHoraAscIdAsc(Long idConversacion);
}
