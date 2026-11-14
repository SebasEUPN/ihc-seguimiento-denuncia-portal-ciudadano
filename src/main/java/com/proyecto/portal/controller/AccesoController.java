package com.proyecto.portal.controller;

import com.proyecto.portal.dto.CiudadanoDTO;
import com.proyecto.portal.dto.ConsultaRequest;
import com.proyecto.portal.dto.PinRequest;
import com.proyecto.portal.exception.ApiException;
import com.proyecto.portal.model.Ciudadano;
import com.proyecto.portal.security.Sesion;
import com.proyecto.portal.service.AccesoService;
import com.proyecto.portal.service.SeguimientoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Acceso del ciudadano. Estas rutas (/api/portal/acceso/**) son las únicas
 * que funcionan sin haber verificado el PIN.
 */
@RestController
@RequestMapping("/api/portal/acceso")
public class AccesoController {

    private final AccesoService accesoService;
    private final SeguimientoService seguimientoService;

    public AccesoController(AccesoService accesoService, SeguimientoService seguimientoService) {
        this.accesoService = accesoService;
        this.seguimientoService = seguimientoService;
    }

    /**
     * POST /api/portal/acceso/consulta — paso 1: valida que el código exista.
     * El código queda guardado en la sesión a la espera del PIN.
     */
    @PostMapping("/consulta")
    public Map<String, String> consulta(@RequestBody ConsultaRequest datos, HttpSession sesion) {
        String codigo = accesoService.consultarCodigo(datos.codigo());
        Sesion.guardarCodigoPendiente(sesion, codigo);
        return Map.of("codigo", codigo);
    }

    /**
     * POST /api/portal/acceso/verificar — paso 2: verifica el PIN.
     * El código se toma de la sesión (el del paso 1), no de lo que envíe el navegador.
     */
    @PostMapping("/verificar")
    public Map<String, Object> verificar(@RequestBody PinRequest datos, HttpServletRequest request) {
        HttpSession sesion = request.getSession();
        String codigo = Sesion.codigoPendiente(sesion);
        if (codigo == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Primero ingresa tu código de denuncia.");
        }
        Ciudadano ciudadano = accesoService.verificarPin(codigo, datos.pin());

        // Al verificarse se cambia el identificador de sesión (evita la "fijación de sesión")
        request.changeSessionId();
        Sesion.iniciar(sesion, ciudadano.getId());
        return Map.of("codigo", codigo, "ciudadano", CiudadanoDTO.de(ciudadano));
    }

    /**
     * GET /api/portal/acceso/sesion — el frontend la consulta al cargar la página.
     * Responde 204 (sin contenido) cuando no hay un ciudadano verificado.
     */
    @GetMapping("/sesion")
    public ResponseEntity<CiudadanoDTO> sesion(HttpSession sesion) {
        Long idCiudadano = Sesion.buscar(sesion);
        if (idCiudadano == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(seguimientoService.ciudadano(idCiudadano));
    }

    /** POST /api/portal/acceso/salir — cierra la consulta y borra la sesión. */
    @PostMapping("/salir")
    public Map<String, String> salir(HttpSession sesion) {
        sesion.invalidate();
        return Map.of("mensaje", "Consulta cerrada.");
    }
}
