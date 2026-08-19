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
class RenovacionLibroServiceTest {

    @Mock
    private RenovacionLibroRepository renovacionRepository;

    @Mock
    private PrestamoLibroRepository prestamoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RenovacionLibroService renovacionService;

    private Usuario usuario;
    private PrestamoLibro prestamo;
    private RenovacionLibro renovacion;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombres("Carlos");
        usuario.setApellidos("Rueda");

        prestamo = new PrestamoLibro();
        prestamo.setId(5L);

        renovacion = new RenovacionLibro(
            prestamo, usuario, null, LocalDateTime.now(), null, LocalDateTime.now().plusDays(7), null,
            "pendiente", "Necesito más tiempo", null
        );
        renovacion.setId(3L);
    }

    @Test
    void findAll_shouldReturnListOfDTOs() {
        when(renovacionRepository.findAll()).thenReturn(List.of(renovacion));
        List<RenovacionLibroResponseDTO> result = renovacionService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(3L);
    }

    @Test
    void findById_shouldReturnDTO_whenExists() {
        when(renovacionRepository.findById(3L)).thenReturn(Optional.of(renovacion));
        RenovacionLibroResponseDTO result = renovacionService.findById(3L);
        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.estado()).isEqualTo("pendiente");
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(renovacionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> renovacionService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void findByPrestamoLibroId_shouldReturnList() {
        when(renovacionRepository.findByPrestamoLibroId(5L)).thenReturn(List.of(renovacion));
        assertThat(renovacionService.findByPrestamoLibroId(5L)).hasSize(1);
    }

    @Test
    void create_shouldSaveAndReturnDTO() {
        RenovacionLibroRequestDTO dto = new RenovacionLibroRequestDTO(5L, 1L, null, null, null, LocalDateTime.now().plusDays(7), null, null, "Necesito más tiempo", null);
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(prestamo));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(renovacionRepository.save(any(RenovacionLibro.class))).thenReturn(renovacion);
        RenovacionLibroResponseDTO result = renovacionService.create(dto);
        assertThat(result).isNotNull();
        verify(renovacionRepository, times(1)).save(any(RenovacionLibro.class));
    }

    @Test
    void create_shouldThrowException_whenPrestamoNotFound() {
        RenovacionLibroRequestDTO dto = new RenovacionLibroRequestDTO(99L, 1L, null, null, null, LocalDateTime.now().plusDays(7), null, null, "Necesito más tiempo", null);
        when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> renovacionService.create(dto))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_shouldThrowException_whenUsuarioNotFound() {
        RenovacionLibroRequestDTO dto = new RenovacionLibroRequestDTO(5L, 99L, null, null, null, LocalDateTime.now().plusDays(7), null, null, "Necesito más tiempo", null);
        when(prestamoRepository.findById(5L)).thenReturn(Optional.of(prestamo));
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> renovacionService.create(dto))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void cambiarEstado_shouldSetFechaRespuesta_whenEstadoNotPendiente() {
        when(renovacionRepository.findById(3L)).thenReturn(Optional.of(renovacion));
        when(renovacionRepository.save(any(RenovacionLibro.class))).thenReturn(renovacion);
        RenovacionLibroResponseDTO result = renovacionService.cambiarEstado(3L, "aprobada");
        assertThat(result.estado()).isEqualTo("aprobada");
        assertThat(renovacion.getFechaRespuesta()).isNotNull();
    }

    @Test
    void delete_shouldDelete_whenExists() {
        when(renovacionRepository.existsById(3L)).thenReturn(true);
        renovacionService.delete(3L);
        verify(renovacionRepository, times(1)).deleteById(3L);
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(renovacionRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> renovacionService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
