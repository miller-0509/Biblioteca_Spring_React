package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.HistorialUsuarioResponseDTO;
import co.sena.adso.biblioteca.dto.UsuarioRequestDTO;
import co.sena.adso.biblioteca.dto.UsuarioResponseDTO;
import co.sena.adso.biblioteca.dto.UsuarioUpdateDTO;
import co.sena.adso.biblioteca.entity.Equipo;
import co.sena.adso.biblioteca.entity.EstadoEquipo;
import co.sena.adso.biblioteca.entity.EstadoLibro;
import co.sena.adso.biblioteca.entity.EstadoPrestamo;
import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.entity.EstadoUsuario;
import co.sena.adso.biblioteca.entity.Libro;
import co.sena.adso.biblioteca.entity.Prestamo;
import co.sena.adso.biblioteca.entity.PrestamoLibro;
import co.sena.adso.biblioteca.entity.RolUsuario;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.repository.HistorialEstadoEquipoRepository;
import co.sena.adso.biblioteca.repository.HistorialEstadoLibroRepository;
import co.sena.adso.biblioteca.repository.MultaRepository;
import co.sena.adso.biblioteca.repository.PrestamoLibroRepository;
import co.sena.adso.biblioteca.repository.PrestamoRepository;
import co.sena.adso.biblioteca.repository.RenovacionEquipoRepository;
import co.sena.adso.biblioteca.repository.RenovacionLibroRepository;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import co.sena.adso.biblioteca.security.CurrentUser;
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
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private PrestamoLibroRepository prestamoLibroRepository;

    @Mock
    private RenovacionEquipoRepository renovacionEquipoRepository;

    @Mock
    private RenovacionLibroRepository renovacionLibroRepository;

    @Mock
    private MultaRepository multaRepository;

    @Mock
    private HistorialEstadoLibroRepository historialLibroRepository;

    @Mock
    private HistorialEstadoEquipoRepository historialEquipoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombres("Carlos");
        usuario.setApellidos("Rueda");
        usuario.setCorreo("carlos@email.com");
        usuario.setPassword("123456");
        usuario.setRol(RolUsuario.aprendiz);
        usuario.setEstado(EstadoUsuario.activo);
    }

    @Test
    void findAll_shouldReturnListOfDTOs() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        List<UsuarioResponseDTO> result = usuarioService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).nombres()).isEqualTo("Carlos");
        assertThat(result.get(0).correo()).isEqualTo("carlos@email.com");
    }

    @Test
    void findById_shouldReturnDTO_whenUsuarioExists() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        UsuarioResponseDTO result = usuarioService.findById(1L);
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrowException_whenUsuarioNotFound() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> usuarioService.findById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void create_shouldSaveAndReturnDTO() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("nuevo@email.com", "abcdef", "Nuevo", "Usuario", null, null);
        when(usuarioRepository.existsByCorreo("nuevo@email.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        UsuarioResponseDTO result = usuarioService.create(dto);
        assertThat(result).isNotNull();
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void create_shouldThrowBusinessException_whenCorreoDuplicado() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("carlos@email.com", "abcdef", "Nuevo", "Usuario", null, null);
        when(usuarioRepository.existsByCorreo("carlos@email.com")).thenReturn(true);
        assertThatThrownBy(() -> usuarioService.create(dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("carlos@email.com");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void findByCorreo_shouldReturnDTO_whenFound() {
        when(usuarioRepository.findByCorreo("carlos@email.com")).thenReturn(Optional.of(usuario));
        UsuarioResponseDTO result = usuarioService.findByCorreo("carlos@email.com");
        assertThat(result.correo()).isEqualTo("carlos@email.com");
    }

    @Test
    void findByCorreo_shouldThrowException_whenNotFound() {
        when(usuarioRepository.findByCorreo("noexiste@email.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> usuarioService.findByCorreo("noexiste@email.com"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldModifyAndSaveUsuario() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO(
            "nuevo@email.com", null, "Carlos", "Rueda", RolUsuario.bibliotecario, EstadoUsuario.activo);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByCorreo("nuevo@email.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        UsuarioResponseDTO result = usuarioService.update(1L, dto);
        assertThat(result.correo()).isEqualTo("nuevo@email.com");
        assertThat(result.rol()).isEqualTo(RolUsuario.bibliotecario);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void update_shouldThrowBusinessException_whenCorreoDeOtroUsuario() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO(
            "otro@email.com", null, "Carlos", "Rueda", RolUsuario.aprendiz, EstadoUsuario.activo);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByCorreo("otro@email.com")).thenReturn(true);
        assertThatThrownBy(() -> usuarioService.update(1L, dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("otro@email.com");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteUsuarioAndSusDependencias() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        usuarioService.delete(1L, 2L);
        verify(usuarioRepository, times(1)).delete(usuario);
    }

    @Test
    void delete_shouldThrowBusinessException_whenEsMismoUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        assertThatThrownBy(() -> usuarioService.delete(1L, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("propia");
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowException_whenUsuarioNotFound() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> usuarioService.delete(99L, 1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void historial_shouldReturnItemsAndStats_forAdmin() {
        Equipo equipo = new Equipo("Laptop", "Laptop", "Lenovo", "T14", "SN-1",
            EstadoEquipo.prestado, null, null, null, false, null, null, null);
        Prestamo prestamo = new Prestamo();
        prestamo.setEquipo(equipo);
        prestamo.setFechaSolicitud(LocalDateTime.now().minusDays(5));
        prestamo.setFechaDevolucionEsperada(LocalDateTime.now().minusDays(1));
        prestamo.setEstado(EstadoPrestamo.aceptado);

        Libro libro = new Libro("Cien años", "García Márquez", "Novela", "LIB-1",
            EstadoLibro.disponible, null, null, null, null, null, null);
        PrestamoLibro prestamoLibro = new PrestamoLibro();
        prestamoLibro.setLibro(libro);
        prestamoLibro.setFechaSolicitud(LocalDateTime.now().minusDays(10));
        prestamoLibro.setFechaDevolucionEsperada(LocalDateTime.now().minusDays(2));
        prestamoLibro.setFechaDevolucionReal(LocalDateTime.now().minusDays(2));
        prestamoLibro.setEstado(EstadoPrestamoLibro.devuelto);

        when(prestamoRepository.findByUsuarioId(1L)).thenReturn(List.of(prestamo));
        when(prestamoLibroRepository.findByUsuarioId(1L)).thenReturn(List.of(prestamoLibro));

        CurrentUser admin = new CurrentUser(9L, "admin@email.com", RolUsuario.administrador);
        HistorialUsuarioResponseDTO result = usuarioService.historial(1L, admin);

        assertThat(result.historial()).hasSize(2);
        assertThat(result.stats().get("total")).isEqualTo(2L);
        assertThat(result.stats().get("activos")).isEqualTo(0L);
        assertThat(result.stats().get("atrasados")).isEqualTo(1L);
        assertThat(result.stats().get("devueltos")).isEqualTo(1L);
    }

    @Test
    void historial_shouldAllowPropioUsuario() {
        CurrentUser propio = new CurrentUser(1L, "carlos@email.com", RolUsuario.aprendiz);
        HistorialUsuarioResponseDTO result = usuarioService.historial(1L, propio);
        assertThat(result.historial()).isEmpty();
        assertThat(result.stats().get("total")).isEqualTo(0L);
    }

    @Test
    void historial_shouldThrowBusinessException_whenNoAutorizado() {
        CurrentUser otro = new CurrentUser(2L, "otro@email.com", RolUsuario.aprendiz);
        assertThatThrownBy(() -> usuarioService.historial(1L, otro))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("permiso");
    }
}
