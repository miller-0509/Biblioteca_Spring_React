package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.AuthResponseDTO;
import co.sena.adso.biblioteca.dto.LoginRequestDTO;
import co.sena.adso.biblioteca.dto.MensajeDTO;
import co.sena.adso.biblioteca.dto.RecuperarPasswordRequestDTO;
import co.sena.adso.biblioteca.dto.RegistroRequestDTO;
import co.sena.adso.biblioteca.dto.RestablecerPasswordRequestDTO;
import co.sena.adso.biblioteca.dto.UsuarioResponseDTO;
import co.sena.adso.biblioteca.security.CurrentUser;
import co.sena.adso.biblioteca.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login, registro, verificación de email y recuperación de contraseña")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar usuario (rol aprendiz)")
    public MensajeDTO registrar(@Valid @RequestBody RegistroRequestDTO dto) {
        return authService.registrar(dto);
    }

    @GetMapping("/verificar/{token}")
    @Operation(summary = "Verificar correo electrónico con token")
    public MensajeDTO verificarEmail(@PathVariable String token) {
        return authService.verificarEmail(token);
    }

    @PostMapping("/reenviar-verificacion")
    @Operation(summary = "Reenviar enlace de verificación")
    public MensajeDTO reenviarVerificacion(@Valid @RequestBody RecuperarPasswordRequestDTO dto) {
        return authService.reenviarVerificacion(dto);
    }

    @PostMapping("/recuperar-password")
    @Operation(summary = "Solicitar recuperación de contraseña")
    public MensajeDTO recuperarPassword(@Valid @RequestBody RecuperarPasswordRequestDTO dto) {
        return authService.recuperarPassword(dto);
    }

    @PostMapping("/restablecer-password/{token}")
    @Operation(summary = "Restablecer contraseña con token")
    public MensajeDTO restablecerPassword(@PathVariable String token,
                                          @Valid @RequestBody RestablecerPasswordRequestDTO dto) {
        return authService.restablecerPassword(token, dto);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener información del usuario autenticado")
    public UsuarioResponseDTO me(@AuthenticationPrincipal CurrentUser currentUser) {
        return authService.me(currentUser.id());
    }
}
