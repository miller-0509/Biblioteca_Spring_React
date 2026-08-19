package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.HistorialEstadoEquipoRequestDTO;
import co.sena.adso.biblioteca.dto.HistorialEstadoEquipoResponseDTO;
import co.sena.adso.biblioteca.entity.Equipo;
import co.sena.adso.biblioteca.entity.HistorialEstadoEquipo;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.EquipoRepository;
import co.sena.adso.biblioteca.repository.HistorialEstadoEquipoRepository;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistorialEstadoEquipoService {

    private final HistorialEstadoEquipoRepository historialRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;

    public HistorialEstadoEquipoService(
            HistorialEstadoEquipoRepository historialRepository,
            EquipoRepository equipoRepository,
            UsuarioRepository usuarioRepository) {
        this.historialRepository = historialRepository;
        this.equipoRepository = equipoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoEquipoResponseDTO> findAll() {
        return historialRepository.findAll().stream()
            .map(HistorialEstadoEquipoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public HistorialEstadoEquipoResponseDTO findById(@org.springframework.lang.NonNull Long id) {
        HistorialEstadoEquipo historial = historialRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("HistorialEstadoEquipo", id));
        return HistorialEstadoEquipoResponseDTO.fromEntity(historial);
    }

    @Transactional(readOnly = true)
    public List<HistorialEstadoEquipoResponseDTO> findByEquipoId(Long idEquipo) {
        return historialRepository.findByEquipoId(idEquipo).stream()
            .map(HistorialEstadoEquipoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional
    public HistorialEstadoEquipoResponseDTO create(HistorialEstadoEquipoRequestDTO dto) {
        Equipo equipo = equipoRepository.findById(dto.equipoId())
            .orElseThrow(() -> new ResourceNotFoundException("Equipo", dto.equipoId()));
        Usuario administrador = usuarioRepository.findById(dto.administradorId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.administradorId()));
        HistorialEstadoEquipo historial = new HistorialEstadoEquipo(
            equipo,
            dto.estadoAnterior(),
            dto.estadoNuevo(),
            dto.observacion(),
            administrador,
            dto.fecha() != null ? dto.fecha() : LocalDateTime.now()
        );
        return HistorialEstadoEquipoResponseDTO.fromEntity(historialRepository.save(historial));
    }

    @Transactional
    public HistorialEstadoEquipoResponseDTO update(@org.springframework.lang.NonNull Long id, HistorialEstadoEquipoRequestDTO dto) {
        HistorialEstadoEquipo historial = historialRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("HistorialEstadoEquipo", id));
        Equipo equipo = equipoRepository.findById(dto.equipoId())
            .orElseThrow(() -> new ResourceNotFoundException("Equipo", dto.equipoId()));
        Usuario administrador = usuarioRepository.findById(dto.administradorId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.administradorId()));
        historial.setEquipo(equipo);
        historial.setEstadoAnterior(dto.estadoAnterior());
        historial.setEstadoNuevo(dto.estadoNuevo());
        historial.setObservacion(dto.observacion());
        historial.setAdministrador(administrador);
        if (dto.fecha() != null) {
            historial.setFecha(dto.fecha());
        }
        return HistorialEstadoEquipoResponseDTO.fromEntity(historialRepository.save(historial));
    }

    @Transactional
    public void delete(@org.springframework.lang.NonNull Long id) {
        if (!historialRepository.existsById(id)) {
            throw new ResourceNotFoundException("HistorialEstadoEquipo", id);
        }
        historialRepository.deleteById(id);
    }
}
