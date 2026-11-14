package com.proyecto.portal.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Se ejecuta ANTES de cada controlador de /api/portal/**.
 * Si el ciudadano no ha validado su código y PIN corta la petición con 401,
 * así ningún dato del expediente puede consultarse sin verificación.
 */
@Component
public class SesionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Sesion.actual(request.getSession()); // lanza 401 si no hay ciudadano verificado
        return true;                         // la petición continúa hacia el controlador
    }
}
