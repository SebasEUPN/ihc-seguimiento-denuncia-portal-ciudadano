package com.proyecto.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Documento o evidencia registrado en una denuncia (tabla "documento"). Solo lectura.
 */
@Entity
@Table(name = "documento")
public class Documento {

    @Id
    @Column(name = "id_documento")
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "codigo_denuncia")
    private String codigoDenuncia;

    @Column(name = "fecha")
    private LocalDate fecha;

    // Validado, Pendiente o Remitido
    @Column(name = "estado")
    private String estado;

    /** Constructor vacío obligatorio para JPA. */
    public Documento() {
    }

    // ----- Getters -----
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCodigoDenuncia() { return codigoDenuncia; }
    public LocalDate getFecha() { return fecha; }
    public String getEstado() { return estado; }
}
