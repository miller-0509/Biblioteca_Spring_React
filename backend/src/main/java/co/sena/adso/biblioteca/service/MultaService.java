package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.entity.EstadoMulta;
import co.sena.adso.biblioteca.entity.EstadoPrestamo;
import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.entity.Multa;
import co.sena.adso.biblioteca.entity.Prestamo;
import co.sena.adso.biblioteca.entity.PrestamoLibro;
import co.sena.adso.biblioteca.entity.TipoRecurso;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.MultaRepository;
import co.sena.adso.biblioteca.repository.PrestamoLibroRepository;
import co.sena.adso.biblioteca.repository.PrestamoRepository;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de multas / suspensiones por retraso (equivalente a multas_service.py).
 * Estados: acumulando -> activa -> cumplida | condonada.
 */
@Service
public class MultaService {

    private static final Logger log = LoggerFactory.getLogger(MultaService.class);

    private final MultaRepository multaRepository;
    private final PrestamoRepository prestamoRepository;
    private final PrestamoLibroRepository prestamoLibroRepository;
    private final UsuarioRepository usuarioRepository;

    @Value("${multas.dias-gracia:1}")
    private int diasGracia;

    @Value("${multas.factor-libro:1}")
    private int factorLibro;

    @Value("${multas.factor-equipo:1}")
    private int factorEquipo;

    public MultaService(MultaRepository multaRepository, PrestamoRepository prestamoRepository,
                        PrestamoLibroRepository prestamoLibroRepository, UsuarioRepository usuarioRepository) {
        this.multaRepository = multaRepository;
        this.prestamoRepository = prestamoRepository;
        this.prestamoLibroRepository = prestamoLibroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Se invoca al registrar la devolución. Calcula el retraso final y activa la suspensión.
     */
    @Transactional
    public Multa activarSuspension(PrestamoLibro oPrestamoLibro, Prestamo oPrestamoEquipo) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaEsperada;
        TipoRecurso tipoRecurso;
        Long idUsuario;
        Long idPrestamoEquipo = null;
        Long idPrestamoLibro = null;
        int factor;

        if (oPrestamoLibro != null) {
            fechaEsperada = oPrestamoLibro.getFechaDevolucionEsperada();
            tipoRecurso = TipoRecurso.libro;
            idUsuario = oPrestamoLibro.getUsuario().getId();
            idPrestamoLibro = oPrestamoLibro.getId();
            factor = factorLibro;
        } else {
            fechaEsperada = oPrestamoEquipo.getFechaDevolucionEsperada();
            tipoRecurso = TipoRecurso.equipo;
            idUsuario = oPrestamoEquipo.getUsuario().getId();
            idPrestamoEquipo = oPrestamoEquipo.getId();
            factor = factorEquipo;
        }

        if (fechaEsperada == null) {
            return null;
        }
        long retrasoTotal = LocalDate.now().toEpochDay() - fechaEsperada.toLocalDate().toEpochDay();

        Multa multa = idPrestamoEquipo != null
            ? multaRepository.findByPrestamoEquipoId(idPrestamoEquipo).orElse(null)
            : multaRepository.findByPrestamoLibroId(idPrestamoLibro).orElse(null);

        if (retrasoTotal > diasGracia) {
            if (multa != null && (multa.getEstado() == EstadoMulta.condonada || multa.getEstado() == EstadoMulta.cumplida)) {
                return null;
            }
            if (multa == null) {
                multa = new Multa();
                multa.setTipoRecurso(tipoRecurso);
                multa.setPrestamoLibro(oPrestamoLibro);
                multa.setPrestamoEquipo(oPrestamoEquipo);
                multa.setUsuario(oPrestamoLibro != null ? oPrestamoLibro.getUsuario() : oPrestamoEquipo.getUsuario());
                multa.setFechaGeneracion(ahora);
                multa.setCreatedAt(ahora);
                multa.setUpdatedAt(ahora);
            }
            multa.setDiasRetraso((int) retrasoTotal);
            multa.setDiasSuspension((int) retrasoTotal * factor);
            multa.setEstado(EstadoMulta.activa);
            multa.setFechaInicioSuspension(ahora);
            multa.setFechaFinSuspension(ahora.plusDays((long) retrasoTotal * factor));
            multa.setUpdatedAt(ahora);
            return multaRepository.save(multa);
        } else if (multa != null && multa.getEstado() == EstadoMulta.acumulando) {
            multaRepository.delete(multa);
        }
        return null;
    }

    /**
     * Cron diario: crea multas 'acumulando' para préstamos vencidos, actualiza días
     * y marca como 'cumplida' las suspensiones terminadas.
     */
    public void actualizarMultasDiarias() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.minusDays(diasGracia);
        log.info("--- Iniciando actualización de multas ({}) ---", ahora);
        int nuevas = 0, actualizadas = 0, cumplidas = 0;

        List<Prestamo> equiposVencidos = prestamoRepository.findByEstadoAndFechaDevolucionEsperadaLessThan(
            EstadoPrestamo.pendiente, limite);
        List<Prestamo> equiposVencidosAceptados = prestamoRepository.findByEstadoAndFechaDevolucionEsperadaLessThan(
            EstadoPrestamo.aceptado, limite);

        for (Prestamo p : equiposVencidos) {
            nuevaOActualiza(p.getId(), p.getUsuario(), null, factorEquipo, true, ahora);
        }
        for (Prestamo p : equiposVencidosAceptados) {
            nuevaOActualiza(p.getId(), p.getUsuario(), null, factorEquipo, true, ahora);
        }

        List<PrestamoLibro> librosVencidos = prestamoLibroRepository.findByEstadoAndFechaDevolucionEsperadaLessThan(
            EstadoPrestamoLibro.pendiente, limite);
        List<PrestamoLibro> librosVencidosAceptados = prestamoLibroRepository.findByEstadoAndFechaDevolucionEsperadaLessThan(
            EstadoPrestamoLibro.aceptado, limite);

        for (PrestamoLibro l : librosVencidos) {
            nuevaOActualiza(null, l.getUsuario(), l, factorLibro, false, ahora);
        }
        for (PrestamoLibro l : librosVencidosAceptados) {
            nuevaOActualiza(null, l.getUsuario(), l, factorLibro, false, ahora);
        }

        List<Multa> activasTerminadas = multaRepository.findByEstadoAndFechaFinSuspensionLessThanEqual(
            EstadoMulta.activa, ahora);
        for (Multa m : activasTerminadas) {
            try {
                m.setEstado(EstadoMulta.cumplida);
                m.setUpdatedAt(ahora);
                multaRepository.save(m);
                cumplidas++;
            } catch (Exception e) {
                log.error("Error marcando multa {} como cumplida: {}", m.getId(), e.getMessage());
            }
        }
        log.info("Multas procesadas: Nuevas={}, Actualizadas={}, Cumplidas={}", nuevas, actualizadas, cumplidas);
    }

    private void nuevaOActualiza(Long idPrestamoEquipo, Usuario usuario, PrestamoLibro prestamoLibro,
                                 int factor, boolean esEquipo, LocalDateTime ahora) {
        try {
            Multa multa = idPrestamoEquipo != null
                ? multaRepository.findByPrestamoEquipoId(idPrestamoEquipo).orElse(null)
                : multaRepository.findByPrestamoLibroId(prestamoLibro.getId()).orElse(null);
            if (multa != null && multa.getEstado() != EstadoMulta.acumulando) {
                return;
            }
            LocalDateTime fechaEsperada = esEquipo
                ? prestamoRepository.findById(idPrestamoEquipo)
                    .map(Prestamo::getFechaDevolucionEsperada).orElse(null)
                : prestamoLibro.getFechaDevolucionEsperada();
            if (fechaEsperada == null) {
                return;
            }
            long retraso = LocalDate.now().toEpochDay() - fechaEsperada.toLocalDate().toEpochDay();
            if (multa == null) {
                multa = new Multa();
                multa.setTipoRecurso(esEquipo ? TipoRecurso.equipo : TipoRecurso.libro);
                multa.setPrestamoEquipo(esEquipo
                    ? prestamoRepository.findById(idPrestamoEquipo).orElse(null) : null);
                multa.setPrestamoLibro(prestamoLibro);
                multa.setUsuario(usuario);
                multa.setEstado(EstadoMulta.acumulando);
                multa.setFechaGeneracion(ahora);
                multa.setCreatedAt(ahora);
                multa.setUpdatedAt(ahora);
            }
            multa.setDiasRetraso((int) retraso);
            multa.setDiasSuspension((int) retraso * factor);
            multa.setUpdatedAt(ahora);
            multaRepository.save(multa);
        } catch (Exception e) {
            log.error("Error procesando multa (equipoId={}, libroId={}): {}", idPrestamoEquipo,
                prestamoLibro != null ? prestamoLibro.getId() : null, e.getMessage());
        }
    }

    @Transactional
    public Multa condonar(Long idMulta, String observacion, Long idAdministrador) {
        Multa multa = multaRepository.findById(idMulta)
            .orElseThrow(() -> new ResourceNotFoundException("Multa", idMulta));
        if (multa.getEstado() != EstadoMulta.acumulando && multa.getEstado() != EstadoMulta.activa) {
            throw new BusinessException("Esta sanción ya no está activa o ya fue condonada.");
        }
        if (observacion == null || observacion.isBlank()) {
            throw new BusinessException("Debes ingresar una observación para condonar la sanción.");
        }
        Usuario admin = usuarioRepository.findById(idAdministrador)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", idAdministrador));
        multa.setEstado(EstadoMulta.condonada);
        multa.setObservacion(observacion);
        multa.setAdministradorResolucion(admin);
        multa.setFechaFinSuspension(LocalDateTime.now());
        multa.setUpdatedAt(LocalDateTime.now());
        return multaRepository.save(multa);
    }

    /**
     * Lista multas aplicando el filtro por rol:
     * administrador: todas; bibliotecario: solo libros; almacenista: solo equipos;
     * aprendiz/instructor: solo sus propias multas.
     */
    @Transactional(readOnly = true)
    public java.util.List<co.sena.adso.biblioteca.dto.MultaResponseDTO> listar(
            co.sena.adso.biblioteca.entity.RolUsuario rol, Long idUsuario, EstadoMulta estado) {
        return multaRepository.findAllByOrderByFechaGeneracionDesc().stream()
            .filter(m -> switch (rol) {
                case administrador -> true;
                case bibliotecario -> m.getTipoRecurso() == TipoRecurso.libro;
                case almacenista -> m.getTipoRecurso() == TipoRecurso.equipo;
                default -> m.getUsuario() != null && m.getUsuario().getId().equals(idUsuario);
            })
            .filter(m -> estado == null || m.getEstado() == estado)
            .map(co.sena.adso.biblioteca.dto.MultaResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public co.sena.adso.biblioteca.dto.MultaResponseDTO findById(Long idMulta) {
        Multa multa = multaRepository.findById(idMulta)
            .orElseThrow(() -> new ResourceNotFoundException("Multa", idMulta));
        return co.sena.adso.biblioteca.dto.MultaResponseDTO.fromEntity(multa);
    }
}
