package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.LibroRequestDTO;
import co.sena.adso.biblioteca.dto.LibroResponseDTO;
import co.sena.adso.biblioteca.entity.EstadoLibro;
import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.entity.Libro;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.LibroRepository;
import co.sena.adso.biblioteca.repository.PrestamoLibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LibroService {

    private static final List<EstadoPrestamoLibro> ESTADOS_ACTIVOS =
        List.of(EstadoPrestamoLibro.pendiente, EstadoPrestamoLibro.aceptado);

    private final LibroRepository libroRepository;
    private final PrestamoLibroRepository prestamoRepository;

    public LibroService(LibroRepository libroRepository, PrestamoLibroRepository prestamoRepository) {
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    private void verificarSinPrestamoActivo(Long idLibro) {
        if (prestamoRepository.existsByLibroIdAndEstadoIn(idLibro, ESTADOS_ACTIVOS)) {
            throw new BusinessException("El libro tiene un préstamo activo y no puede modificarse ni eliminarse.");
        }
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LibroResponseDTO> findAll(org.springframework.data.domain.Pageable pageable) {
        return libroRepository.findByEliminadoFalse(pageable)
            .map(LibroResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public LibroResponseDTO findById(@org.springframework.lang.NonNull Long id) {
        Libro libro = libroRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Libro", id));
        return LibroResponseDTO.fromEntity(libro);
    }

    @Transactional
    public LibroResponseDTO create(LibroRequestDTO dto) {
        if (libroRepository.existsByCodigoUnico(dto.codigoUnico())) {
            throw new BusinessException("El código único " + dto.codigoUnico() + " ya está registrado");
        }
        Libro libro = new Libro(
            dto.titulo(),
            dto.autor(),
            dto.genero(),
            dto.codigoUnico(),
            dto.estado(),
            dto.ubicacion(),
            dto.tiempoMaxPrestamo(),
            dto.descripcion(),
            dto.fechaCompra(),
            dto.proveedor(),
            dto.responsable()
        );
        libro.setFechaRegistro(LocalDateTime.now());
        libro.setEliminado(false);
        libro.setDisponiblePrestamo(dto.disponiblePrestamo() != null
            ? dto.disponiblePrestamo()
            : dto.estado() == null || dto.estado() == EstadoLibro.disponible);
        return LibroResponseDTO.fromEntity(libroRepository.save(libro));
    }

    @Transactional
    public LibroResponseDTO update(@org.springframework.lang.NonNull Long id, LibroRequestDTO dto) {
        Libro libro = libroRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Libro", id));
        if (dto.codigoUnico() != null && !dto.codigoUnico().equals(libro.getCodigoUnico())
                && libroRepository.existsByCodigoUnico(dto.codigoUnico())) {
            throw new BusinessException("El código único " + dto.codigoUnico() + " ya está registrado");
        }
        libro.setTitulo(dto.titulo());
        libro.setAutor(dto.autor());
        libro.setGenero(dto.genero());
        libro.setCodigoUnico(dto.codigoUnico());
        libro.setEstado(dto.estado());
        libro.setUbicacion(dto.ubicacion());
        libro.setTiempoMaxPrestamo(dto.tiempoMaxPrestamo());
        libro.setDescripcion(dto.descripcion());
        libro.setFechaCompra(dto.fechaCompra());
        libro.setProveedor(dto.proveedor());
        libro.setResponsable(dto.responsable());
        libro.setDisponiblePrestamo(dto.disponiblePrestamo() != null
            ? dto.disponiblePrestamo()
            : dto.estado() == null || dto.estado() == EstadoLibro.disponible);
        return LibroResponseDTO.fromEntity(libroRepository.save(libro));
    }

    @Transactional
    public void delete(@org.springframework.lang.NonNull Long id) {
        Libro libro = libroRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Libro", id));
        verificarSinPrestamoActivo(id);
        libro.setEliminado(true);
        libroRepository.save(libro);
    }

    @Transactional
    public LibroResponseDTO cambiarEstado(@org.springframework.lang.NonNull Long id, EstadoLibro estado) {
        Libro libro = libroRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Libro", id));
        verificarSinPrestamoActivo(id);
        libro.setEstado(estado);
        libro.setDisponiblePrestamo(estado == EstadoLibro.disponible);
        return LibroResponseDTO.fromEntity(libroRepository.save(libro));
    }
}
