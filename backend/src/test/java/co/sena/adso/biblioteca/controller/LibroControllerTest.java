package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.LibroRequestDTO;
import co.sena.adso.biblioteca.dto.LibroResponseDTO;
import co.sena.adso.biblioteca.entity.EstadoLibro;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.service.JwtService;
import co.sena.adso.biblioteca.service.LibroService;
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
@WebMvcTest(controllers = LibroController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LibroService libroService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private LibroResponseDTO libroDTO() {
        return new LibroResponseDTO(
            1L, "Cien años de soledad", "Gabriel García Márquez", "Novela", "LIB-001",
            EstadoLibro.disponible, "Estante A-1", null, true, 15, null, false, null, null, null
        );
    }

    @Test
    void listar_shouldReturnListOfDTOs() throws Exception {
        org.springframework.data.domain.Page<LibroResponseDTO> page =
            new org.springframework.data.domain.PageImpl<>(List.of(libroDTO()));
        when(libroService.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(page);

        mockMvc.perform(get("/api/libros"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].titulo").value("Cien años de soledad"));
    }

    @Test
    void obtener_shouldReturnDTO_whenLibroExists() throws Exception {
        when(libroService.findById(1L)).thenReturn(libroDTO());

        mockMvc.perform(get("/api/libros/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtener_shouldReturn404_whenLibroNotFound() throws Exception {
        when(libroService.findById(99L)).thenThrow(new ResourceNotFoundException("Libro", 99L));

        mockMvc.perform(get("/api/libros/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void crear_shouldReturnCreatedStatus() throws Exception {
        LibroRequestDTO request = new LibroRequestDTO("Libro nuevo", "Autor", "Terror", "LIB-002", EstadoLibro.disponible, null, 10, null, null, null, null, null);
        LibroResponseDTO response = new LibroResponseDTO(
            2L, "Libro nuevo", "Autor", "Terror", "LIB-002", EstadoLibro.disponible, null, null, true, 10, null, false, null, null, null);
        when(libroService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void crear_shouldReturn400_whenInvalidBody() throws Exception {
        mockMvc.perform(post("/api/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void crear_shouldReturn422_whenCodigoUnicoDuplicado() throws Exception {
        LibroRequestDTO request = new LibroRequestDTO("Libro nuevo", "Autor", "Terror", "LIB-001", EstadoLibro.disponible, null, 10, null, null, null, null, null);
        when(libroService.create(any())).thenThrow(new BusinessException("El código único LIB-001 ya está registrado"));

        mockMvc.perform(post("/api/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void cambiarEstado_shouldReturnDTO() throws Exception {
        LibroResponseDTO response = new LibroResponseDTO(
            1L, "Cien años de soledad", "Gabriel García Márquez", "Novela", "LIB-001",
            EstadoLibro.prestado, "Estante A-1", null, false, 15, null, false, null, null, null);
        when(libroService.cambiarEstado(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/libros/1/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"prestado\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("prestado"));
    }

    @Test
    void eliminar_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/libros/1"))
            .andExpect(status().isNoContent());
    }
}
