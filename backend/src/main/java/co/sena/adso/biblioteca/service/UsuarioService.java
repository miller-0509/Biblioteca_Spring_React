package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.HistorialPrestamoItemDTO;
import co.sena.adso.biblioteca.dto.HistorialUsuarioResponseDTO;
import co.sena.adso.biblioteca.dto.UsuarioRequestDTO;
import co.sena.adso.biblioteca.dto.UsuarioResponseDTO;
import co.sena.adso.biblioteca.dto.UsuarioUpdateDTO;
import co.sena.adso.biblioteca.entity.EstadoPrestamo;
import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.entity.EstadoUsuario;
import co.sena.adso.biblioteca.entity.Prestamo;
import co.sena.adso.biblioteca.entity.PrestamoLibro;
import co.sena.adso.biblioteca.entity.RolUsuario;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.HistorialEstadoEquipoRepository;
import co.sena.adso.biblioteca.repository.HistorialEstadoLibroRepository;
import co.sena.adso.biblioteca.repository.MultaRepository;
import co.sena.adso.biblioteca.repository.PrestamoLibroRepository;
import co.sena.adso.biblioteca.repository.PrestamoRepository;
import co.sena.adso.biblioteca.repository.RenovacionEquipoRepository;
import co.sena.adso.biblioteca.repository.RenovacionLibroRepository;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import co.sena.adso.biblioteca.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final PrestamoLibroRepository prestamoLibroRepository;
    private final RenovacionEquipoRepository renovacionEquipoRepository;
    private final RenovacionLibroRepository renovacionLibroRepository;
    private final MultaRepository multaRepository;
    private final HistorialEstadoLibroRepository historialLibroRepository;
    private final HistorialEstadoEquipoRepository historialEquipoRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PrestamoRepository prestamoRepository,
                          PrestamoLibroRepository prestamoLibroRepository,
                          RenovacionEquipoRepository renovacionEquipoRepository,
                          RenovacionLibroRepository renovacionLibroRepository,
                          MultaRepository multaRepository,
                          HistorialEstadoLibroRepository historialLibroRepository,
                          HistorialEstadoEquipoRepository historialEquipoRepository,
                          @org.springframework.context.annotation.Lazy org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
        this.prestamoLibroRepository = prestamoLibroRepository;
        this.renovacionEquipoRepository = renovacionEquipoRepository;
        this.renovacionLibroRepository = renovacionLibroRepository;
        this.multaRepository = multaRepository;
        this.historialLibroRepository = historialLibroRepository;
        this.historialEquipoRepository = historialEquipoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll().stream()
            .map(UsuarioResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO findById(@org.springframework.lang.NonNull Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return UsuarioResponseDTO.fromEntity(usuario);
    }

    @Transactional
    public UsuarioResponseDTO create(UsuarioRequestDTO dto) {
        String correoLimpio = dto.correo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correoLimpio)) {
            throw new BusinessException("El correo " + dto.correo() + " ya está registrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNombres(dto.nombres().trim());
        usuario.setApellidos(dto.apellidos().trim());
        usuario.setCorreo(correoLimpio);
        usuario.setPassword(passwordEncoder != null ? passwordEncoder.encode(dto.password()) : dto.password());
        usuario.setRol(dto.rol() != null ? dto.rol() : RolUsuario.aprendiz);
        usuario.setEstado(dto.estado() != null ? dto.estado() : EstadoUsuario.activo);
        usuario.setEmailVerificado(true);
        usuario.setFechaRegistro(LocalDateTime.now());
        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO findByCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim().toLowerCase())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario con correo " + correo, 0L));
        return UsuarioResponseDTO.fromEntity(usuario);
    }

    @Transactional
    public UsuarioResponseDTO update(@org.springframework.lang.NonNull Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        String nuevoCorreo = dto.correo().trim().toLowerCase();
        if (!usuario.getCorreo().equalsIgnoreCase(nuevoCorreo) && usuarioRepository.existsByCorreoIgnoreCase(nuevoCorreo)) {
            throw new BusinessException("El correo " + dto.correo() + " ya está registrado");
        }
        usuario.setNombres(dto.nombres().trim());
        usuario.setApellidos(dto.apellidos().trim());
        usuario.setCorreo(nuevoCorreo);
        if (dto.rol() != null) usuario.setRol(dto.rol());
        if (dto.estado() != null) usuario.setEstado(dto.estado());
        if (dto.password() != null && !dto.password().isBlank()) {
            usuario.setPassword(passwordEncoder != null ? passwordEncoder.encode(dto.password()) : dto.password());
        }
        return UsuarioResponseDTO.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public void delete(@org.springframework.lang.NonNull Long id, Long actorId) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        if (Objects.equals(id, actorId)) {
            throw new BusinessException("No puedes eliminar tu propia cuenta.");
        }

        // Renovaciones donde el usuario es solicitante o aprobador
        renovacionEquipoRepository.deleteAll(renovacionEquipoRepository.findByUsuarioId(id));
        renovacionLibroRepository.deleteAll(renovacionLibroRepository.findByUsuarioId(id));
        renovacionEquipoRepository.deleteAll(renovacionEquipoRepository.findByAdministradorId(id));
        renovacionLibroRepository.deleteAll(renovacionLibroRepository.findByAdministradorId(id));

        // Préstamos gestionados por el usuario: desvincular administrador
        prestamoRepository.findByAdministradorId(id).forEach(p -> p.setAdministrador(null));
        prestamoLibroRepository.findByAdministradorId(id).forEach(p -> p.setAdministrador(null));

        // Renovaciones asociadas a los préstamos del usuario
        List<Long> idsPrestamosEquipos = prestamoRepository.findByUsuarioId(id).stream()
            .map(Prestamo::getId).toList();
        List<Long> idsPrestamosLibros = prestamoLibroRepository.findByUsuarioId(id).stream()
            .map(PrestamoLibro::getId).toList();
        if (!idsPrestamosEquipos.isEmpty()) {
            renovacionEquipoRepository.deleteAll(renovacionEquipoRepository.findByPrestamoIdIn(idsPrestamosEquipos));
        }
        if (!idsPrestamosLibros.isEmpty()) {
            renovacionLibroRepository.deleteAll(renovacionLibroRepository.findByPrestamoLibroIdIn(idsPrestamosLibros));
        }

        // Multas del usuario y multas resueltas por el usuario
        multaRepository.deleteByUsuarioId(id);
        multaRepository.findByAdministradorResolucionId(id).forEach(m -> m.setAdministradorResolucion(null));

        // Préstamos del usuario
        prestamoRepository.deleteAll(prestamoRepository.findByUsuarioId(id));
        prestamoLibroRepository.deleteAll(prestamoLibroRepository.findByUsuarioId(id));

        // Historial de estados registrado por el usuario
        historialLibroRepository.deleteAll(historialLibroRepository.findByAdministradorId(id));
        historialEquipoRepository.deleteAll(historialEquipoRepository.findByAdministradorId(id));

        usuarioRepository.delete(usuario);
    }

    @Transactional(readOnly = true)
    public HistorialUsuarioResponseDTO historial(Long idUsuario, CurrentUser actor) {
        boolean esAdmin = actor.rol() == RolUsuario.administrador;
        if (!esAdmin && !Objects.equals(actor.id(), idUsuario)) {
            throw new BusinessException("No tienes permiso para ver este historial.");
        }
        LocalDateTime now = LocalDateTime.now();
        List<HistorialPrestamoItemDTO> items = new ArrayList<>();

        for (Prestamo p : prestamoRepository.findByUsuarioId(idUsuario)) {
            items.add(new HistorialPrestamoItemDTO(
                "Equipo",
                p.getEquipo() != null ? p.getEquipo().getNombre() : "Equipo eliminado",
                p.getFechaSolicitud(),
                p.getFechaDevolucionEsperada(),
                p.getFechaDevolucionReal(),
                estadoCalculado(p.getEstado(), p.getFechaDevolucionEsperada(), now),
                p.getObservaciones(),
                p.getRazonRechazo(),
                p.getObservacionDevolucion(),
                p.getEstadoFisicoDevolucion(),
                p.getEstadoRenovacion()
            ));
        }
        for (PrestamoLibro p : prestamoLibroRepository.findByUsuarioId(idUsuario)) {
            items.add(new HistorialPrestamoItemDTO(
                "Libro",
                p.getLibro() != null ? p.getLibro().getTitulo() : "Libro eliminado",
                p.getFechaSolicitud(),
                p.getFechaDevolucionEsperada(),
                p.getFechaDevolucionReal(),
                estadoCalculado(p.getEstado(), p.getFechaDevolucionEsperada(), now),
                p.getObservaciones(),
                p.getRazonRechazo(),
                p.getObservacionDevolucion(),
                p.getEstadoFisicoDevolucion(),
                p.getEstadoRenovacion()
            ));
        }

        items.sort(Comparator.comparing(HistorialPrestamoItemDTO::fechaSolicitud,
            Comparator.nullsLast(Comparator.reverseOrder())));

        long total = items.size();
        long activos = items.stream()
            .filter(i -> "pendiente".equals(i.estado()) || "aceptado".equals(i.estado()))
            .count();
        long atrasados = items.stream().filter(i -> "atrasado".equals(i.estado())).count();
        long devueltos = items.stream().filter(i -> "devuelto".equals(i.estado())).count();

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("activos", activos);
        stats.put("atrasados", atrasados);
        stats.put("devueltos", devueltos);

        return new HistorialUsuarioResponseDTO(items, stats);
    }

    private String estadoCalculado(EstadoPrestamo estado, LocalDateTime fechaEsperada, LocalDateTime now) {
        String base = estado != null ? estado.name() : "";
        if ((estado == EstadoPrestamo.pendiente || estado == EstadoPrestamo.aceptado)
                && fechaEsperada != null && fechaEsperada.isBefore(now)) {
            return "atrasado";
        }
        return base;
    }

    private String estadoCalculado(EstadoPrestamoLibro estado, LocalDateTime fechaEsperada, LocalDateTime now) {
        String base = estado != null ? estado.name() : "";
        if ((estado == EstadoPrestamoLibro.pendiente || estado == EstadoPrestamoLibro.aceptado)
                && fechaEsperada != null && fechaEsperada.isBefore(now)) {
            return "atrasado";
        }
        return base;
    }
}
