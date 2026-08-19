package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.HistorialUsuarioResponseDTO;
import co.sena.adso.biblioteca.dto.UsuarioRequestDTO;
import co.sena.adso.biblioteca.dto.UsuarioResponseDTO;
import co.sena.adso.biblioteca.dto.UsuarioUpdateDTO;
import co.sena.adso.biblioteca.security.CurrentUser;
import co.sena.adso.biblioteca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios de la biblioteca")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios")
    public List<UsuarioResponseDTO> listar() { return usuarioService.findAll(); }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public UsuarioResponseDTO obtener(@PathVariable Long id) { return usuarioService.findById(id); }

    @GetMapping("/correo/{correo}")
    @Operation(summary = "Buscar usuario por correo")
    public UsuarioResponseDTO buscarPorCorreo(@PathVariable String correo) {
        return usuarioService.findByCorreo(correo);
    }

    @PostMapping
    @Operation(summary = "Crear usuario")
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public UsuarioResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        return usuarioService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario y su historial de préstamos (solo administrador)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal CurrentUser currentUser) {
        usuarioService.delete(id, currentUser.id());
    }

    @GetMapping("/{id}/historial")
    @Operation(summary = "Historial unificado de préstamos del usuario (admin o el propio usuario)")
    public HistorialUsuarioResponseDTO historial(@PathVariable Long id,
                                                 @AuthenticationPrincipal CurrentUser currentUser) {
        return usuarioService.historial(id, currentUser);
    }
}
