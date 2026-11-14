package com.proyecto.portal.repository;

import com.proyecto.portal.model.Actuacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a la tabla "actuacion". */
public interface ActuacionRepository extends JpaRepository<Actuacion, Long> {

    // Actuaciones registradas en una denuncia
    List<Actuacion> findByCodigoDenunciaOrderByFechaAsc(String codigoDenuncia);
}
