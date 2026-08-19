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
class HistorialEstadoLibroServiceTest {

    @Mock
    private HistorialEstadoLibroRepository historialRepository;

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private HistorialEstadoLibroService historialService;

    private Libro libro;
    private Usuario administrador;
    private HistorialEstadoLibro historial;

    @BeforeEach
    void setUp() {
        libro = new Libro();
        libro.setId(2L);
        libro.setTitulo("Cien años de soledad");

        administrador = new Usuario();
        administrador.setId(1L);
        administrador.setNombres("Ana");
        administrador.setApellidos("López");

        historial = new HistorialEstadoLibro(
            libro, "disponible", "prestado", "Préstamo iniciado", administrador, LocalDateTime.now()
        );
        historial.setId(7L);
    }

    @Test
    void findAll_shouldReturnListOfDTOs() {
        when(historialRepository.findAll()).thenReturn(List.of(historial));
        List<HistorialEstadoLibroResponseDTO> result = historialService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(7L);
    }

    @Test
    void findById_shouldReturnDTO_whenExists() {
        when(historialRepository.findById(7L)).thenReturn(Optional.of(historial));
        HistorialEstadoLibroResponseDTO result = historialService.findById(7L);
        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.estadoNuevo()).isEqualTo("prestado");
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(historialRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> historialService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void findByLibroId_shouldReturnList() {
        when(historialRepository.findByLibroId(2L)).thenReturn(List.of(historial));
        assertThat(historialService.findByLibroId(2L)).hasSize(1);
    }

    @Test
    void create_shouldSaveAndReturnDTO() {
        HistorialEstadoLibroRequestDTO dto = new HistorialEstadoLibroRequestDTO(2L, "disponible", "mantenimiento", "En reparación", 1L, null);
        when(libroRepository.findById(2L)).thenReturn(Optional.of(libro));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(administrador));
        when(historialRepository.save(any(HistorialEstadoLibro.class))).thenReturn(historial);
        HistorialEstadoLibroResponseDTO result = historialService.create(dto);
        assertThat(result).isNotNull();
        verify(historialRepository, times(1)).save(any(HistorialEstadoLibro.class));
    }

    @Test
    void create_shouldThrowException_whenLibroNotFound() {
        HistorialEstadoLibroRequestDTO dto = new HistorialEstadoLibroRequestDTO(99L, "disponible", "mantenimiento", "En reparación", 1L, null);
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> historialService.create(dto))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_shouldThrowException_whenAdministradorNotFound() {
        HistorialEstadoLibroRequestDTO dto = new HistorialEstadoLibroRequestDTO(2L, "disponible", "mantenimiento", "En reparación", 99L, null);
        when(libroRepository.findById(2L)).thenReturn(Optional.of(libro));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> historialService.create(dto))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void delete_shouldDelete_whenExists() {
        when(historialRepository.existsById(7L)).thenReturn(true);
        historialService.delete(7L);
        verify(historialRepository, times(1)).deleteById(7L);
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(historialRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> historialService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
