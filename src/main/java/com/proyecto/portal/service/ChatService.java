package com.proyecto.portal.service;

import com.proyecto.portal.dto.ChatDTO;
import com.proyecto.portal.exception.ApiException;
import com.proyecto.portal.model.Conversacion;
import com.proyecto.portal.model.Denuncia;
import com.proyecto.portal.model.Mensaje;
import com.proyecto.portal.model.Usuario;
import com.proyecto.portal.repository.ConversacionRepository;
import com.proyecto.portal.repository.MensajeRepository;
import com.proyecto.portal.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chat del ciudadano con el funcionario responsable de su denuncia.
 *
 * Usa las mismas tablas (conversacion y mensaje) que el "Chat institucional"
 * del primer prototipo: lo que el ciudadano escribe aquí le aparece al
 * funcionario allá, y sus respuestas aparecen aquí.
 */
@Service
public class ChatService {

    private final SeguimientoService seguimientoService;
    private final ConversacionRepository conversacionRepository;
    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;

    public ChatService(SeguimientoService seguimientoService, ConversacionRepository conversacionRepository,
                       MensajeRepository mensajeRepository, UsuarioRepository usuarioRepository) {
        this.seguimientoService = seguimientoService;
        this.conversacionRepository = conversacionRepository;
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Devuelve la conversación sobre una denuncia. Al consultarla, los
     * mensajes del funcionario quedan marcados como leídos.
     */
    @Transactional
    public ChatDTO obtener(String codigo, Long idCiudadano) {
        Denuncia denuncia = seguimientoService.denunciaPropia(codigo, idCiudadano);
        Conversacion conversacion = buscarConversacion(denuncia, idCiudadano);

        // Quien atiende: el funcionario de la conversación o, si aún no existe, el responsable de la denuncia
        Usuario funcionario = conversacion != null
                ? usuarioRepository.findById(conversacion.getIdUsuarioA()).orElse(denuncia.getResponsable())
                : denuncia.getResponsable();
        if (funcionario == null) {
            return new ChatDTO(false, null, false, List.of());
        }

        List<Mensaje> mensajes = conversacion == null ? List.of()
                : mensajeRepository.findByIdConversacionOrderByFechaHoraAscIdAsc(conversacion.getId());

        List<ChatDTO.MensajeItem> lista = mensajes.stream().map(m -> new ChatDTO.MensajeItem(
                m.getId(), m.esDelCiudadano(), m.getTexto(), m.getFechaHora(),
                m.getAdjuntoArchivo() == null ? null : new ChatDTO.Adjunto(
                        m.getAdjuntoNombre(),
                        m.getAdjuntoTamano() == null ? 0 : m.getAdjuntoTamano(),
                        "/api/portal/adjuntos/" + m.getId())
        )).toList();

        // Los mensajes del funcionario pasan a "leído" (Hibernate guarda el cambio al terminar la transacción)
        mensajes.stream().filter(m -> !m.esDelCiudadano() && !m.isLeido()).forEach(m -> m.setLeido(true));

        // "En línea" = ingresó al sistema de gestión en los últimos 15 minutos
        boolean enLinea = funcionario.getUltimoAcceso() != null
                && funcionario.getUltimoAcceso().isAfter(LocalDateTime.now().minusMinutes(15));

        return new ChatDTO(true, nombreInstitucional(funcionario), enLinea, lista);
    }

    /** Envía un mensaje del ciudadano. Si es el primero, se crea la conversación con el responsable. */
    @Transactional
    public void enviar(String codigo, Long idCiudadano, String texto) {
        String contenido = texto == null ? "" : texto.trim();
        if (contenido.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escribe tu mensaje para continuar.");
        }
        if (contenido.length() > 2000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El mensaje admite como máximo 2000 caracteres.");
        }

        Denuncia denuncia = seguimientoService.denunciaPropia(codigo, idCiudadano);
        Conversacion conversacion = buscarConversacion(denuncia, idCiudadano);
        if (conversacion == null) {
            if (denuncia.getResponsable() == null) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Tu denuncia aún no tiene un funcionario asignado. Inténtalo más adelante.");
            }
            conversacion = conversacionRepository.save(
                    new Conversacion(denuncia.getCodigo(), denuncia.getResponsable().getId(), idCiudadano));
        }
        mensajeRepository.save(new Mensaje(conversacion.getId(), contenido, LocalDateTime.now()));
    }

    /**
     * Devuelve un mensaje con archivo adjunto solo si pertenece a una
     * conversación del ciudadano de la sesión.
     */
    @Transactional(readOnly = true)
    public Mensaje adjunto(Long idMensaje, Long idCiudadano) {
        Mensaje mensaje = mensajeRepository.findById(idMensaje).orElse(null);
        Conversacion conversacion = mensaje == null ? null
                : conversacionRepository.findById(mensaje.getIdConversacion()).orElse(null);
        if (mensaje == null || mensaje.getAdjuntoArchivo() == null || conversacion == null
                || !idCiudadano.equals(conversacion.getIdCiudadano())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "El archivo adjunto no existe.");
        }
        return mensaje;
    }

    // ------------------------------------------------------------------
    // Métodos de apoyo
    // ------------------------------------------------------------------

    private Conversacion buscarConversacion(Denuncia denuncia, Long idCiudadano) {
        return conversacionRepository
                .findFirstByTipoAndReferenciaAndIdCiudadanoOrderByIdAsc(Conversacion.TIPO_CIUDADANO, denuncia.getCodigo(), idCiudadano)
                .orElse(null);
    }

    /** "Suboficial de Primera Carlos Mendoza Ruiz". */
    private String nombreInstitucional(Usuario u) {
        return (u.getRango() == null || u.getRango().isBlank() ? "" : u.getRango() + " ") + u.getNombreCompleto();
    }
}
