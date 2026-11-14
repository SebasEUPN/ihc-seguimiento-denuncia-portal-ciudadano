package com.proyecto.portal.service;

import com.proyecto.portal.exception.ApiException;
import com.proyecto.portal.model.Ciudadano;
import com.proyecto.portal.model.Denuncia;
import com.proyecto.portal.repository.DenunciaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Acceso del ciudadano en dos pasos:
 *  1. escribe el código de su denuncia;
 *  2. escribe su PIN personal de 6 dígitos.
 * Recién después del paso 2 se muestra información del expediente.
 */
@Service
public class AccesoService {

    private static final String CODIGO_NO_ENCONTRADO =
            "No encontramos una denuncia con ese código. Revisa las letras, números y guiones de tu constancia e inténtalo de nuevo.";

    private final DenunciaRepository denunciaRepository;
    private final PasswordEncoder passwordEncoder;

    /** Intentos fallidos permitidos antes de bloquear (app.portal.pin.max-intentos). */
    private final int maxIntentos;

    /** Minutos que dura el bloqueo (app.portal.pin.minutos-bloqueo). */
    private final int minutosBloqueo;

    public AccesoService(DenunciaRepository denunciaRepository, PasswordEncoder passwordEncoder,
                         @Value("${app.portal.pin.max-intentos:5}") int maxIntentos,
                         @Value("${app.portal.pin.minutos-bloqueo:15}") int minutosBloqueo) {
        this.denunciaRepository = denunciaRepository;
        this.passwordEncoder = passwordEncoder;
        this.maxIntentos = maxIntentos;
        this.minutosBloqueo = minutosBloqueo;
    }

    /**
     * Paso 1: comprueba que el código exista y lo devuelve normalizado
     * (sin espacios y en mayúsculas). No entrega ningún dato de la denuncia.
     */
    @Transactional(readOnly = true)
    public String consultarCodigo(String codigo) {
        String limpio = codigo == null ? "" : codigo.trim().toUpperCase();
        if (limpio.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escribe el código de denuncia para continuar.");
        }
        // El código solo puede tener letras, números y guiones (máximo 12 caracteres)
        if (!limpio.matches("[A-Z0-9-]{1,20}") || !denunciaRepository.existsByCodigo(limpio)) {
            throw new ApiException(HttpStatus.NOT_FOUND, CODIGO_NO_ENCONTRADO);
        }
        return limpio;
    }

    /**
     * Paso 2: verifica el PIN del ciudadano dueño de la denuncia.
     *
     * Un PIN de 6 dígitos solo tiene un millón de combinaciones, así que se
     * limita la cantidad de intentos: al llegar al máximo, la consulta queda
     * bloqueada unos minutos.
     *
     * noRollbackFor: aunque el método termine con una excepción (PIN incorrecto),
     * el contador de intentos SÍ debe quedar guardado en la base de datos.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public Ciudadano verificarPin(String codigo, String pin) {
        Denuncia denuncia = denunciaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, CODIGO_NO_ENCONTRADO));
        Ciudadano ciudadano = denuncia.getCiudadano();
        LocalDateTime ahora = LocalDateTime.now();

        // 1. ¿La consulta está bloqueada por demasiados intentos?
        if (ciudadano.getPinBloqueadoHasta() != null && ciudadano.getPinBloqueadoHasta().isAfter(ahora)) {
            long minutos = Math.max(1, Duration.between(ahora, ciudadano.getPinBloqueadoHasta()).toMinutes() + 1);
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Se superó el número de intentos permitidos. Vuelve a intentarlo en " + minutos + " minuto(s).");
        }

        // 2. El ciudadano debe tener un PIN asignado
        if (ciudadano.getPinHash() == null) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Esta denuncia aún no tiene un PIN asignado. Comunícate con la dependencia policial donde la registraste.");
        }

        // 3. Se compara el PIN escrito con el hash BCrypt guardado
        boolean formatoValido = pin != null && pin.matches("\\d{6}");
        if (!formatoValido || !passwordEncoder.matches(pin, ciudadano.getPinHash())) {
            int intentos = ciudadano.getPinIntentos() + 1;
            if (intentos >= maxIntentos) {
                // Se alcanzó el máximo: se bloquea y el contador vuelve a cero
                ciudadano.setPinIntentos(0);
                ciudadano.setPinBloqueadoHasta(ahora.plusMinutes(minutosBloqueo));
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                        "El PIN no es correcto. Por seguridad, la consulta queda bloqueada durante " + minutosBloqueo + " minutos.");
            }
            ciudadano.setPinIntentos(intentos);
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "El PIN no es correcto. Te quedan " + (maxIntentos - intentos) + " intento(s).");
        }

        // 4. PIN correcto: se limpian el contador y el bloqueo
        ciudadano.setPinIntentos(0);
        ciudadano.setPinBloqueadoHasta(null);
        return ciudadano;
    }
}
