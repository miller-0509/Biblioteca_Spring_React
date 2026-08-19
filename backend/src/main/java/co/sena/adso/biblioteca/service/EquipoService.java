package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.CambioEstadoEquipoRequestDTO;
import co.sena.adso.biblioteca.dto.EquipoRequestDTO;
import co.sena.adso.biblioteca.dto.EquipoResponseDTO;
import co.sena.adso.biblioteca.entity.Equipo;
import co.sena.adso.biblioteca.entity.EstadoEquipo;
import co.sena.adso.biblioteca.entity.EstadoPrestamo;
import co.sena.adso.biblioteca.entity.HistorialEstadoEquipo;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.EquipoRepository;
import co.sena.adso.biblioteca.repository.HistorialEstadoEquipoRepository;
import co.sena.adso.biblioteca.repository.PrestamoRepository;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EquipoService {

    private static final List<EstadoPrestamo> ESTADOS_ACTIVOS =
        List.of(EstadoPrestamo.pendiente, EstadoPrestamo.aceptado);

    private final EquipoRepository equipoRepository;
    private final HistorialEstadoEquipoRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;

    public EquipoService(EquipoRepository equipoRepository,
                         HistorialEstadoEquipoRepository historialRepository,
                         UsuarioRepository usuarioRepository,
                         PrestamoRepository prestamoRepository) {
        this.equipoRepository = equipoRepository;
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
    }

    private void verificarSinPrestamoActivo(Long idEquipo) {
        if (prestamoRepository.existsByEquipoIdAndEstadoIn(idEquipo, ESTADOS_ACTIVOS)) {
            throw new BusinessException("El equipo tiene un préstamo activo y no puede modificarse ni eliminarse.");
        }
    }

    @Transactional(readOnly = true)
    public List<EquipoResponseDTO> findAll() {
        return equipoRepository.findByEliminadoFalse().stream()
            .map(EquipoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipoResponseDTO> findByBusqueda(String busqueda, EstadoEquipo estado, String tipo) {
        List<Equipo> equipos;
        if (busqueda != null && !busqueda.isBlank()) {
            equipos = equipoRepository.findByEliminadoFalse().stream()
                .filter(e -> e.getNombre().toLowerCase().contains(busqueda.toLowerCase())
                        || (e.getNumeroSerie() != null && e.getNumeroSerie().toLowerCase().contains(busqueda.toLowerCase()))
                        || (e.getMarca() != null && e.getMarca().toLowerCase().contains(busqueda.toLowerCase())))
                .toList();
        } else {
            equipos = equipoRepository.findByEliminadoFalse();
        }
        return equipos.stream()
            .filter(e -> estado == null || e.getEstado() == estado)
            .filter(e -> tipo == null || tipo.isBlank() || tipo.equalsIgnoreCase(e.getTipoEquipo()))
            .map(EquipoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipoResponseDTO> findByDisponibles() {
        return equipoRepository.findByEliminadoFalseAndEstadoAndDisponiblePrestamoTrue(EstadoEquipo.disponible)
            .stream()
            .map(EquipoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public EquipoResponseDTO findById(@org.springframework.lang.NonNull Long id) {
        Equipo equipo = equipoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipo", id));
        return EquipoResponseDTO.fromEntity(equipo);
    }

    @Transactional
    public EquipoResponseDTO create(EquipoRequestDTO dto) {
        if (equipoRepository.existsByNumeroSerie(dto.numeroSerie())) {
            throw new BusinessException("El número de serie " + dto.numeroSerie() + " ya está registrado");
        }
        Equipo equipo = new Equipo(
            dto.nombre(),
            dto.tipoEquipo(),
            dto.marca(),
            dto.modelo(),
            dto.numeroSerie(),
            dto.estado(),
            dto.ubicacion(),
            dto.proveedor(),
            dto.responsable(),
            dto.disponiblePrestamo(),
            dto.tiempoMaxPrestamo(),
            dto.descripcion(),
            dto.fechaCompra()
        );
        equipo.setFechaRegistro(LocalDateTime.now());
        equipo.setEliminado(false);
        if (equipo.getDisponiblePrestamo() == null) {
            equipo.setDisponiblePrestamo(equipo.getEstado() == null || equipo.getEstado() == EstadoEquipo.disponible);
        }
        return EquipoResponseDTO.fromEntity(equipoRepository.save(equipo));
    }

    @Transactional
    public EquipoResponseDTO update(@org.springframework.lang.NonNull Long id, EquipoRequestDTO dto) {
        Equipo equipo = equipoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipo", id));
        if (dto.numeroSerie() != null && !dto.numeroSerie().equals(equipo.getNumeroSerie())
                && equipoRepository.existsByNumeroSerie(dto.numeroSerie())) {
            throw new BusinessException("El número de serie " + dto.numeroSerie() + " ya está registrado");
        }
        equipo.setNombre(dto.nombre());
        equipo.setTipoEquipo(dto.tipoEquipo());
        equipo.setMarca(dto.marca());
        equipo.setModelo(dto.modelo());
        equipo.setNumeroSerie(dto.numeroSerie());
        equipo.setEstado(dto.estado());
        equipo.setUbicacion(dto.ubicacion());
        equipo.setFechaCompra(dto.fechaCompra());
        equipo.setProveedor(dto.proveedor());
        equipo.setResponsable(dto.responsable());
        equipo.setTiempoMaxPrestamo(dto.tiempoMaxPrestamo());
        equipo.setDescripcion(dto.descripcion());
        equipo.setDisponiblePrestamo(dto.disponiblePrestamo() != null
            ? dto.disponiblePrestamo()
            : dto.estado() == null || dto.estado() == EstadoEquipo.disponible);
        return EquipoResponseDTO.fromEntity(equipoRepository.save(equipo));
    }

    @Transactional
    public void delete(@org.springframework.lang.NonNull Long id) {
        Equipo equipo = equipoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipo", id));
        verificarSinPrestamoActivo(id);
        equipo.setEliminado(true);
        equipoRepository.save(equipo);
    }

    @Transactional
    public EquipoResponseDTO cambiarEstado(@org.springframework.lang.NonNull Long id, CambioEstadoEquipoRequestDTO dto) {
        Equipo equipo = equipoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipo", id));
        verificarSinPrestamoActivo(id);
        EstadoEquipo nuevoEstado = dto.estado();
        if (equipo.getEstado() == nuevoEstado) {
            throw new BusinessException("El equipo ya se encuentra en ese estado");
        }
        if (nuevoEstado == EstadoEquipo.disponible && Boolean.TRUE.equals(equipo.getEliminado())) {
            throw new BusinessException("No se puede marcar como disponible un equipo eliminado");
        }
        Usuario administrador = usuarioRepository.findById(dto.administradorId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.administradorId()));

        String estadoAnterior = equipo.getEstado() != null ? equipo.getEstado().name() : "sin_estado";
        equipo.setEstado(nuevoEstado);
        if (nuevoEstado != EstadoEquipo.disponible) {
            equipo.setDisponiblePrestamo(false);
        } else {
            equipo.setDisponiblePrestamo(true);
        }
        equipoRepository.save(equipo);

        HistorialEstadoEquipo historial = new HistorialEstadoEquipo(
            equipo,
            estadoAnterior,
            nuevoEstado.name(),
            dto.observacion(),
            administrador,
            LocalDateTime.now()
        );
        historialRepository.save(historial);
        return EquipoResponseDTO.fromEntity(equipo);
    }
}
