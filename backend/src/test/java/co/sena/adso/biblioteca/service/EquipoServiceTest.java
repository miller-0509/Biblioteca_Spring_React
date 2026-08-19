package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.CambioEstadoEquipoRequestDTO;
import co.sena.adso.biblioteca.dto.EquipoRequestDTO;
import co.sena.adso.biblioteca.dto.EquipoResponseDTO;
import co.sena.adso.biblioteca.entity.Equipo;
import co.sena.adso.biblioteca.entity.EstadoEquipo;
import co.sena.adso.biblioteca.entity.HistorialEstadoEquipo;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.EquipoRepository;
import co.sena.adso.biblioteca.repository.HistorialEstadoEquipoRepository;
import co.sena.adso.biblioteca.repository.PrestamoRepository;
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
class EquipoServiceTest {

    @Mock
    private EquipoRepository equipoRepository;

    @Mock
    private HistorialEstadoEquipoRepository historialRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private EquipoService equipoService;

    private Equipo equipo;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        equipo = new Equipo(
            "Laptop Lenovo T14",
            "Laptop",
            "Lenovo",
            "T14 Gen 3",
            "SN-001",
            EstadoEquipo.disponible,
            "Biblioteca",
            "Distribuidora X",
            "Biblioteca",
            true,
            15,
            "Equipo de prueba",
            null
        );
        equipo.setId(1L);
        equipo.setFechaRegistro(LocalDateTime.now());
        equipo.setEliminado(false);

        admin = new Usuario();
        admin.setId(1L);
        admin.setNombres("Miller");
        admin.setApellidos("Capera");
    }

    @Test
    void findAll_shouldReturnOnlyNonDeletedEquipos() {
        when(equipoRepository.findByEliminadoFalse()).thenReturn(List.of(equipo));
        List<EquipoResponseDTO> result = equipoService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).nombre()).isEqualTo("Laptop Lenovo T14");
    }

    @Test
    void findById_shouldReturnDTO_whenEquipoExists() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        EquipoResponseDTO result = equipoService.findById(1L);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrowException_whenEquipoNotFound() {
        when(equipoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> equipoService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void findByDisponibles_shouldReturnOnlyDisponibles() {
        when(equipoRepository.findByEliminadoFalseAndEstadoAndDisponiblePrestamoTrue(EstadoEquipo.disponible))
            .thenReturn(List.of(equipo));
        List<EquipoResponseDTO> result = equipoService.findByDisponibles();
        assertThat(result).hasSize(1);
    }

    @Test
    void create_shouldSaveAndReturnDTO() {
        EquipoRequestDTO dto = new EquipoRequestDTO(
            "Monitor Samsung", "Monitor", "Samsung", "S24", "SN-002",
            EstadoEquipo.disponible, "Biblioteca", null, null, null, true, 10, null);
        when(equipoRepository.existsByNumeroSerie("SN-002")).thenReturn(false);
        when(equipoRepository.save(any(Equipo.class))).thenReturn(equipo);
        EquipoResponseDTO result = equipoService.create(dto);
        assertThat(result).isNotNull();
        verify(equipoRepository, times(1)).save(any(Equipo.class));
    }

    @Test
    void create_shouldThrowBusinessException_whenNumeroSerieDuplicado() {
        EquipoRequestDTO dto = new EquipoRequestDTO(
            "Monitor Samsung", "Monitor", "Samsung", "S24", "SN-001",
            EstadoEquipo.disponible, "Biblioteca", null, null, null, true, 10, null);
        when(equipoRepository.existsByNumeroSerie("SN-001")).thenReturn(true);
        assertThatThrownBy(() -> equipoService.create(dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("SN-001");
        verify(equipoRepository, never()).save(any());
    }

    @Test
    void update_shouldModifyAndSaveEquipo() {
        EquipoRequestDTO dto = new EquipoRequestDTO(
            "Laptop actualizada", "Laptop", "Lenovo", "T14", "SN-999",
            EstadoEquipo.prestado, "Almacen", null, null, null, false, 20, null);
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(equipoRepository.existsByNumeroSerie("SN-999")).thenReturn(false);
        when(equipoRepository.save(any(Equipo.class))).thenReturn(equipo);
        EquipoResponseDTO result = equipoService.update(1L, dto);
        assertThat(result).isNotNull();
        verify(equipoRepository, times(1)).save(any(Equipo.class));
    }

    @Test
    void delete_shouldSetEliminadoTrue() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(prestamoRepository.existsByEquipoIdAndEstadoIn(any(), any())).thenReturn(false);
        equipoService.delete(1L);
        assertThat(equipo.getEliminado()).isTrue();
        verify(equipoRepository, times(1)).save(equipo);
    }

    @Test
    void delete_shouldThrowException_whenEquipoNotFound() {
        when(equipoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> equipoService.delete(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldThrowBusinessException_whenPrestamoActivo() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(prestamoRepository.existsByEquipoIdAndEstadoIn(any(), any())).thenReturn(true);
        assertThatThrownBy(() -> equipoService.delete(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("préstamo activo");
        verify(equipoRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_shouldUpdateEstadoAndRegisterHistorial() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(prestamoRepository.existsByEquipoIdAndEstadoIn(any(), any())).thenReturn(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(equipoRepository.save(any(Equipo.class))).thenReturn(equipo);
        when(historialRepository.save(any(HistorialEstadoEquipo.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        CambioEstadoEquipoRequestDTO dto = new CambioEstadoEquipoRequestDTO(
            EstadoEquipo.mantenimiento, "Laptop presenta falla en teclado", 1L);

        EquipoResponseDTO result = equipoService.cambiarEstado(1L, dto);
        assertThat(result.estado()).isEqualTo(EstadoEquipo.mantenimiento);
        assertThat(equipo.getDisponiblePrestamo()).isFalse();
        verify(historialRepository, times(1)).save(any(HistorialEstadoEquipo.class));
    }

    @Test
    void cambiarEstado_shouldThrowBusinessException_whenMismoEstado() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(prestamoRepository.existsByEquipoIdAndEstadoIn(any(), any())).thenReturn(false);
        CambioEstadoEquipoRequestDTO dto = new CambioEstadoEquipoRequestDTO(
            EstadoEquipo.disponible, "Sin cambio real", 1L);
        assertThatThrownBy(() -> equipoService.cambiarEstado(1L, dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("ya se encuentra");
    }

    @Test
    void cambiarEstado_shouldThrowBusinessException_whenPrestamoActivo() {
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(equipo));
        when(prestamoRepository.existsByEquipoIdAndEstadoIn(any(), any())).thenReturn(true);
        CambioEstadoEquipoRequestDTO dto = new CambioEstadoEquipoRequestDTO(
            EstadoEquipo.mantenimiento, "Cambio con préstamo activo", 1L);
        assertThatThrownBy(() -> equipoService.cambiarEstado(1L, dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("préstamo activo");
        verify(equipoRepository, never()).save(any());
    }
}
