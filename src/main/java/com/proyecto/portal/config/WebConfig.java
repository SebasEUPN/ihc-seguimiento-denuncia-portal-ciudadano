package com.proyecto.portal.config;

import com.proyecto.portal.security.SesionInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Configuración general de la capa web. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SesionInterceptor sesionInterceptor;

    public WebConfig(SesionInterceptor sesionInterceptor) {
        this.sesionInterceptor = sesionInterceptor;
    }

    /**
     * Protege todas las rutas /api/portal/** excepto /api/portal/acceso/**
     * (consulta del código, verificación del PIN y cierre), que deben poder
     * usarse antes de estar verificado.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sesionInterceptor)
                .addPathPatterns("/api/portal/**")
                .excludePathPatterns("/api/portal/acceso/**");
    }

    /** Codificador BCrypt para cifrar y comparar el PIN del ciudadano. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
