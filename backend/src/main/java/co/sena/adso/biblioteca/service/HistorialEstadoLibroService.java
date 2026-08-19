package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.HistorialEstadoLibroRequestDTO;
import co.sena.adso.biblioteca.dto.HistorialEstadoLibroResponseDTO;
import co.sena.adso.biblioteca.entity.HistorialEstadoLibro;
import co.sena.adso.biblioteca.entity.Libro;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.HistorialEstadoLibroRepository;
import co.sena.adso.biblioteca.repository.LibroRepository;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistorialEstadoLibroService {

    private final HistorialEstadoLibroRepository historialRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public HistorialEstadoLibroService(
            HistorialEstadoLibroRepository historialRepository,
            LibroRepository libroRepository,
            UsuarioRepository usuarioRepository) {
        this.historialRepository = historialRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoLibroResponseDTO> findAll() {
        return historialRepository.findAll().stream()
            .map(HistorialEstadoLibroResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public HistorialEstadoLibroResponseDTO findById(@org.springframework.lang.NonNull Long id) {
        HistorialEstadoLibro historial = historialRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("HistorialEstadoLibro", id));
        return HistorialEstadoLibroResponseDTO.fromEntity(historial);
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoLibroResponseDTO> findByLibroId(Long idLibro) {
        return historialRepository.findByLibroId(idLibro).stream()
            .map(HistorialEstadoLibroResponseDTO::fromEntity)
            .toList();
    }

    @Transactional
    public HistorialEstadoLibroResponseDTO create(HistorialEstadoLibroRequestDTO dto) {
        Libro libro = libroRepository.findById(dto.libroId())
            .orElseThrow(() -> new ResourceNotFoundException("Libro", dto.libroId()));
        Usuario administrador = usuarioRepository.findById(dto.administradorId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.administradorId()));
        HistorialEstadoLibro historial = new HistorialEstadoLibro(
            libro,
            dto.estadoAnterior(),
            dto.estadoNuevo(),
            dto.observacion(),
            administrador,
            dto.fecha() != null ? dto.fecha() : LocalDateTime.now()
        );
        return HistorialEstadoLibroResponseDTO.fromEntity(historialRepository.save(historial));
    }

    @Transactional
    public HistorialEstadoLibroResponseDTO update(@org.springframework.lang.NonNull Long id, HistorialEstadoLibroRequestDTO dto) {
        HistorialEstadoLibro historial = historialRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("HistorialEstadoLibro", id));
        Libro libro = libroRepository.findById(dto.libroId())
            .orElseThrow(() -> new ResourceNotFoundException("Libro", dto.libroId()));
        Usuario administrador = usuarioRepository.findById(dto.administradorId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.administradorId()));
        historial.setLibro(libro);
        historial.setEstadoAnterior(dto.estadoAnterior());
        historial.setEstadoNuevo(dto.estadoNuevo());
        historial.setObservacion(dto.observacion());
        historial.setAdministrador(administrador);
        if (dto.fecha() != null) {
            historial.setFecha(dto.fecha());
        }
        return HistorialEstadoLibroResponseDTO.fromEntity(historialRepository.save(historial));
    }

    @Transactional
    public void delete(@org.springframework.lang.NonNull Long id) {
        if (!historialRepository.existsById(id)) {
            throw new ResourceNotFoundException("HistorialEstadoLibro", id);
        }
        historialRepository.deleteById(id);
    }
}
