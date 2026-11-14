package com.proyecto.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Registro del historial de una denuncia (tabla "seguimiento_denuncia").
 * Lo escribe el primer prototipo cada vez que un funcionario cambia el
 * estado; el portal solo lo lee para armar la línea de tiempo.
 */
@Entity
@Table(name = "seguimiento_denuncia")
public class SeguimientoDenuncia {

    @Id
    @Column(name = "id_seguimiento")
    private Long id;

    // Denuncia a la que pertenece el registro
    @Column(name = "id_denuncia")
    private Long idDenuncia;

    // POLICIA o FISCAL: institución que hizo el cambio
    @Column(name = "perfil")
    private String perfil;

    @Column(name = "estado")
    private String estado;

    @Column(name = "observacion")
    private String observacion;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    /** Constructor vacío obligatorio para JPA. */
    public SeguimientoDenuncia() {
    }

    /** true si el cambio lo registró la Fiscalía. */
    public boolean esFiscal() {
        return "FISCAL".equals(perfil);
    }

    // ----- Getters -----
    public Long getId() { return id; }
    public Long getIdDenuncia() { return idDenuncia; }
    public String getPerfil() { return perfil; }
    public String getEstado() { return estado; }
    public String getObservacion() { return observacion; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}
