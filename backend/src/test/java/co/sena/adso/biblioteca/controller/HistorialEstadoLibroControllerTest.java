package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.HistorialEstadoLibroRequestDTO;
import co.sena.adso.biblioteca.dto.HistorialEstadoLibroResponseDTO;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.service.HistorialEstadoLibroService;
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
@WebMvcTest(controllers = HistorialEstadoLibroController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
class HistorialEstadoLibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistorialEstadoLibroService historialService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private HistorialEstadoLibroResponseDTO historialDTO() {
        return new HistorialEstadoLibroResponseDTO(
            7L, 2L, "Cien años de soledad", "disponible", "prestado", "Préstamo iniciado", 1L, "Ana López", LocalDateTime.now()
        );
    }

    @Test
    void listar_shouldReturnListOfDTOs() throws Exception {
        when(historialService.findAll()).thenReturn(List.of(historialDTO()));

        mockMvc.perform(get("/api/historial-estado-libros"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(7))
            .andExpect(jsonPath("$[0].estadoNuevo").value("prestado"));
    }

    @Test
    void obtener_shouldReturnDTO_whenExists() throws Exception {
        when(historialService.findById(7L)).thenReturn(historialDTO());

        mockMvc.perform(get("/api/historial-estado-libros/7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void obtener_shouldReturn404_whenNotFound() throws Exception {
        when(historialService.findById(99L)).thenThrow(new ResourceNotFoundException("HistorialEstadoLibro", 99L));

        mockMvc.perform(get("/api/historial-estado-libros/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listarPorLibro_shouldReturnList() throws Exception {
        when(historialService.findByLibroId(2L)).thenReturn(List.of(historialDTO()));

        mockMvc.perform(get("/api/historial-estado-libros/libro/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].libroId").value(2));
    }

    @Test
    void crear_shouldReturnCreatedStatus() throws Exception {
        HistorialEstadoLibroRequestDTO request = new HistorialEstadoLibroRequestDTO(2L, "disponible", "prestado", "Préstamo iniciado", 1L, null);
        when(historialService.create(any())).thenReturn(historialDTO());

        mockMvc.perform(post("/api/historial-estado-libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void crear_shouldReturn400_whenInvalidBody() throws Exception {
        mockMvc.perform(post("/api/historial-estado-libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void crear_shouldReturn404_whenLibroNotFound() throws Exception {
        HistorialEstadoLibroRequestDTO request = new HistorialEstadoLibroRequestDTO(99L, "disponible", "prestado", "Préstamo iniciado", 1L, null);
        when(historialService.create(any())).thenThrow(new ResourceNotFoundException("Libro", 99L));

        mockMvc.perform(post("/api/historial-estado-libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void actualizar_shouldReturnDTO() throws Exception {
        HistorialEstadoLibroRequestDTO request = new HistorialEstadoLibroRequestDTO(2L, "disponible", "mantenimiento", "En reparación", 1L, null);
        when(historialService.update(eq(7L), any())).thenReturn(historialDTO());

        mockMvc.perform(put("/api/historial-estado-libros/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void eliminar_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/historial-estado-libros/7"))
            .andExpect(status().isNoContent());
    }
}
