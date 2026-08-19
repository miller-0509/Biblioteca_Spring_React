package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.HistorialEstadoEquipoRequestDTO;
import co.sena.adso.biblioteca.dto.HistorialEstadoEquipoResponseDTO;
import co.sena.adso.biblioteca.entity.Equipo;
import co.sena.adso.biblioteca.entity.EstadoEquipo;
import co.sena.adso.biblioteca.entity.HistorialEstadoEquipo;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.EquipoRepository;
import co.sena.adso.biblioteca.repository.HistorialEstadoEquipoRepository;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class HistorialEstadoEquipoServiceTest {

    @Mock
    private HistorialEstadoEquipoRepository historialRepository;

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private HistorialEstadoEquipoService historialService;

    private HistorialEstadoEquipo historial;
    private Equipo equipo;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        equipo = new Equipo("Laptop Lenovo T14", "Laptop", "Lenovo", "T14", "SN-001",
            EstadoEquipo.disponible, "Biblioteca", null, null, true, 15, null, null);
        equipo.setId(1L);

        admin = new Usuario();
        admin.setId(1L);
        admin.setNombres("Miller");
        admin.setApellidos("Capera");

        historial = new HistorialEstadoEquipo(
            equipo, "disponible", "mantenimiento", "Falla en teclado", admin, LocalDateTime.now());
        historial.setId(1L);
    }

    @Test
    void findAll_shouldReturnHistorial() {
        when(historialRepository.findAll()).thenReturn(List.of(historial));
        List<HistorialEstadoEquipoResponseDTO> result = historialService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).estadoNuevo()).isEqualTo("mantenimiento");
    }

    @Test
    void findByEquipoId_shouldReturnHistorialOfEquipo() {
        when(historialRepository.findByEquipoId(1L)).thenReturn(List.of(historial));
        List<HistorialEstadoEquipoResponseDTO> result = historialService.findByEquipoId(1L);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).equipoNombre()).isEqualTo("Laptop Lenovo T14");
    }

    @Test
    void findById_shouldReturnDTO_whenExists() {
        when(historialRepository.findById(1L)).thenReturn(Optional.of(historial));
        HistorialEstadoEquipoResponseDTO result = historialService.findById(1L);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(historialRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> historialService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_shouldSaveAndReturnDTO() {
        HistorialEstadoEquipoRequestDTO dto = new HistorialEstadoEquipoRequestDTO(
            1L, "disponible", "dañado", "Pantalla rota", 1L, null);
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(historialRepository.save(any(HistorialEstadoEquipo.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        HistorialEstadoEquipoResponseDTO result = historialService.create(dto);
        assertThat(result).isNotNull();
        verify(historialRepository, times(1)).save(any(HistorialEstadoEquipo.class));
    }

    @Test
    void create_shouldThrowException_whenEquipoNotFound() {
        HistorialEstadoEquipoRequestDTO dto = new HistorialEstadoEquipoRequestDTO(
            99L, "disponible", "dañado", "Pantalla rota", 1L, null);
        when(equipoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> historialService.create(dto))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldDeleteWhenExists() {
        when(historialRepository.existsById(1L)).thenReturn(true);
        historialService.delete(1L);
        verify(historialRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(historialRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> historialService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
