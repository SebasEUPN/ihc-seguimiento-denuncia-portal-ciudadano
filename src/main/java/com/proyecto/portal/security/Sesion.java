package com.proyecto.portal.security;

import com.proyecto.portal.exception.ApiException;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;

/**
 * Utilidades para manejar la sesión del ciudadano.
 * La sesión vive en el servidor; el navegador solo guarda una cookie con
 * su identificador. En ella se recuerdan dos datos:
 *  - el código de denuncia que se está intentando consultar (antes del PIN)
 *  - el id del ciudadano, una vez que el PIN fue verificado
 */
public final class Sesion {

    private static final String ATRIBUTO_CIUDADANO = "idCiudadano";
    private static final String ATRIBUTO_CODIGO_PENDIENTE = "codigoPendiente";

    // Clase de utilidades: no se crean objetos de ella
    private Sesion() {
    }

    /** Recuerda el código escrito en el paso 1, a la espera del PIN. */
    public static void guardarCodigoPendiente(HttpSession sesion, String codigo) {
        sesion.setAttribute(ATRIBUTO_CODIGO_PENDIENTE, codigo);
    }

    /** Código que espera la verificación del PIN (null si no se hizo el paso 1). */
    public static String codigoPendiente(HttpSession sesion) {
        return (String) sesion.getAttribute(ATRIBUTO_CODIGO_PENDIENTE);
    }

    /** Marca la sesión como verificada para ese ciudadano. */
    public static void iniciar(HttpSession sesion, Long idCiudadano) {
        sesion.removeAttribute(ATRIBUTO_CODIGO_PENDIENTE);
        sesion.setAttribute(ATRIBUTO_CIUDADANO, idCiudadano);
    }

    /** Id del ciudadano verificado, o null si todavía no validó su PIN. */
    public static Long buscar(HttpSession sesion) {
        return (Long) sesion.getAttribute(ATRIBUTO_CIUDADANO);
    }

    /** Id del ciudadano verificado; si no hay sesión responde 401. */
    public static Long actual(HttpSession sesion) {
        Long id = buscar(sesion);
        if (id == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Tu consulta se cerró por seguridad. Ingresa nuevamente tu código y PIN.");
        }
        return id;
    }
}
