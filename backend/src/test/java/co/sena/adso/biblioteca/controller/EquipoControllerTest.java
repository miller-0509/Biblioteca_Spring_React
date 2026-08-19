package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.CambioEstadoEquipoRequestDTO;
import co.sena.adso.biblioteca.dto.EquipoRequestDTO;
import co.sena.adso.biblioteca.dto.EquipoResponseDTO;
import co.sena.adso.biblioteca.entity.EstadoEquipo;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.service.EquipoService;
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

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@WebMvcTest(controllers = EquipoController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
class EquipoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EquipoService equipoService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private EquipoResponseDTO equipoDTO() {
        return new EquipoResponseDTO(
            1L, "Laptop Lenovo T14", "Laptop", "Lenovo", "T14 Gen 3", "SN-001",
            EstadoEquipo.disponible, "Biblioteca", null, true, 15, null, false, null, null, null
        );
    }

    @Test
    void listar_shouldReturnListOfDTOs() throws Exception {
        when(equipoService.findByBusqueda(isNull(), isNull(), isNull())).thenReturn(List.of(equipoDTO()));

        mockMvc.perform(get("/api/equipos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].nombre").value("Laptop Lenovo T14"));
    }

    @Test
    void listarDisponibles_shouldReturnDisponibles() throws Exception {
        when(equipoService.findByDisponibles()).thenReturn(List.of(equipoDTO()));

        mockMvc.perform(get("/api/equipos/disponibles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].estado").value("disponible"));
    }

    @Test
    void obtener_shouldReturnDTO_whenEquipoExists() throws Exception {
        when(equipoService.findById(1L)).thenReturn(equipoDTO());

        mockMvc.perform(get("/api/equipos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtener_shouldReturn404_whenEquipoNotFound() throws Exception {
        when(equipoService.findById(99L)).thenThrow(new ResourceNotFoundException("Equipo", 99L));

        mockMvc.perform(get("/api/equipos/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void crear_shouldReturnCreatedStatus() throws Exception {
        EquipoRequestDTO request = new EquipoRequestDTO(
            "Laptop Lenovo T14", "Laptop", "Lenovo", "T14 Gen 3", "SN-001",
            EstadoEquipo.disponible, "Biblioteca", null, null, null, true, 15, null);
        when(equipoService.create(any())).thenReturn(equipoDTO());

        mockMvc.perform(post("/api/equipos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crear_shouldReturn400_whenInvalidBody() throws Exception {
        mockMvc.perform(post("/api/equipos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void crear_shouldReturn422_whenNumeroSerieDuplicado() throws Exception {
        EquipoRequestDTO request = new EquipoRequestDTO(
            "Laptop Lenovo T14", "Laptop", "Lenovo", "T14 Gen 3", "SN-001",
            EstadoEquipo.disponible, "Biblioteca", null, null, null, true, 15, null);
        when(equipoService.create(any())).thenThrow(new BusinessException("El número de serie SN-001 ya está registrado"));

        mockMvc.perform(post("/api/equipos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void cambiarEstado_shouldReturnDTO() throws Exception {
        EquipoResponseDTO response = new EquipoResponseDTO(
            1L, "Laptop Lenovo T14", "Laptop", "Lenovo", "T14 Gen 3", "SN-001",
            EstadoEquipo.mantenimiento, "Biblioteca", null, false, 15, null, false, null, null, null);
        when(equipoService.cambiarEstado(eq(1L), any())).thenReturn(response);

        CambioEstadoEquipoRequestDTO request = new CambioEstadoEquipoRequestDTO(
            EstadoEquipo.mantenimiento, "Laptop presenta falla", 1L);

        mockMvc.perform(put("/api/equipos/1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("mantenimiento"));
    }

    @Test
    void eliminar_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/equipos/1"))
            .andExpect(status().isNoContent());
    }
}
