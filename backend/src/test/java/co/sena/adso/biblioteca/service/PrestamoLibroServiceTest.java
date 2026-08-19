package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.PrestamoLibroRequestDTO;
import co.sena.adso.biblioteca.dto.PrestamoLibroResponseDTO;
import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.entity.Libro;
import co.sena.adso.biblioteca.entity.PrestamoLibro;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.LibroRepository;
import co.sena.adso.biblioteca.repository.PrestamoLibroRepository;
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
class PrestamoLibroServiceTest {

    @Mock
    private PrestamoLibroRepository prestamoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private PrestamoLibroService prestamoService;

    private Usuario usuario;
    private Libro libro;
    private PrestamoLibro prestamo;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombres("Carlos");
        usuario.setApellidos("Rueda");

        libro = new Libro();
        libro.setId(2L);
        libro.setTitulo("Cien años de soledad");

        prestamo = new PrestamoLibro(
            usuario, libro, null, LocalDateTime.now(), null, LocalDateTime.now().plusDays(15), null,
            EstadoPrestamoLibro.pendiente, null, null, null, null, 0, null
        );
        prestamo.setId(10L);
    }

    @Test
    void findAll_shouldReturnListOfDTOs() {
        when(prestamoRepository.findAll()).thenReturn(List.of(prestamo));
        List<PrestamoLibroResponseDTO> result = prestamoService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
    }

    @Test
    void findById_shouldReturnDTO_whenExists() {
        when(prestamoRepository.findById(10L)).thenReturn(Optional.of(prestamo));
        PrestamoLibroResponseDTO result = prestamoService.findById(10L);
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.usuarioNombre()).isEqualTo("Carlos Rueda");
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> prestamoService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void findByUsuarioId_shouldReturnList() {
        when(prestamoRepository.findByUsuarioId(1L)).thenReturn(List.of(prestamo));
        assertThat(prestamoService.findByUsuarioId(1L)).hasSize(1);
    }

    @Test
    void findByLibroId_shouldReturnList() {
        when(prestamoRepository.findByLibroId(2L)).thenReturn(List.of(prestamo));
        assertThat(prestamoService.findByLibroId(2L)).hasSize(1);
    }

    @Test
    void create_shouldSaveAndReturnDTO() {
        PrestamoLibroRequestDTO dto = new PrestamoLibroRequestDTO(1L, 2L, null, null, null, null, null, null, null, null, null, null, null, null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(libroRepository.findById(2L)).thenReturn(Optional.of(libro));
        when(prestamoRepository.save(any(PrestamoLibro.class))).thenReturn(prestamo);
        PrestamoLibroResponseDTO result = prestamoService.create(dto);
        assertThat(result).isNotNull();
        verify(prestamoRepository, times(1)).save(any(PrestamoLibro.class));
    }

    @Test
    void create_shouldThrowException_whenUsuarioNotFound() {
        PrestamoLibroRequestDTO dto = new PrestamoLibroRequestDTO(99L, 2L, null, null, null, null, null, null, null, null, null, null, null, null);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> prestamoService.create(dto))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_shouldThrowException_whenLibroNotFound() {
        PrestamoLibroRequestDTO dto = new PrestamoLibroRequestDTO(1L, 99L, null, null, null, null, null, null, null, null, null, null, null, null);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> prestamoService.create(dto))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void cambiarEstado_shouldSetFechaAprobacion_whenAceptado() {
        when(prestamoRepository.findById(10L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(any(PrestamoLibro.class))).thenReturn(prestamo);
        PrestamoLibroResponseDTO result = prestamoService.cambiarEstado(10L, EstadoPrestamoLibro.aceptado);
        assertThat(result.estado()).isEqualTo(EstadoPrestamoLibro.aceptado);
        assertThat(prestamo.getFechaAprobacion()).isNotNull();
    }

    @Test
    void cambiarEstado_shouldSetFechaDevolucionReal_whenDevuelto() {
        when(prestamoRepository.findById(10L)).thenReturn(Optional.of(prestamo));
        when(prestamoRepository.save(any(PrestamoLibro.class))).thenReturn(prestamo);
        prestamoService.cambiarEstado(10L, EstadoPrestamoLibro.devuelto);
        assertThat(prestamo.getFechaDevolucionReal()).isNotNull();
    }

    @Test
    void delete_shouldDelete_whenExists() {
        when(prestamoRepository.existsById(10L)).thenReturn(true);
        prestamoService.delete(10L);
        verify(prestamoRepository, times(1)).deleteById(10L);
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(prestamoRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> prestamoService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
