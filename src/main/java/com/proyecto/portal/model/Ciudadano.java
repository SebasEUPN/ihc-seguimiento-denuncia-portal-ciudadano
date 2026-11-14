package com.proyecto.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Ciudadano que presentó una denuncia (tabla "ciudadano").
 * Además de los datos del primer prototipo, el portal usa las columnas
 * del PIN agregadas por el script 02_portal_ciudadano.sql.
 */
@Entity
@Table(name = "ciudadano")
public class Ciudadano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ciudadano")
    private Long id;

    @Column(name = "nombres", nullable = false, length = 80)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 80)
    private String apellidos;

    @Column(name = "dni", nullable = false, unique = true, length = 8)
    private String dni;

    @Column(name = "correo", length = 120)
    private String correo;

    @Column(name = "telefono", length = 15)
    private String telefono;

    // PIN de acceso cifrado con BCrypt (null = todavía no tiene PIN)
    @Column(name = "pin_hash", length = 100)
    private String pinHash;

    // Intentos fallidos seguidos al escribir el PIN
    @Column(name = "pin_intentos", nullable = false)
    private int pinIntentos;

    // Si tiene valor, no se aceptan más intentos hasta esa fecha y hora
    @Column(name = "pin_bloqueado_hasta")
    private LocalDateTime pinBloqueadoHasta;

    /** Constructor vacío obligatorio para JPA. */
    public Ciudadano() {
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    // ----- Getters y setters -----
    public Long getId() { return id; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getDni() { return dni; }
    public String getCorreo() { return correo; }
    public String getTelefono() { return telefono; }
    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }
    public int getPinIntentos() { return pinIntentos; }
    public void setPinIntentos(int pinIntentos) { this.pinIntentos = pinIntentos; }
    public LocalDateTime getPinBloqueadoHasta() { return pinBloqueadoHasta; }
    public void setPinBloqueadoHasta(LocalDateTime pinBloqueadoHasta) { this.pinBloqueadoHasta = pinBloqueadoHasta; }
}
