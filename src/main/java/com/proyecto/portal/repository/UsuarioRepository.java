package com.proyecto.portal.repository;

import com.proyecto.portal.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso (solo lectura) a la tabla "usuario". */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Funcionarios habilitados de un perfil (ej.: "POLICIA")
    List<Usuario> findByRolAndHabilitadoTrueOrderByIdAsc(String rol);
}
