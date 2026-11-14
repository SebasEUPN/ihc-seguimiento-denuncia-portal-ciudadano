package com.proyecto.portal.repository;

import com.proyecto.portal.model.SeguimientoDenuncia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a la tabla "seguimiento_denuncia". */
public interface SeguimientoDenunciaRepository extends JpaRepository<SeguimientoDenuncia, Long> {

    // Cambios de estado de una denuncia en orden cronológico
    List<SeguimientoDenuncia> findByIdDenunciaOrderByFechaHoraAsc(Long idDenuncia);
}
