package com.proyecto.portal.controller;

import com.proyecto.portal.dto.ChatDTO;
import com.proyecto.portal.dto.DenunciaDetalleDTO;
import com.proyecto.portal.dto.DenunciaResumenDTO;
import com.proyecto.portal.dto.MensajeRequest;
import com.proyecto.portal.dto.NotificacionDTO;
import com.proyecto.portal.model.Mensaje;
import com.proyecto.portal.security.Sesion;
import com.proyecto.portal.service.ArchivoService;
import com.proyecto.portal.service.ChatService;
import com.proyecto.portal.service.SeguimientoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Seguimiento de las denuncias del ciudadano verificado.
 * El ciudadano siempre se toma de la sesión, nunca de un dato enviado por
 * el navegador: así cada persona solo puede ver sus propios expedientes.
 */
@RestController
@RequestMapping("/api/portal")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;
    private final ChatService chatService;
    private final ArchivoService archivoService;

    public SeguimientoController(SeguimientoService seguimientoService, ChatService chatService,
                                 ArchivoService archivoService) {
        this.seguimientoService = seguimientoService;
        this.chatService = chatService;
        this.archivoService = archivoService;
    }

    /** GET /api/portal/denuncias — "Mis denuncias registradas". */
    @GetMapping("/denuncias")
    public List<DenunciaResumenDTO> listar(HttpSession sesion) {
        return seguimientoService.listar(Sesion.actual(sesion));
    }

    /** GET /api/portal/denuncias/{codigo} — detalle, documentos, historial y avance fiscal. */
    @GetMapping("/denuncias/{codigo}")
    public DenunciaDetalleDTO detalle(@PathVariable("codigo") String codigo, HttpSession sesion) {
        return seguimientoService.detalle(codigo, Sesion.actual(sesion));
    }

    /** GET /api/portal/denuncias/{codigo}/chat — conversación con el funcionario responsable. */
    @GetMapping("/denuncias/{codigo}/chat")
    public ChatDTO chat(@PathVariable("codigo") String codigo, HttpSession sesion) {
        return chatService.obtener(codigo, Sesion.actual(sesion));
    }

    /** POST /api/portal/denuncias/{codigo}/chat — envía un mensaje y devuelve la conversación actualizada. */
    @PostMapping("/denuncias/{codigo}/chat")
    public ChatDTO enviar(@PathVariable("codigo") String codigo, @RequestBody MensajeRequest datos, HttpSession sesion) {
        Long idCiudadano = Sesion.actual(sesion);
        chatService.enviar(codigo, idCiudadano, datos.texto());
        return chatService.obtener(codigo, idCiudadano);
    }

    /** GET /api/portal/notificaciones — avisos de todas las denuncias del ciudadano. */
    @GetMapping("/notificaciones")
    public List<NotificacionDTO> notificaciones(HttpSession sesion) {
        return seguimientoService.notificaciones(Sesion.actual(sesion));
    }

    /**
     * GET /api/portal/adjuntos/{idMensaje} — descarga un archivo que el funcionario
     * envió por el chat. Siempre se entrega como descarga (nunca se abre dentro de
     * la página), para que el navegador no ejecute un archivo subido por otra persona.
     */
    @GetMapping("/adjuntos/{idMensaje}")
    public ResponseEntity<Resource> adjunto(@PathVariable("idMensaje") Long idMensaje, HttpSession sesion) {
        Mensaje mensaje = chatService.adjunto(idMensaje, Sesion.actual(sesion));
        String nombre = mensaje.getAdjuntoNombre() == null ? "archivo" : mensaje.getAdjuntoNombre();
        ContentDisposition disposicion = ContentDisposition.attachment().filename(nombre, StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(archivoService.cargar(mensaje.getAdjuntoArchivo()));
    }
}
