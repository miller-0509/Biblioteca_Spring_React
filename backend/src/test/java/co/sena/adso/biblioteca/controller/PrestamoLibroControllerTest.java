package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.PrestamoLibroRequestDTO;
import co.sena.adso.biblioteca.dto.PrestamoLibroResponseDTO;
import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.service.JwtService;
import co.sena.adso.biblioteca.service.PrestamoLibroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(controllers = PrestamoLibroController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
class PrestamoLibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrestamoLibroService prestamoService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private PrestamoLibroResponseDTO prestamoDTO() {
        return new PrestamoLibroResponseDTO(
            10L, 1L, "Carlos Rueda", 2L, "Cien años de soledad", null, null,
            LocalDateTime.now(), null, LocalDateTime.now().plusDays(15), null,
            EstadoPrestamoLibro.pendiente, null, null, false, false, null, null, 0, null
        );
    }

    @Test
    void listar_shouldReturnListOfDTOs() throws Exception {
        when(prestamoService.findAll()).thenReturn(List.of(prestamoDTO()));

        mockMvc.perform(get("/api/prestamos-libros"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(10))
            .andExpect(jsonPath("$[0].libroTitulo").value("Cien años de soledad"));
    }

    @Test
    void obtener_shouldReturnDTO_whenExists() throws Exception {
        when(prestamoService.findById(10L)).thenReturn(prestamoDTO());

        mockMvc.perform(get("/api/prestamos-libros/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void obtener_shouldReturn404_whenNotFound() throws Exception {
        when(prestamoService.findById(99L)).thenThrow(new ResourceNotFoundException("PrestamoLibro", 99L));

        mockMvc.perform(get("/api/prestamos-libros/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listarPorUsuario_shouldReturnList() throws Exception {
        when(prestamoService.findByUsuarioId(1L)).thenReturn(List.of(prestamoDTO()));

        mockMvc.perform(get("/api/prestamos-libros/usuario/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].usuarioId").value(1));
    }

    @Test
    void listarPorLibro_shouldReturnList() throws Exception {
        when(prestamoService.findByLibroId(2L)).thenReturn(List.of(prestamoDTO()));

        mockMvc.perform(get("/api/prestamos-libros/libro/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].libroId").value(2));
    }

    @Test
    void crear_shouldReturnCreatedStatus() throws Exception {
        PrestamoLibroRequestDTO request = new PrestamoLibroRequestDTO(1L, 2L, null, null, null, null, null, null, null, null, null, null, null, null);
        when(prestamoService.create(any())).thenReturn(prestamoDTO());

        mockMvc.perform(post("/api/prestamos-libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void crear_shouldReturn400_whenInvalidBody() throws Exception {
        mockMvc.perform(post("/api/prestamos-libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void crear_shouldReturn404_whenUsuarioNotFound() throws Exception {
        PrestamoLibroRequestDTO request = new PrestamoLibroRequestDTO(99L, 2L, null, null, null, null, null, null, null, null, null, null, null, null);
        when(prestamoService.create(any())).thenThrow(new ResourceNotFoundException("Usuario", 99L));

        mockMvc.perform(post("/api/prestamos-libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void cambiarEstado_shouldReturnDTO() throws Exception {
        PrestamoLibroResponseDTO response = new PrestamoLibroResponseDTO(
            10L, 1L, "Carlos Rueda", 2L, "Cien años de soledad", null, null,
            LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusDays(15), null,
            EstadoPrestamoLibro.aceptado, null, null, false, false, null, null, 0, null
        );
        when(prestamoService.cambiarEstado(eq(10L), any())).thenReturn(response);

        mockMvc.perform(put("/api/prestamos-libros/10/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"aceptado\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("aceptado"));
    }

    @Test
    void eliminar_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/prestamos-libros/10"))
            .andExpect(status().isNoContent());
    }
}
