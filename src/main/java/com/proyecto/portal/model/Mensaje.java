package com.proyecto.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Mensaje del chat (tabla "mensaje").
 * id_usuario_remitente = NULL significa que lo escribió el ciudadano;
 * con valor, lo escribió el funcionario desde el primer prototipo.
 */
@Entity
@Table(name = "mensaje")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long id;

    @Column(name = "id_conversacion", nullable = false)
    private Long idConversacion;

    @Column(name = "id_usuario_remitente")
    private Long idUsuarioRemitente;

    @Column(name = "texto", length = 2000)
    private String texto;

    // Datos del archivo adjunto que pudo enviar el funcionario (null si es solo texto)
    @Column(name = "adjunto_nombre", length = 200)
    private String adjuntoNombre;

    @Column(name = "adjunto_tamano")
    private Long adjuntoTamano;

    @Column(name = "adjunto_archivo", length = 100)
    private String adjuntoArchivo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "leido", nullable = false)
    private boolean leido;

    /** Constructor vacío obligatorio para JPA. */
    public Mensaje() {
    }

    /** Crea un mensaje de texto escrito por el ciudadano. */
    public Mensaje(Long idConversacion, String texto, LocalDateTime fechaHora) {
        this.idConversacion = idConversacion;
        this.idUsuarioRemitente = null; // null = ciudadano
        this.texto = texto;
        this.fechaHora = fechaHora;
        this.leido = false;             // el funcionario todavía no lo ha leído
    }

    /** true si el mensaje lo escribió el ciudadano. */
    public boolean esDelCiudadano() {
        return idUsuarioRemitente == null;
    }

    // ----- Getters y setters -----
    public Long getId() { return id; }
    public Long getIdConversacion() { return idConversacion; }
    public Long getIdUsuarioRemitente() { return idUsuarioRemitente; }
    public String getTexto() { return texto; }
    public String getAdjuntoNombre() { return adjuntoNombre; }
    public Long getAdjuntoTamano() { return adjuntoTamano; }
    public String getAdjuntoArchivo() { return adjuntoArchivo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}
