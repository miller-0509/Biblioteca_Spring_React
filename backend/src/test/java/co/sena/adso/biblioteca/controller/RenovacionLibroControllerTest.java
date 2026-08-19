package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.RenovacionLibroRequestDTO;
import co.sena.adso.biblioteca.dto.RenovacionLibroResponseDTO;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.service.JwtService;
import co.sena.adso.biblioteca.service.RenovacionLibroService;
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
@WebMvcTest(controllers = RenovacionLibroController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
class RenovacionLibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RenovacionLibroService renovacionService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private RenovacionLibroResponseDTO renovacionDTO() {
        return new RenovacionLibroResponseDTO(
            3L, 5L, 1L, "Carlos Rueda", null, null,
            LocalDateTime.now(), null, LocalDateTime.now().plusDays(7), null,
            "pendiente", "Necesito más tiempo", null
        );
    }

    @Test
    void listar_shouldReturnListOfDTOs() throws Exception {
        when(renovacionService.findAll()).thenReturn(List.of(renovacionDTO()));

        mockMvc.perform(get("/api/renovaciones-libros"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(3))
            .andExpect(jsonPath("$[0].motivoSolicitud").value("Necesito más tiempo"));
    }

    @Test
    void obtener_shouldReturnDTO_whenExists() throws Exception {
        when(renovacionService.findById(3L)).thenReturn(renovacionDTO());

        mockMvc.perform(get("/api/renovaciones-libros/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void obtener_shouldReturn404_whenNotFound() throws Exception {
        when(renovacionService.findById(99L)).thenThrow(new ResourceNotFoundException("RenovacionLibro", 99L));

        mockMvc.perform(get("/api/renovaciones-libros/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listarPorPrestamo_shouldReturnList() throws Exception {
        when(renovacionService.findByPrestamoLibroId(5L)).thenReturn(List.of(renovacionDTO()));

        mockMvc.perform(get("/api/renovaciones-libros/prestamo/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].prestamoLibroId").value(5));
    }

    @Test
    void crear_shouldReturnCreatedStatus() throws Exception {
        RenovacionLibroRequestDTO request = new RenovacionLibroRequestDTO(5L, 1L, null, null, null, LocalDateTime.now().plusDays(7), null, null, "Necesito más tiempo", null);
        when(renovacionService.create(any())).thenReturn(renovacionDTO());

        mockMvc.perform(post("/api/renovaciones-libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void crear_shouldReturn400_whenInvalidBody() throws Exception {
        mockMvc.perform(post("/api/renovaciones-libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void crear_shouldReturn404_whenPrestamoNotFound() throws Exception {
        RenovacionLibroRequestDTO request = new RenovacionLibroRequestDTO(99L, 1L, null, null, null, LocalDateTime.now().plusDays(7), null, null, "Necesito más tiempo", null);
        when(renovacionService.create(any())).thenThrow(new ResourceNotFoundException("PrestamoLibro", 99L));

        mockMvc.perform(post("/api/renovaciones-libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void cambiarEstado_shouldReturnDTO() throws Exception {
        RenovacionLibroResponseDTO response = new RenovacionLibroResponseDTO(
            3L, 5L, 1L, "Carlos Rueda", null, null,
            LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusDays(7), null,
            "aprobada", "Necesito más tiempo", null
        );
        when(renovacionService.cambiarEstado(eq(3L), any())).thenReturn(response);

        mockMvc.perform(put("/api/renovaciones-libros/3/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"aprobada\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("aprobada"));
    }

    @Test
    void eliminar_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/renovaciones-libros/3"))
            .andExpect(status().isNoContent());
    }
}
