package com.proyecto.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
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
// El rango/cargo y la dependencia están en la tabla del perfil ("policia" o
// "fiscal"), unidas 1 a 1 con "usuario". Hibernate las lee con LEFT JOIN.
@SecondaryTable(name = "policia", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id_usuario"))
@SecondaryTable(name = "fiscal", pkJoinColumns = @PrimaryKeyJoinColumn(name = "id_usuario"))
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

    // ----- Tabla "policia" -----
    @Column(name = "rango", table = "policia")
    private String policiaRango;

    @Column(name = "dependencia", table = "policia")
    private String policiaDependencia;

    // ----- Tabla "fiscal" -----
    @Column(name = "cargo", table = "fiscal")
    private String fiscalCargo;

    @Column(name = "despacho", table = "fiscal")
    private String fiscalDespacho;

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
    /** Rango del policía o cargo del fiscal. */
    public String getRango() { return "FISCAL".equals(rol) ? fiscalCargo : policiaRango; }
    /** Dependencia policial o despacho fiscal. */
    public String getDependencia() { return "FISCAL".equals(rol) ? fiscalDespacho : policiaDependencia; }
    public boolean isHabilitado() { return habilitado; }
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
}
