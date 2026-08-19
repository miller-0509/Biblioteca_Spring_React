package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.RenovacionEquipoResponseDTO;
import co.sena.adso.biblioteca.entity.RenovacionEquipo;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.RenovacionEquipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RenovacionEquipoService {

    private final RenovacionEquipoRepository renovacionRepository;

    public RenovacionEquipoService(RenovacionEquipoRepository renovacionRepository) {
        this.renovacionRepository = renovacionRepository;
    }

    @Transactional(readOnly = true)
    public List<RenovacionEquipoResponseDTO> findAll() {
        return renovacionRepository.findAll().stream()
            .map(RenovacionEquipoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public RenovacionEquipoResponseDTO findById(Long id) {
        RenovacionEquipo renovacion = renovacionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RenovacionEquipo", id));
        return RenovacionEquipoResponseDTO.fromEntity(renovacion);
    }

    @Transactional(readOnly = true)
    public List<RenovacionEquipoResponseDTO> findByPrestamoId(Long idPrestamo) {
        return renovacionRepository.findByPrestamoId(idPrestamo).stream()
            .map(RenovacionEquipoResponseDTO::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<RenovacionEquipoResponseDTO> findByUsuarioId(Long idUsuario) {
        return renovacionRepository.findByUsuarioId(idUsuario).stream()
            .map(RenovacionEquipoResponseDTO::fromEntity)
            .toList();
    }
}
