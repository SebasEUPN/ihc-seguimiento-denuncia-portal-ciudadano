package com.proyecto.portal.repository;

import com.proyecto.portal.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a la tabla "documento". */
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    // Documentos y evidencias de una denuncia
    List<Documento> findByCodigoDenunciaOrderByFechaAscIdAsc(String codigoDenuncia);
}
