package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.HistorialEstadoEquipoRequestDTO;
import co.sena.adso.biblioteca.dto.HistorialEstadoEquipoResponseDTO;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.service.HistorialEstadoEquipoService;
import co.sena.adso.biblioteca.service.JwtService;
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
@WebMvcTest(controllers = HistorialEstadoEquipoController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
class HistorialEstadoEquipoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistorialEstadoEquipoService historialService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private HistorialEstadoEquipoResponseDTO historialDTO() {
        return new HistorialEstadoEquipoResponseDTO(
            1L, 1L, "Laptop Lenovo T14", "disponible", "mantenimiento",
            "Falla en teclado", 1L, "Miller Capera", LocalDateTime.now()
        );
    }

    @Test
    void listar_shouldReturnListOfDTOs() throws Exception {
        when(historialService.findAll()).thenReturn(List.of(historialDTO()));

        mockMvc.perform(get("/api/historial-estado-equipos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].estadoNuevo").value("mantenimiento"));
    }

    @Test
    void listarPorEquipo_shouldReturnHistorial() throws Exception {
        when(historialService.findByEquipoId(1L)).thenReturn(List.of(historialDTO()));

        mockMvc.perform(get("/api/historial-estado-equipos/equipo/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].equipoNombre").value("Laptop Lenovo T14"));
    }

    @Test
    void obtener_shouldReturnDTO_whenExists() throws Exception {
        when(historialService.findById(1L)).thenReturn(historialDTO());

        mockMvc.perform(get("/api/historial-estado-equipos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtener_shouldReturn404_whenNotFound() throws Exception {
        when(historialService.findById(99L)).thenThrow(new ResourceNotFoundException("HistorialEstadoEquipo", 99L));

        mockMvc.perform(get("/api/historial-estado-equipos/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void crear_shouldReturnCreatedStatus() throws Exception {
        HistorialEstadoEquipoRequestDTO request = new HistorialEstadoEquipoRequestDTO(
            1L, "disponible", "mantenimiento", "Falla en teclado", 1L, null);
        when(historialService.create(any())).thenReturn(historialDTO());

        mockMvc.perform(post("/api/historial-estado-equipos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crear_shouldReturn400_whenInvalidBody() throws Exception {
        mockMvc.perform(post("/api/historial-estado-equipos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void eliminar_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/historial-estado-equipos/1"))
            .andExpect(status().isNoContent());
    }
}
