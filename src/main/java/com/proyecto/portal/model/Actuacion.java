package com.proyecto.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Actuación policial o fiscal de una denuncia (tabla "actuacion"). Solo lectura.
 */
@Entity
@Table(name = "actuacion")
public class Actuacion {

    @Id
    @Column(name = "id_actuacion")
    private Long id;

    @Column(name = "codigo_denuncia")
    private String codigoDenuncia;

    // POLICIA o FISCAL
    @Column(name = "perfil")
    private String perfil;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha")
    private LocalDate fecha;

    // Completada, En proceso, Programada o Pendiente
    @Column(name = "estado")
    private String estado;

    /** Constructor vacío obligatorio para JPA. */
    public Actuacion() {
    }

    // ----- Getters -----
    public Long getId() { return id; }
    public String getCodigoDenuncia() { return codigoDenuncia; }
    public String getPerfil() { return perfil; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFecha() { return fecha; }
    public String getEstado() { return estado; }
}
