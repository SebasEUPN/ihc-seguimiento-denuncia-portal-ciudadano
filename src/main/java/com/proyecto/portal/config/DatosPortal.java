package com.proyecto.portal.config;

import com.proyecto.portal.model.Ciudadano;
import com.proyecto.portal.model.Conversacion;
import com.proyecto.portal.model.Denuncia;
import com.proyecto.portal.model.Usuario;
import com.proyecto.portal.repository.CiudadanoRepository;
import com.proyecto.portal.repository.ConversacionRepository;
import com.proyecto.portal.repository.DenunciaRepository;
import com.proyecto.portal.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Prepara, cada vez que arranca el portal, los datos de demostración que
 * falten. Nunca modifica lo que ya tiene valor, así que puede ejecutarse
 * las veces que sea:
 *  1. PIN de demostración para los ciudadanos que aún no tienen PIN.
 *  2. Funcionario responsable para las denuncias que aún no lo tienen.
 *  3. (Opcional) dos denuncias más para el ciudadano de D-000584.
 *
 * Los datos base (usuarios, ciudadanos, denuncias) los carga el primer
 * prototipo; por eso debe haberse ejecutado al menos una vez antes.
 */
@Component
public class DatosPortal implements CommandLineRunner {

    private final CiudadanoRepository ciudadanoRepository;
    private final DenunciaRepository denunciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConversacionRepository conversacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final String pinDemo;
    private final boolean denunciasExtra;

    public DatosPortal(CiudadanoRepository ciudadanoRepository, DenunciaRepository denunciaRepository,
                       UsuarioRepository usuarioRepository, ConversacionRepository conversacionRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.demo.pin:123456}") String pinDemo,
                       @Value("${app.demo.denuncias-extra:true}") boolean denunciasExtra) {
        this.ciudadanoRepository = ciudadanoRepository;
        this.denunciaRepository = denunciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.conversacionRepository = conversacionRepository;
        this.passwordEncoder = passwordEncoder;
        this.pinDemo = pinDemo;
        this.denunciasExtra = denunciasExtra;
    }

    /** Spring ejecuta este método automáticamente al terminar de arrancar. */
    @Override
    @Transactional
    public void run(String... args) {
        if (denunciasExtra) {
            agregarDenunciasDeEjemplo();
        }
        asignarPin();
        asignarResponsables();
    }

    /**
     * Versión en la nube: los dos prototipos arrancan por separado en Render y
     * el primero puede terminar de cargar sus datos (por ejemplo las 500
     * denuncias del Excel) después que el portal. Por eso cada 5 minutos se
     * vuelve a completar lo que falte (PIN y responsable); si no falta nada, no cambia nada.
     */
    @Scheduled(initialDelay = 120_000, fixedDelay = 300_000)
    @Transactional
    public void completarPendientes() {
        if (denunciasExtra) {
            agregarDenunciasDeEjemplo();
        }
        asignarPin();
        asignarResponsables();
    }

    /** 1. Los ciudadanos sin PIN reciben el PIN de demostración, cifrado con BCrypt. */
    private void asignarPin() {
        List<Ciudadano> sinPin = ciudadanoRepository.findByPinHashIsNull();
        if (sinPin.isEmpty()) {
            return;
        }
        String hash = passwordEncoder.encode(pinDemo);
        sinPin.forEach(c -> c.setPinHash(hash)); // Hibernate guarda los cambios al terminar la transacción
    }

    /**
     * 2. Cada denuncia sin responsable recibe uno:
     *    - si ya existe un chat del ciudadano sobre esa denuncia, el funcionario de ese chat;
     *    - si no, se reparten por turnos entre los policías habilitados.
     */
    private void asignarResponsables() {
        List<Usuario> policias = usuarioRepository.findByRolAndHabilitadoTrueOrderByIdAsc("POLICIA");
        if (policias.isEmpty()) {
            return; // todavía no hay funcionarios: se asignarán en un próximo arranque
        }
        int turno = 0;
        for (Denuncia d : denunciaRepository.findByResponsableIsNullOrderByIdAsc()) {
            Usuario delChat = conversacionRepository
                    .findFirstByTipoAndReferenciaAndIdCiudadanoOrderByIdAsc(Conversacion.TIPO_CIUDADANO, d.getCodigo(), d.getCiudadano().getId())
                    .flatMap(c -> usuarioRepository.findById(c.getIdUsuarioA()))
                    .orElse(null);
            d.setResponsable(delChat != null ? delChat : policias.get(turno++ % policias.size()));
        }
    }

    /**
     * 3. Agrega dos denuncias al ciudadano de D-000584 para que la pantalla
     *    "Mis denuncias registradas" muestre un historial de varias denuncias.
     */
    private void agregarDenunciasDeEjemplo() {
        Denuncia base = denunciaRepository.findByCodigo("D-000584").orElse(null);
        if (base == null) {
            return; // el primer prototipo aún no cargó sus datos
        }
        crearSiNoExiste("D-000604", base.getCiudadano(), "Hurto", "Por resolver", "Diligencias",
                LocalDateTime.of(2026, 10, 18, 11, 10), LocalDateTime.of(2026, 10, 20, 14, 20),
                "Jr. Los Álamos 345, Lima", "CF-2026-00145", "Segunda Fiscalía Provincial Penal", "Dr. Miguel Salazar",
                "El ciudadano informó la sustracción de sus pertenencias personales mientras se encontraba en un establecimiento comercial.");
        crearSiNoExiste("D-000605", base.getCiudadano(), "Daños", "Resuelto", "Archivado",
                LocalDateTime.of(2026, 9, 24, 16, 35), LocalDateTime.of(2026, 10, 2, 10, 15),
                "Av. Universitaria 860, Lima", "CF-2026-00146", "Tercera Fiscalía Provincial Penal", "Dra. Patricia Rojas",
                "Se registraron daños materiales en el vehículo del ciudadano mientras permanecía estacionado en la vía pública.");
    }

    private void crearSiNoExiste(String codigo, Ciudadano ciudadano, String delito, String estadoPolicial,
                                 String estadoFiscal, LocalDateTime registro, LocalDateTime actualizacion,
                                 String lugar, String carpeta, String despacho, String fiscal, String descripcion) {
        if (denunciaRepository.existsByCodigo(codigo)) {
            return;
        }
        Denuncia d = new Denuncia();
        d.setCodigo(codigo);
        d.setCiudadano(ciudadano);
        d.setTipoDelito(delito);
        d.setEstadoPolicial(estadoPolicial);
        d.setEstadoFiscal(estadoFiscal);
        d.setFechaRegistro(registro);
        d.setUltimaActualizacion(actualizacion);
        d.setLugarHechos(lugar);
        d.setCarpetaFiscal(carpeta);
        d.setDespachoFiscal(despacho);
        d.setFiscalResponsable(fiscal);
        d.setDescripcion(descripcion);
        denunciaRepository.save(d);
    }
}
