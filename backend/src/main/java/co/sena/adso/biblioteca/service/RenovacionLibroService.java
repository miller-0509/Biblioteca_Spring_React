package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.RenovacionLibroRequestDTO;
import co.sena.adso.biblioteca.dto.RenovacionLibroResponseDTO;
import co.sena.adso.biblioteca.entity.PrestamoLibro;
import co.sena.adso.biblioteca.entity.RenovacionLibro;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.PrestamoLibroRepository;
import co.sena.adso.biblioteca.repository.RenovacionLibroRepository;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RenovacionLibroService {

    private final RenovacionLibroRepository renovacionRepository;
    private final PrestamoLibroRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;

    public RenovacionLibroService(
            RenovacionLibroRepository renovacionRepository,
            PrestamoLibroRepository prestamoRepository,
            UsuarioRepository usuarioRepository) {
        this.renovacionRepository = renovacionRepository;
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<RenovacionLibroResponseDTO> findAll() {
        return renovacionRepository.findAll().stream()
            .map(RenovacionLibroResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public RenovacionLibroResponseDTO findById(@org.springframework.lang.NonNull Long id) {
        RenovacionLibro renovacion = renovacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RenovacionLibro", id));
        return RenovacionLibroResponseDTO.fromEntity(renovacion);
    }

    @Transactional(readOnly = true)
    public List<RenovacionLibroResponseDTO> findByPrestamoLibroId(Long idPrestamoLibro) {
        return renovacionRepository.findByPrestamoLibroId(idPrestamoLibro).stream()
            .map(RenovacionLibroResponseDTO::fromEntity)
            .toList();
    }

    @Transactional
    public RenovacionLibroResponseDTO create(RenovacionLibroRequestDTO dto) {
        PrestamoLibro prestamoLibro = prestamoRepository.findById(dto.prestamoLibroId())
            .orElseThrow(() -> new ResourceNotFoundException("PrestamoLibro", dto.prestamoLibroId()));
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.usuarioId()));
        Usuario administrador = dto.administradorId() != null
            ? usuarioRepository.findById(dto.administradorId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.administradorId()))
            : null;
        RenovacionLibro renovacion = new RenovacionLibro(
            prestamoLibro,
            usuario,
            administrador,
            dto.fechaSolicitud() != null ? dto.fechaSolicitud() : LocalDateTime.now(),
            dto.fechaRespuesta(),
            dto.fechaEsperadaOriginal(),
            dto.fechaEsperadaNueva(),
            dto.estado() != null ? dto.estado() : "pendiente",
            dto.motivoSolicitud(),
            dto.motivoRechazo()
        );
        return RenovacionLibroResponseDTO.fromEntity(renovacionRepository.save(renovacion));
    }

    @Transactional
    public RenovacionLibroResponseDTO update(@org.springframework.lang.NonNull Long id, RenovacionLibroRequestDTO dto) {
        RenovacionLibro renovacion = renovacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RenovacionLibro", id));
        PrestamoLibro prestamoLibro = prestamoRepository.findById(dto.prestamoLibroId())
            .orElseThrow(() -> new ResourceNotFoundException("PrestamoLibro", dto.prestamoLibroId()));
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.usuarioId()));
        Usuario administrador = dto.administradorId() != null
            ? usuarioRepository.findById(dto.administradorId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", dto.administradorId()))
            : null;
        renovacion.setPrestamoLibro(prestamoLibro);
        renovacion.setUsuario(usuario);
        renovacion.setAdministrador(administrador);
        renovacion.setFechaSolicitud(dto.fechaSolicitud());
        renovacion.setFechaRespuesta(dto.fechaRespuesta());
        renovacion.setFechaEsperadaOriginal(dto.fechaEsperadaOriginal());
        renovacion.setFechaEsperadaNueva(dto.fechaEsperadaNueva());
        renovacion.setEstado(dto.estado());
        renovacion.setMotivoSolicitud(dto.motivoSolicitud());
        renovacion.setMotivoRechazo(dto.motivoRechazo());
        return RenovacionLibroResponseDTO.fromEntity(renovacionRepository.save(renovacion));
    }

    @Transactional
    public void delete(@org.springframework.lang.NonNull Long id) {
        if (!renovacionRepository.existsById(id)) {
            throw new ResourceNotFoundException("RenovacionLibro", id);
        }
        renovacionRepository.deleteById(id);
    }

    @Transactional
    public RenovacionLibroResponseDTO cambiarEstado(@org.springframework.lang.NonNull Long id, String estado) {
        RenovacionLibro renovacion = renovacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RenovacionLibro", id));
        renovacion.setEstado(estado);
        if (estado != null && !"pendiente".equalsIgnoreCase(estado) && renovacion.getFechaRespuesta() == null) {
            renovacion.setFechaRespuesta(LocalDateTime.now());
        }
        return RenovacionLibroResponseDTO.fromEntity(renovacionRepository.save(renovacion));
    }
}
