package com.proyecto.portal.repository;

import com.proyecto.portal.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acceso a la tabla "denuncia". */
public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    // Busca una denuncia por el código que escribe el ciudadano
    Optional<Denuncia> findByCodigo(String codigo);

    // ¿Existe una denuncia con ese código?
    boolean existsByCodigo(String codigo);

    // "Mis denuncias": todas las de un ciudadano, de la más reciente a la más antigua
    List<Denuncia> findByCiudadanoIdOrderByFechaRegistroDesc(Long idCiudadano);

    // Denuncias que todavía no tienen funcionario responsable
    List<Denuncia> findByResponsableIsNullOrderByIdAsc();
}
