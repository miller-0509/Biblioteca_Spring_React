package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.PrestamoDevolucionDTO;
import co.sena.adso.biblioteca.dto.PrestamoRequestDTO;
import co.sena.adso.biblioteca.dto.PrestamoResponseDTO;
import co.sena.adso.biblioteca.dto.ProcesarRenovacionDTO;
import co.sena.adso.biblioteca.dto.RenovacionPrestamoRequestDTO;
import co.sena.adso.biblioteca.entity.Equipo;
import co.sena.adso.biblioteca.entity.EstadoEquipo;
import co.sena.adso.biblioteca.entity.EstadoPrestamo;
import co.sena.adso.biblioteca.entity.HistorialEstadoEquipo;
import co.sena.adso.biblioteca.entity.Multa;
import co.sena.adso.biblioteca.entity.Prestamo;
import co.sena.adso.biblioteca.entity.RenovacionEquipo;
import co.sena.adso.biblioteca.entity.RolUsuario;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.EquipoRepository;
import co.sena.adso.biblioteca.repository.HistorialEstadoEquipoRepository;
import co.sena.adso.biblioteca.repository.PrestamoRepository;
import co.sena.adso.biblioteca.repository.RenovacionEquipoRepository;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import co.sena.adso.biblioteca.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class PrestamoService {

    private static final Set<EstadoEquipo> ESTADOS_FINALES_VALIDOS = Set.of(
        EstadoEquipo.disponible, EstadoEquipo.mantenimiento, EstadoEquipo.dañado,
        EstadoEquipo.perdido, EstadoEquipo.eliminado, EstadoEquipo.fuera_de_servicio);
    private static final List<String> ESTADOS_FISICOS_VALIDOS =
        Arrays.asList("excelente", "bueno", "regular", "dañado", "incompleto");

    private final PrestamoRepository prestamoRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RenovacionEquipoRepository renovacionRepository;
    private final HistorialEstadoEquipoRepository historialRepository;
    private final PrestamoRules rules;
    private final MultaService multaService;
    private final EmailService emailService;

    public PrestamoService(PrestamoRepository prestamoRepository, EquipoRepository equipoRepository,
                           UsuarioRepository usuarioRepository, RenovacionEquipoRepository renovacionRepository,
                           HistorialEstadoEquipoRepository historialRepository, PrestamoRules rules,
                           MultaService multaService, EmailService emailService) {
        this.prestamoRepository = prestamoRepository;
        this.equipoRepository = equipoRepository;
        this.usuarioRepository = usuarioRepository;
        this.renovacionRepository = renovacionRepository;
        this.historialRepository = historialRepository;
        this.rules = rules;
        this.multaService = multaService;
        this.emailService = emailService;
    }

    private boolean gestionEquipos(RolUsuario rol) {
        return rol == RolUsuario.administrador || rol == RolUsuario.almacenista;
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> findAll() {
        return prestamoRepository.findAll().stream()
            .map(PrestamoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public PrestamoResponseDTO findById(Long id, CurrentUser actor) {
        Prestamo prestamo = prestamoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prestamo", id));
        verificarAcceso(prestamo, actor);
        return PrestamoResponseDTO.fromEntity(prestamo);
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> findByUsuarioId(Long idUsuario) {
        return prestamoRepository.findByUsuarioId(idUsuario).stream()
            .map(PrestamoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> findByEquipoId(Long idEquipo) {
        return prestamoRepository.findByEquipoId(idEquipo).stream()
            .map(PrestamoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional
    public PrestamoResponseDTO crear(PrestamoRequestDTO dto, CurrentUser actor) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.usuarioId()));
        Equipo equipo = equipoRepository.findById(dto.equipoId())
            .orElseThrow(() -> new ResourceNotFoundException("Equipo", dto.equipoId()));

        validarSolicitud(usuario, equipo);

        boolean staff = gestionEquipos(actor.rol());
        if (!staff && rules.prestamosActivosCount(usuario) >= rules.limitePrestamos(usuario.getRol())) {
            throw new BusinessException("Has alcanzado el límite de " + rules.limitePrestamos(usuario.getRol())
                + " préstamos activos permitidos para tu rol.");
        }
        // Protección contra race condition
        if (equipo.getEstado() != EstadoEquipo.disponible || !Boolean.TRUE.equals(equipo.getDisponiblePrestamo())) {
            throw new BusinessException("El equipo ya no está disponible. Otro usuario pudo haberlo solicitado.");
        }

        int dias = dto.diasPrestamo() != null ? dto.diasPrestamo()
            : (equipo.getTiempoMaxPrestamo() != null ? equipo.getTiempoMaxPrestamo() : 7);

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setEquipo(equipo);
        prestamo.setFechaSolicitud(LocalDateTime.now());
        prestamo.setObservaciones(dto.observaciones());
        prestamo.setRenovacionesAplicadas(0);

        if (staff) {
            prestamo.setEstado(EstadoPrestamo.aceptado);
            prestamo.setAdministrador(usuarioRepository.findById(actor.id()).orElse(null));
            prestamo.setFechaAprobacion(LocalDateTime.now());
            equipo.setEstado(EstadoEquipo.prestado);
            equipo.setDisponiblePrestamo(false);
            emailService.notificarPrestamo(usuario.getNombres(), usuario.getCorreo(), "aprobado",
                equipo.getNombre(), "Tu solicitud de préstamo fue aprobada por " + dias + " días.");
        } else {
            prestamo.setEstado(EstadoPrestamo.pendiente);
            emailService.notificarPrestamo(usuario.getNombres(), usuario.getCorreo(), "pendiente",
                equipo.getNombre(), "Tu solicitud de préstamo fue recibida y está pendiente de aprobación.");
        }
        prestamo.setFechaDevolucionEsperada(LocalDateTime.now().plusDays(dias));
        return PrestamoResponseDTO.fromEntity(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponseDTO aceptar(Long id, CurrentUser actor) {
        Prestamo prestamo = obtener(id);
        if (prestamo.getEstado() != EstadoPrestamo.pendiente) {
            throw new BusinessException("Este préstamo no está en estado pendiente.");
        }
        Equipo equipo = prestamo.getEquipo();
        if (equipo.getEstado() != EstadoEquipo.disponible) {
            throw new BusinessException("El equipo ya no está disponible. Puede haber sido prestado a otro usuario.");
        }
        prestamo.setEstado(EstadoPrestamo.aceptado);
        prestamo.setFechaAprobacion(LocalDateTime.now());
        prestamo.setAdministrador(usuarioRepository.findById(actor.id()).orElse(null));
        equipo.setEstado(EstadoEquipo.prestado);
        equipo.setDisponiblePrestamo(false);
        prestamoRepository.save(prestamo);
        emailService.notificarPrestamo(prestamo.getUsuario().getNombres(), prestamo.getUsuario().getCorreo(),
            "aprobado", equipo.getNombre(),
            "Tu solicitud de préstamo fue aprobada. Devolución esperada: " + prestamo.getFechaDevolucionEsperada());
        return PrestamoResponseDTO.fromEntity(prestamo);
    }

    @Transactional
    public PrestamoResponseDTO rechazar(Long id, String razon, CurrentUser actor) {
        Prestamo prestamo = obtener(id);
        if (prestamo.getEstado() != EstadoPrestamo.pendiente) {
            throw new BusinessException("Este préstamo no está en estado pendiente.");
        }
        if (razon != null && razon.length() > 255) {
            throw new BusinessException("La razón de rechazo no puede exceder 255 caracteres.");
        }
        prestamo.setEstado(EstadoPrestamo.rechazado);
        prestamo.setRazonRechazo(razon);
        prestamo.setAdministrador(usuarioRepository.findById(actor.id()).orElse(null));
        prestamoRepository.save(prestamo);
        emailService.notificarPrestamo(prestamo.getUsuario().getNombres(), prestamo.getUsuario().getCorreo(),
            "rechazado", prestamo.getEquipo().getNombre(), "Motivo: " + (razon == null ? "No especificado" : razon));
        return PrestamoResponseDTO.fromEntity(prestamo);
    }

    @Transactional
    public PrestamoResponseDTO devolver(Long id, PrestamoDevolucionDTO dto, CurrentUser actor) {
        Prestamo prestamo = obtener(id);
        if (prestamo.getEstado() != EstadoPrestamo.aceptado) {
            throw new BusinessException("Este préstamo no está en estado aceptado.");
        }
        if (!ESTADOS_FISICOS_VALIDOS.contains(dto.estadoFisico())) {
            throw new BusinessException("Estado físico inválido.");
        }
        EstadoEquipo estadoFinal;
        try {
            estadoFinal = EstadoEquipo.valueOf(dto.estadoFinal());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado final inválido.");
        }
        if (!ESTADOS_FINALES_VALIDOS.contains(estadoFinal)) {
            throw new BusinessException("Estado final inválido.");
        }

        EstadoEquipo estadoAnterior = prestamo.getEquipo().getEstado();
        prestamo.setEstado(EstadoPrestamo.devuelto);
        prestamo.setFechaDevolucionReal(LocalDateTime.now());
        prestamo.setObservacionDevolucion(dto.observacionDevolucion());
        prestamo.setEstadoFisicoDevolucion(dto.estadoFisico());

        Equipo equipo = prestamo.getEquipo();
        equipo.setEstado(estadoFinal);
        equipo.setDisponiblePrestamo(estadoFinal == EstadoEquipo.disponible);
        prestamoRepository.save(prestamo);

        if (estadoAnterior != estadoFinal) {
            HistorialEstadoEquipo historial = new HistorialEstadoEquipo(
                equipo, estadoAnterior.name(), estadoFinal.name(),
                "Cambio por devolución. Físico: " + dto.estadoFisico() + ". Obs: " + dto.observacionDevolucion(),
                usuarioRepository.findById(actor.id()).orElse(null), LocalDateTime.now());
            historialRepository.save(historial);
        }

        Multa multa = multaService.activarSuspension(null, prestamo);
        if (multa != null) {
            emailService.notificarMulta(prestamo.getUsuario().getNombres(), prestamo.getUsuario().getCorreo(),
                "activa", "Has sido suspendido por " + multa.getDiasSuspension()
                    + " días por retraso en la devolución. Hasta: " + multa.getFechaFinSuspension());
        }
        emailService.notificarPrestamo(prestamo.getUsuario().getNombres(), prestamo.getUsuario().getCorreo(),
            "devuelto", equipo.getNombre(), "Devolución registrada. El equipo pasó a estado " + estadoFinal.name() + ".");
        return PrestamoResponseDTO.fromEntity(prestamo);
    }

    @Transactional
    public PrestamoResponseDTO solicitarRenovacion(Long id, RenovacionPrestamoRequestDTO dto, CurrentUser actor) {
        Prestamo prestamo = obtener(id);
        if (!gestionEquipos(actor.rol()) && !prestamo.getUsuario().getId().equals(actor.id())) {
            throw new BusinessException("No tienes permiso para renovar este préstamo.");
        }
        Usuario usuario = prestamo.getUsuario();
        if (!gestionEquipos(actor.rol()) && rules.tieneMultasPendientes(usuario)) {
            throw new BusinessException("No puedes renovar préstamos porque tienes una sanción por retraso activa o en proceso.");
        }
        if (prestamo.getEstado() != EstadoPrestamo.aceptado) {
            throw new BusinessException("Solo puedes renovar préstamos activos (aceptados).");
        }
        if (prestamo.getFechaDevolucionEsperada() == null) {
            throw new BusinessException("Este préstamo no tiene fecha de devolución esperada; no se puede renovar.");
        }
        if (prestamo.getFechaDevolucionEsperada().isBefore(LocalDateTime.now())) {
            throw new BusinessException("No puedes renovar un préstamo vencido.");
        }
        if ("pendiente".equals(prestamo.getEstadoRenovacion())) {
            throw new BusinessException("Ya tienes una solicitud de renovación pendiente para este préstamo.");
        }
        if (rules.limiteRenovacionesAlcanzado(usuario, prestamo.getRenovacionesAplicadas())) {
            throw new BusinessException("Has alcanzado el límite máximo de renovaciones permitidas para tu rol.");
        }

        int diasExtra = prestamo.getEquipo().getTiempoMaxPrestamo() != null
            ? prestamo.getEquipo().getTiempoMaxPrestamo() : 3;
        RenovacionEquipo renovacion = new RenovacionEquipo();
        renovacion.setPrestamo(prestamo);
        renovacion.setUsuario(usuario);
        renovacion.setFechaSolicitud(LocalDateTime.now());
        renovacion.setFechaEsperadaOriginal(prestamo.getFechaDevolucionEsperada());
        renovacion.setFechaEsperadaNueva(prestamo.getFechaDevolucionEsperada().plusDays(diasExtra));
        renovacion.setEstado("pendiente");
        renovacion.setMotivoSolicitud(dto.motivoRenovacion());
        renovacionRepository.save(renovacion);

        prestamo.setEstadoRenovacion("pendiente");
        prestamoRepository.save(prestamo);
        emailService.notificarPrestamo(usuario.getNombres(), usuario.getCorreo(), "renovacion_solicitada",
            prestamo.getEquipo().getNombre(), "Tu solicitud de renovación está pendiente de aprobación.");
        return PrestamoResponseDTO.fromEntity(prestamo);
    }

    @Transactional
    public PrestamoResponseDTO procesarRenovacion(Long id, ProcesarRenovacionDTO dto, CurrentUser actor) {
        Prestamo prestamo = obtener(id);
        if (!"pendiente".equals(prestamo.getEstadoRenovacion())) {
            throw new BusinessException("No hay ninguna solicitud de renovación pendiente para este préstamo.");
        }
        RenovacionEquipo renovacion = renovacionRepository
            .findTopByPrestamoIdAndEstadoOrderByFechaSolicitudDesc(id, "pendiente")
            .orElseThrow(() -> new BusinessException("No se encontró la solicitud de renovación."));

        renovacion.setAdministrador(usuarioRepository.findById(actor.id()).orElse(null));
        renovacion.setFechaRespuesta(LocalDateTime.now());

        String accion = dto.accion() == null ? "" : dto.accion().toLowerCase();
        if ("aprobar".equals(accion)) {
            renovacion.setEstado("aprobada");
            prestamo.setEstadoRenovacion("aprobada");
            prestamo.setFechaDevolucionEsperada(renovacion.getFechaEsperadaNueva());
            prestamo.setRenovacionesAplicadas(prestamo.getRenovacionesAplicadas() + 1);
            prestamo.setNotificacionVencimientoEnviada(false);
            prestamo.setNotificacionVencidoEnviada(false);
            emailService.notificarPrestamo(prestamo.getUsuario().getNombres(), prestamo.getUsuario().getCorreo(),
                "renovado", prestamo.getEquipo().getNombre(),
                "Renovación aprobada. Nueva fecha de devolución: " + prestamo.getFechaDevolucionEsperada());
        } else if ("rechazar".equals(accion)) {
            if (dto.motivoRechazo() == null || dto.motivoRechazo().isBlank()) {
                throw new BusinessException("Debes proporcionar un motivo para rechazar la renovación.");
            }
            renovacion.setEstado("rechazada");
            renovacion.setMotivoRechazo(dto.motivoRechazo());
            prestamo.setEstadoRenovacion("rechazada");
            emailService.notificarPrestamo(prestamo.getUsuario().getNombres(), prestamo.getUsuario().getCorreo(),
                "renovacion_rechazada", prestamo.getEquipo().getNombre(), "Motivo: " + dto.motivoRechazo());
        } else {
            throw new BusinessException("Acción inválida. Usa 'aprobar' o 'rechazar'.");
        }
        renovacionRepository.save(renovacion);
        prestamoRepository.save(prestamo);
        return PrestamoResponseDTO.fromEntity(prestamo);
    }

    private void validarSolicitud(Usuario usuario, Equipo equipo) {
        if (usuario.getRol() == RolUsuario.administrador) {
            throw new BusinessException("Los administradores no pueden solicitar préstamos.");
        }
        if (rules.tieneMultasPendientes(usuario)) {
            throw new BusinessException("No puedes solicitar préstamos porque tienes una sanción por retraso activa o en proceso.");
        }
        if (!Boolean.TRUE.equals(equipo.getDisponiblePrestamo())) {
            throw new BusinessException("Este equipo no está disponible para préstamo.");
        }
        if (equipo.getEstado() != EstadoEquipo.disponible) {
            throw new BusinessException("El equipo está en estado \"" + equipo.getEstado() + "\" y no puede prestarse.");
        }
        boolean tieneActivo = prestamoRepository.existsByEquipoIdAndEstadoIn(
            equipo.getId(), List.of(EstadoPrestamo.pendiente, EstadoPrestamo.aceptado));
        if (tieneActivo) {
            throw new BusinessException("Este equipo ya tiene un préstamo en proceso.");
        }
    }

    private void verificarAcceso(Prestamo prestamo, CurrentUser actor) {
        if (!gestionEquipos(actor.rol()) && !prestamo.getUsuario().getId().equals(actor.id())) {
            throw new BusinessException("No tienes permiso para ver este préstamo.");
        }
    }

    private Prestamo obtener(Long id) {
        return prestamoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prestamo", id));
    }
}
