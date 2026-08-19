package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.PrestamoLibroRequestDTO;
import co.sena.adso.biblioteca.dto.PrestamoLibroResponseDTO;
import co.sena.adso.biblioteca.dto.ProcesarRenovacionDTO;
import co.sena.adso.biblioteca.dto.RenovacionPrestamoRequestDTO;
import co.sena.adso.biblioteca.entity.EstadoLibro;
import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.entity.HistorialEstadoLibro;
import co.sena.adso.biblioteca.entity.Libro;
import co.sena.adso.biblioteca.entity.Multa;
import co.sena.adso.biblioteca.entity.PrestamoLibro;
import co.sena.adso.biblioteca.entity.RenovacionLibro;
import co.sena.adso.biblioteca.entity.RolUsuario;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.HistorialEstadoLibroRepository;
import co.sena.adso.biblioteca.repository.LibroRepository;
import co.sena.adso.biblioteca.repository.PrestamoLibroRepository;
import co.sena.adso.biblioteca.repository.RenovacionLibroRepository;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import co.sena.adso.biblioteca.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class PrestamoLibroService {

    private static final Set<EstadoLibro> ESTADOS_FINALES_VALIDOS = Set.of(
        EstadoLibro.disponible, EstadoLibro.mantenimiento, EstadoLibro.dañado,
        EstadoLibro.perdido, EstadoLibro.eliminado);
    private static final List<String> ESTADOS_FISICOS_VALIDOS =
        Arrays.asList("excelente", "bueno", "regular", "dañado", "incompleto");

    private final PrestamoLibroRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final RenovacionLibroRepository renovacionRepository;
    private final HistorialEstadoLibroRepository historialRepository;
    private final PrestamoRules rules;
    private final MultaService multaService;
    private final EmailService emailService;

    public PrestamoLibroService(
            PrestamoLibroRepository prestamoRepository,
            UsuarioRepository usuarioRepository,
            LibroRepository libroRepository,
            RenovacionLibroRepository renovacionRepository,
            HistorialEstadoLibroRepository historialRepository,
            PrestamoRules rules,
            MultaService multaService,
            EmailService emailService) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.renovacionRepository = renovacionRepository;
        this.historialRepository = historialRepository;
        this.rules = rules;
        this.multaService = multaService;
        this.emailService = emailService;
    }

    private boolean gestionLibros(RolUsuario rol) {
        return rol == RolUsuario.administrador || rol == RolUsuario.bibliotecario;
    }

    @Transactional(readOnly = true)
    public List<PrestamoLibroResponseDTO> findAll() {
        return prestamoRepository.findAll().stream()
            .map(PrestamoLibroResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public PrestamoLibroResponseDTO findById(Long id) {
        PrestamoLibro prestamo = prestamoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PrestamoLibro", id));
        return PrestamoLibroResponseDTO.fromEntity(prestamo);
    }

    @Transactional(readOnly = true)
    public List<PrestamoLibroResponseDTO> findByUsuarioId(Long idUsuario) {
        return prestamoRepository.findByUsuarioId(idUsuario).stream()
            .map(PrestamoLibroResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PrestamoLibroResponseDTO> findByLibroId(Long idLibro) {
        return prestamoRepository.findByLibroId(idLibro).stream()
            .map(PrestamoLibroResponseDTO::fromEntity)
            .toList();
    }

    // ------------------------------------------------------------------
    // CRUD genérico (mantenido para compatibilidad de endpoints/tests)
    // ------------------------------------------------------------------

    @Transactional
    public PrestamoLibroResponseDTO create(PrestamoLibroRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.usuarioId()));
        Libro libro = libroRepository.findById(dto.libroId())
            .orElseThrow(() -> new ResourceNotFoundException("Libro", dto.libroId()));
        Usuario administrador = dto.administradorId() != null
            ? usuarioRepository.findById(dto.administradorId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.administradorId()))
            : null;
        LocalDateTime fechaEsperada = dto.fechaDevolucionEsperada() != null
            ? dto.fechaDevolucionEsperada()
            : LocalDateTime.now().plusDays(libro.getTiempoMaxPrestamo() != null ? libro.getTiempoMaxPrestamo() : 15);
        PrestamoLibro prestamo = new PrestamoLibro(
            usuario,
            libro,
            administrador,
            dto.fechaSolicitud() != null ? dto.fechaSolicitud() : LocalDateTime.now(),
            dto.fechaAprobacion(),
            fechaEsperada,
            dto.fechaDevolucionReal(),
            dto.estado() != null ? dto.estado() : EstadoPrestamoLibro.pendiente,
            dto.razonRechazo(),
            dto.observaciones(),
            dto.observacionDevolucion(),
            dto.estadoFisicoDevolucion(),
            dto.renovacionesAplicadas() != null ? dto.renovacionesAplicadas() : 0,
            dto.estadoRenovacion()
        );
        return PrestamoLibroResponseDTO.fromEntity(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoLibroResponseDTO update(Long id, PrestamoLibroRequestDTO dto) {
        PrestamoLibro prestamo = prestamoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PrestamoLibro", id));
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.usuarioId()));
        Libro libro = libroRepository.findById(dto.libroId())
            .orElseThrow(() -> new ResourceNotFoundException("Libro", dto.libroId()));
        Usuario administrador = dto.administradorId() != null
            ? usuarioRepository.findById(dto.administradorId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.administradorId()))
            : null;
        prestamo.setUsuario(usuario);
        prestamo.setLibro(libro);
        prestamo.setAdministrador(administrador);
        prestamo.setFechaSolicitud(dto.fechaSolicitud());
        prestamo.setFechaAprobacion(dto.fechaAprobacion());
        prestamo.setFechaDevolucionEsperada(dto.fechaDevolucionEsperada());
        prestamo.setFechaDevolucionReal(dto.fechaDevolucionReal());
        prestamo.setEstado(dto.estado());
        prestamo.setRazonRechazo(dto.razonRechazo());
        prestamo.setObservaciones(dto.observaciones());
        prestamo.setObservacionDevolucion(dto.observacionDevolucion());
        prestamo.setEstadoFisicoDevolucion(dto.estadoFisicoDevolucion());
        prestamo.setRenovacionesAplicadas(dto.renovacionesAplicadas());
        prestamo.setEstadoRenovacion(dto.estadoRenovacion());
        return PrestamoLibroResponseDTO.fromEntity(prestamoRepository.save(prestamo));
    }

    @Transactional
    public void delete(Long id) {
        if (!prestamoRepository.existsById(id)) {
            throw new ResourceNotFoundException("PrestamoLibro", id);
        }
        renovacionRepository.deleteAll(renovacionRepository.findByPrestamoLibroId(id));
        prestamoRepository.deleteById(id);
    }

    @Transactional
    public PrestamoLibroResponseDTO cambiarEstado(Long id, EstadoPrestamoLibro estado) {
        PrestamoLibro prestamo = prestamoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PrestamoLibro", id));
        prestamo.setEstado(estado);
        if (estado == EstadoPrestamoLibro.aceptado && prestamo.getFechaAprobacion() == null) {
            prestamo.setFechaAprobacion(LocalDateTime.now());
        }
        if (estado == EstadoPrestamoLibro.devuelto && prestamo.getFechaDevolucionReal() == null) {
            prestamo.setFechaDevolucionReal(LocalDateTime.now());
        }
        return PrestamoLibroResponseDTO.fromEntity(prestamoRepository.save(prestamo));
    }

    // ------------------------------------------------------------------
    // Flujos de negocio (equivalentes a los de equipos)
    // ------------------------------------------------------------------

    @Transactional
    public PrestamoLibroResponseDTO crear(PrestamoLibroRequestDTO dto, CurrentUser actor) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.usuarioId()));
        Libro libro = libroRepository.findById(dto.libroId())
            .orElseThrow(() -> new ResourceNotFoundException("Libro", dto.libroId()));

        validarSolicitud(usuario, libro);

        boolean staff = gestionLibros(actor.rol());
        if (!staff && rules.prestamosActivosCount(usuario) >= rules.limitePrestamos(usuario.getRol())) {
            throw new BusinessException("Has alcanzado el límite de " + rules.limitePrestamos(usuario.getRol())
                + " préstamos activos permitidos para tu rol.");
        }
        if (libro.getEstado() != EstadoLibro.disponible || !Boolean.TRUE.equals(libro.getDisponiblePrestamo())) {
            throw new BusinessException("El libro ya no está disponible. Otro usuario pudo haberlo solicitado.");
        }

        int dias = dto.fechaDevolucionEsperada() != null
            ? (int) java.time.Duration.between(LocalDateTime.now(), dto.fechaDevolucionEsperada()).toDays()
            : (libro.getTiempoMaxPrestamo() != null ? libro.getTiempoMaxPrestamo() : 15);

        PrestamoLibro prestamo = new PrestamoLibro();
        prestamo.setUsuario(usuario);
        prestamo.setLibro(libro);
        prestamo.setFechaSolicitud(LocalDateTime.now());
        prestamo.setObservaciones(dto.observaciones());
        prestamo.setRenovacionesAplicadas(0);

        if (staff) {
            prestamo.setEstado(EstadoPrestamoLibro.aceptado);
            prestamo.setAdministrador(usuarioRepository.findById(actor.id()).orElse(null));
            prestamo.setFechaAprobacion(LocalDateTime.now());
            libro.setEstado(EstadoLibro.prestado);
            libro.setDisponiblePrestamo(false);
            emailService.notificarPrestamo(usuario.getNombres(), usuario.getCorreo(), "aprobado",
                libro.getTitulo(), "Tu solicitud de préstamo fue aprobada por " + dias + " días.");
        } else {
            prestamo.setEstado(EstadoPrestamoLibro.pendiente);
            emailService.notificarPrestamo(usuario.getNombres(), usuario.getCorreo(), "pendiente",
                libro.getTitulo(), "Tu solicitud de préstamo fue recibida y está pendiente de aprobación.");
        }
        prestamo.setFechaDevolucionEsperada(LocalDateTime.now().plusDays(dias));
        return PrestamoLibroResponseDTO.fromEntity(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoLibroResponseDTO aceptar(Long id, CurrentUser actor) {
        PrestamoLibro prestamo = obtener(id);
        if (prestamo.getEstado() != EstadoPrestamoLibro.pendiente) {
            throw new BusinessException("Este préstamo no está en estado pendiente.");
        }
        Libro libro = prestamo.getLibro();
        if (libro.getEstado() != EstadoLibro.disponible) {
            throw new BusinessException("El libro ya no está disponible. Puede haber sido prestado a otro usuario.");
        }
        prestamo.setEstado(EstadoPrestamoLibro.aceptado);
        prestamo.setFechaAprobacion(LocalDateTime.now());
        if (prestamo.getFechaDevolucionEsperada() == null) {
            prestamo.setFechaDevolucionEsperada(LocalDateTime.now()
                .plusDays(libro.getTiempoMaxPrestamo() != null ? libro.getTiempoMaxPrestamo() : 15));
        }
        prestamo.setAdministrador(usuarioRepository.findById(actor.id()).orElse(null));
        libro.setEstado(EstadoLibro.prestado);
        libro.setDisponiblePrestamo(false);
        prestamoRepository.save(prestamo);
        emailService.notificarPrestamo(prestamo.getUsuario().getNombres(), prestamo.getUsuario().getCorreo(),
            "aprobado", libro.getTitulo(),
            "Tu solicitud de préstamo fue aprobada. Devolución esperada: " + prestamo.getFechaDevolucionEsperada());
        return PrestamoLibroResponseDTO.fromEntity(prestamo);
    }

    @Transactional
    public PrestamoLibroResponseDTO rechazar(Long id, String razon, CurrentUser actor) {
        PrestamoLibro prestamo = obtener(id);
        if (prestamo.getEstado() != EstadoPrestamoLibro.pendiente) {
            throw new BusinessException("Este préstamo no está en estado pendiente.");
        }
        if (razon != null && razon.length() > 255) {
            throw new BusinessException("La razón de rechazo no puede exceder 255 caracteres.");
        }
        prestamo.setEstado(EstadoPrestamoLibro.rechazado);
        prestamo.setRazonRechazo(razon);
        prestamo.setAdministrador(usuarioRepository.findById(actor.id()).orElse(null));
        prestamoRepository.save(prestamo);
        emailService.notificarPrestamo(prestamo.getUsuario().getNombres(), prestamo.getUsuario().getCorreo(),
            "rechazado", prestamo.getLibro().getTitulo(), "Motivo: " + (razon == null ? "No especificado" : razon));
        return PrestamoLibroResponseDTO.fromEntity(prestamo);
    }

    @Transactional
    public PrestamoLibroResponseDTO devolver(Long id, co.sena.adso.biblioteca.dto.PrestamoDevolucionDTO dto, CurrentUser actor) {
        PrestamoLibro prestamo = obtener(id);
        if (prestamo.getEstado() != EstadoPrestamoLibro.aceptado) {
            throw new BusinessException("Este préstamo no está en estado aceptado.");
        }
        if (!ESTADOS_FISICOS_VALIDOS.contains(dto.estadoFisico())) {
            throw new BusinessException("Estado físico inválido.");
        }
        EstadoLibro estadoFinal;
        try {
            estadoFinal = EstadoLibro.valueOf(dto.estadoFinal());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado final inválido.");
        }
        if (!ESTADOS_FINALES_VALIDOS.contains(estadoFinal)) {
            throw new BusinessException("Estado final inválido.");
        }

        EstadoLibro estadoAnterior = prestamo.getLibro().getEstado();
        prestamo.setEstado(EstadoPrestamoLibro.devuelto);
        prestamo.setFechaDevolucionReal(LocalDateTime.now());
        prestamo.setObservacionDevolucion(dto.observacionDevolucion());
        prestamo.setEstadoFisicoDevolucion(dto.estadoFisico());

        Libro libro = prestamo.getLibro();
        libro.setEstado(estadoFinal);
        libro.setDisponiblePrestamo(estadoFinal == EstadoLibro.disponible);
        prestamoRepository.save(prestamo);

        if (estadoAnterior != estadoFinal) {
            HistorialEstadoLibro historial = new HistorialEstadoLibro(
                libro, estadoAnterior.name(), estadoFinal.name(),
                "Cambio por devolución. Físico: " + dto.estadoFisico() + ". Obs: " + dto.observacionDevolucion(),
                usuarioRepository.findById(actor.id()).orElse(null), LocalDateTime.now());
            historialRepository.save(historial);
        }

        Multa multa = multaService.activarSuspension(prestamo, null);
        if (multa != null) {
            emailService.notificarMulta(prestamo.getUsuario().getNombres(), prestamo.getUsuario().getCorreo(),
                "activa", "Has sido suspendido por " + multa.getDiasSuspension()
                    + " días por retraso en la devolución. Hasta: " + multa.getFechaFinSuspension());
        }
        emailService.notificarPrestamo(prestamo.getUsuario().getNombres(), prestamo.getUsuario().getCorreo(),
            "devuelto", libro.getTitulo(), "Devolución registrada. El libro pasó a estado " + estadoFinal.name() + ".");
        return PrestamoLibroResponseDTO.fromEntity(prestamo);
    }

    @Transactional
    public PrestamoLibroResponseDTO solicitarRenovacion(Long id, RenovacionPrestamoRequestDTO dto, CurrentUser actor) {
        PrestamoLibro prestamo = obtener(id);
        if (!gestionLibros(actor.rol()) && !prestamo.getUsuario().getId().equals(actor.id())) {
            throw new BusinessException("No tienes permiso para renovar este préstamo.");
        }
        Usuario usuario = prestamo.getUsuario();
        if (!gestionLibros(actor.rol()) && rules.tieneMultasPendientes(usuario)) {
            throw new BusinessException("No puedes renovar préstamos porque tienes una sanción por retraso activa o en proceso.");
        }
        if (prestamo.getEstado() != EstadoPrestamoLibro.aceptado) {
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

        int diasExtra = prestamo.getLibro().getTiempoMaxPrestamo() != null
            ? prestamo.getLibro().getTiempoMaxPrestamo() : 15;
        RenovacionLibro renovacion = new RenovacionLibro();
        renovacion.setPrestamoLibro(prestamo);
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
            prestamo.getLibro().getTitulo(), "Tu solicitud de renovación está pendiente de aprobación.");
        return PrestamoLibroResponseDTO.fromEntity(prestamo);
    }

    @Transactional
    public PrestamoLibroResponseDTO procesarRenovacion(Long id, ProcesarRenovacionDTO dto, CurrentUser actor) {
        PrestamoLibro prestamo = obtener(id);
        if (!"pendiente".equals(prestamo.getEstadoRenovacion())) {
            throw new BusinessException("No hay ninguna solicitud de renovación pendiente para este préstamo.");
        }
        RenovacionLibro renovacion = renovacionRepository
            .findTopByPrestamoLibroIdAndEstadoOrderByFechaSolicitudDesc(id, "pendiente")
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
                "renovado", prestamo.getLibro().getTitulo(),
                "Renovación aprobada. Nueva fecha de devolución: " + prestamo.getFechaDevolucionEsperada());
        } else if ("rechazar".equals(accion)) {
            if (dto.motivoRechazo() == null || dto.motivoRechazo().isBlank()) {
                throw new BusinessException("Debes proporcionar un motivo para rechazar la renovación.");
            }
            renovacion.setEstado("rechazada");
            renovacion.setMotivoRechazo(dto.motivoRechazo());
            prestamo.setEstadoRenovacion("rechazada");
            emailService.notificarPrestamo(prestamo.getUsuario().getNombres(), prestamo.getUsuario().getCorreo(),
                "renovacion_rechazada", prestamo.getLibro().getTitulo(), "Motivo: " + dto.motivoRechazo());
        } else {
            throw new BusinessException("Acción inválida. Usa 'aprobar' o 'rechazar'.");
        }
        renovacionRepository.save(renovacion);
        prestamoRepository.save(prestamo);
        return PrestamoLibroResponseDTO.fromEntity(prestamo);
    }

    private void validarSolicitud(Usuario usuario, Libro libro) {
        if (usuario.getRol() == RolUsuario.administrador) {
            throw new BusinessException("Los administradores no pueden solicitar préstamos.");
        }
        if (rules.tieneMultasPendientes(usuario)) {
            throw new BusinessException("No puedes solicitar préstamos porque tienes una sanción por retraso activa o en proceso.");
        }
        if (!Boolean.TRUE.equals(libro.getDisponiblePrestamo())) {
            throw new BusinessException("Este libro no está disponible para préstamo.");
        }
        if (libro.getEstado() != EstadoLibro.disponible) {
            throw new BusinessException("El libro está en estado \"" + libro.getEstado() + "\" y no puede prestarse.");
        }
        boolean tieneActivo = prestamoRepository.existsByLibroIdAndEstadoIn(
            libro.getId(), List.of(EstadoPrestamoLibro.pendiente, EstadoPrestamoLibro.aceptado));
        if (tieneActivo) {
            throw new BusinessException("Este libro ya tiene un préstamo en proceso.");
        }
    }

    private PrestamoLibro obtener(Long id) {
        return prestamoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PrestamoLibro", id));
    }
}
