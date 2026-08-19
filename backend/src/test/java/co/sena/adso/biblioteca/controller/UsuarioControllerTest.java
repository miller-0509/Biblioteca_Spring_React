package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.UsuarioRequestDTO;
import co.sena.adso.biblioteca.dto.UsuarioResponseDTO;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.exception.ResourceNotFoundException;
import co.sena.adso.biblioteca.service.JwtService;
import co.sena.adso.biblioteca.service.UsuarioService;
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
@WebMvcTest(controllers = UsuarioController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private UsuarioResponseDTO usuarioDTO() {
        return new UsuarioResponseDTO(1L, "Carlos", "Rueda", "carlos@email.com", null, null, null, false, null);
    }

    @Test
    void listar_shouldReturnListOfDTOs() throws Exception {
        when(usuarioService.findAll()).thenReturn(List.of(usuarioDTO()));

        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].correo").value("carlos@email.com"));
    }

    @Test
    void obtener_shouldReturnDTO_whenUsuarioExists() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(usuarioDTO());

        mockMvc.perform(get("/api/usuarios/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtener_shouldReturn404_whenUsuarioNotFound() throws Exception {
        when(usuarioService.findById(99L)).thenThrow(new ResourceNotFoundException("Usuario", 99L));

        mockMvc.perform(get("/api/usuarios/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void buscarPorCorreo_shouldReturnDTO_whenFound() throws Exception {
        when(usuarioService.findByCorreo("carlos@email.com")).thenReturn(usuarioDTO());

        mockMvc.perform(get("/api/usuarios/correo/carlos@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correo").value("carlos@email.com"));
    }

    @Test
    void buscarPorCorreo_shouldReturn404_whenNotFound() throws Exception {
        when(usuarioService.findByCorreo("noexiste@email.com")).thenThrow(new ResourceNotFoundException("Usuario con correo noexiste@email.com", 0L));

        mockMvc.perform(get("/api/usuarios/correo/noexiste@email.com"))
            .andExpect(status().isNotFound());
    }

    @Test
    void crear_shouldReturnCreatedStatus() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO("nuevo@email.com", "abcdef", "Nuevo", "Usuario", null, null);
        UsuarioResponseDTO response = new UsuarioResponseDTO(2L, "Nuevo", "Usuario", "nuevo@email.com", null, null, null, false, null);
        when(usuarioService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void crear_shouldReturn400_whenInvalidBody() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void crear_shouldReturn422_whenCorreoDuplicado() throws Exception {
        UsuarioRequestDTO request = new UsuarioRequestDTO("existente@email.com", "abcdef", "Nuevo", "Usuario", null, null);
        when(usuarioService.create(any())).thenThrow(new BusinessException("El correo existente@email.com ya está registrado"));

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.status").value(422));
    }
}
