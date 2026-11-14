package com.proyecto.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Funcionario registrado en el primer prototipo (tabla "usuario").
 * El portal solo LEE sus datos institucionales para mostrar quién es el
 * responsable de la denuncia; por eso no se mapean la contraseña ni los
 * datos personales (DNI, teléfono, dirección) y la clase no tiene setters.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "codigo")
    private String codigo;

    @Column(name = "nombres")
    private String nombres;

    @Column(name = "apellidos")
    private String apellidos;

    // ADMIN, POLICIA o FISCAL
    @Column(name = "rol")
    private String rol;

    @Column(name = "rango")
    private String rango;

    @Column(name = "dependencia")
    private String dependencia;

    @Column(name = "habilitado")
    private boolean habilitado;

    // Último inicio de sesión en el sistema de gestión (para saber si está "en línea")
    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    /** Constructor vacío obligatorio para JPA. */
    public Usuario() {
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    // ----- Getters -----
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getRol() { return rol; }
    public String getRango() { return rango; }
    public String getDependencia() { return dependencia; }
    public boolean isHabilitado() { return habilitado; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
}
