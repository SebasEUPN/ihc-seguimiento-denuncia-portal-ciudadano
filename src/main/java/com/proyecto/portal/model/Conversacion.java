package com.proyecto.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Conversación del chat (tabla "conversacion"). El portal solo usa las de
 * tipo CIUDADANO: un funcionario (id_usuario_a) y un ciudadano, sobre una
 * denuncia (referencia). Es la misma conversación que el funcionario ve en
 * el "Chat institucional" del primer prototipo.
 */
@Entity
@Table(name = "conversacion")
public class Conversacion {

    public static final String TIPO_CIUDADANO = "CIUDADANO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversacion")
    private Long id;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    // Código de la denuncia sobre la que se conversa
    @Column(name = "referencia", nullable = false, length = 40)
    private String referencia;

    // Funcionario que atiende al ciudadano
    @Column(name = "id_usuario_a", nullable = false)
    private Long idUsuarioA;

    @Column(name = "id_ciudadano")
    private Long idCiudadano;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Constructor vacío obligatorio para JPA. */
    public Conversacion() {
    }

    /** Crea una conversación nueva entre un funcionario y un ciudadano. */
    public Conversacion(String referencia, Long idUsuarioA, Long idCiudadano) {
        this.tipo = TIPO_CIUDADANO;
        this.referencia = referencia;
        this.idUsuarioA = idUsuarioA;
        this.idCiudadano = idCiudadano;
    }

    // ----- Getters -----
    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getReferencia() { return referencia; }
    public Long getIdUsuarioA() { return idUsuarioA; }
    public Long getIdCiudadano() { return idCiudadano; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
