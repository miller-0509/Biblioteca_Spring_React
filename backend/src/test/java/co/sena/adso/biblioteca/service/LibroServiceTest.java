package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.LibroRequestDTO;
import co.sena.adso.biblioteca.dto.LibroResponseDTO;
import co.sena.adso.biblioteca.entity.EstadoLibro;
import co.sena.adso.biblioteca.entity.Libro;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.LibroRepository;
import co.sena.adso.biblioteca.repository.PrestamoLibroRepository;
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
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private PrestamoLibroRepository prestamoRepository;

    @InjectMocks
    private LibroService libroService;

    private Libro libro;

    @BeforeEach
    void setUp() {
        libro = new Libro(
            "Cien años de soledad",
            "Gabriel García Márquez",
            "Novela",
            "LIB-001",
            EstadoLibro.disponible,
            "Estante A-1",
            15,
            "Clásico colombiano",
            null,
            "Distribuidora X",
            "Biblioteca"
        );
        libro.setId(1L);
        libro.setFechaRegistro(LocalDateTime.now());
        libro.setDisponiblePrestamo(true);
        libro.setEliminado(false);
    }

    @Test
    void findAll_ShouldReturnPageOfDTOs() {
        org.springframework.data.domain.Page<Libro> page = new org.springframework.data.domain.PageImpl<>(List.of(libro));
        when(libroRepository.findByEliminadoFalse(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        org.springframework.data.domain.Page<LibroResponseDTO> result = libroService.findAll(org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(result).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).titulo()).isEqualTo("Cien años de soledad");
    }

    @Test
    void findById_shouldReturnDTO_whenLibroExists() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        LibroResponseDTO result = libroService.findById(1L);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrowException_whenLibroNotFound() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> libroService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_shouldSaveAndReturnDTO() {
        LibroRequestDTO dto = new LibroRequestDTO("Libro nuevo", "Autor", "Terror", "LIB-002", EstadoLibro.disponible, null, 10, null, null, null, null, null);
        when(libroRepository.existsByCodigoUnico("LIB-002")).thenReturn(false);
        when(libroRepository.save(any(Libro.class))).thenReturn(libro);
        LibroResponseDTO result = libroService.create(dto);
        assertThat(result).isNotNull();
        verify(libroRepository, times(1)).save(any(Libro.class));
    }

    @Test
    void create_shouldThrowBusinessException_whenCodigoUnicoDuplicado() {
        LibroRequestDTO dto = new LibroRequestDTO("Libro nuevo", "Autor", "Terror", "LIB-001", EstadoLibro.disponible, null, 10, null, null, null, null, null);
        when(libroRepository.existsByCodigoUnico("LIB-001")).thenReturn(true);
        assertThatThrownBy(() -> libroService.create(dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("LIB-001");
        verify(libroRepository, never()).save(any());
    }

    @Test
    void update_shouldModifyAndSaveLibro() {
        LibroRequestDTO dto = new LibroRequestDTO("Titulo actualizado", "Autor", "Terror", "LIB-999", EstadoLibro.prestado, "B-2", 20, null, null, null, null, true);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(libroRepository.existsByCodigoUnico("LIB-999")).thenReturn(false);
        when(libroRepository.save(any(Libro.class))).thenReturn(libro);
        LibroResponseDTO result = libroService.update(1L, dto);
        assertThat(result).isNotNull();
        verify(libroRepository, times(1)).save(any(Libro.class));
    }

    @Test
    void delete_shouldSetEliminadoTrue() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(prestamoRepository.existsByLibroIdAndEstadoIn(any(), any())).thenReturn(false);
        libroService.delete(1L);
        assertThat(libro.getEliminado()).isTrue();
        verify(libroRepository, times(1)).save(libro);
    }

    @Test
    void delete_shouldThrowException_whenLibroNotFound() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> libroService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldThrowBusinessException_whenPrestamoActivo() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(prestamoRepository.existsByLibroIdAndEstadoIn(any(), any())).thenReturn(true);
        assertThatThrownBy(() -> libroService.delete(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("préstamo activo");
        verify(libroRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_shouldUpdateEstadoAndDisponiblePrestamo() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(prestamoRepository.existsByLibroIdAndEstadoIn(any(), any())).thenReturn(false);
        when(libroRepository.save(any(Libro.class))).thenReturn(libro);
        LibroResponseDTO result = libroService.cambiarEstado(1L, EstadoLibro.prestado);
        assertThat(result.estado()).isEqualTo(EstadoLibro.prestado);
        assertThat(libro.getDisponiblePrestamo()).isFalse();
    }

    @Test
    void cambiarEstado_shouldThrowBusinessException_whenPrestamoActivo() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));
        when(prestamoRepository.existsByLibroIdAndEstadoIn(any(), any())).thenReturn(true);
        assertThatThrownBy(() -> libroService.cambiarEstado(1L, EstadoLibro.disponible))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("préstamo activo");
        verify(libroRepository, never()).save(any());
    }
}
