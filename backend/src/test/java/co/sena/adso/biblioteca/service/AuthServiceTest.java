package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.AuthResponseDTO;
import co.sena.adso.biblioteca.dto.LoginRequestDTO;
import co.sena.adso.biblioteca.dto.MensajeDTO;
import co.sena.adso.biblioteca.dto.RecuperarPasswordRequestDTO;
import co.sena.adso.biblioteca.dto.RegistroRequestDTO;
import co.sena.adso.biblioteca.entity.EstadoUsuario;
import co.sena.adso.biblioteca.entity.RolUsuario;
import co.sena.adso.biblioteca.entity.TipoToken;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioVerificado;
    private Usuario usuarioNoVerificado;

    @BeforeEach
    void setUp() {
        usuarioVerificado = new Usuario();
        usuarioVerificado.setId(1L);
        usuarioVerificado.setNombres("Maria");
        usuarioVerificado.setApellidos("Perez");
        usuarioVerificado.setCorreo("maria@email.com");
        usuarioVerificado.setPassword("$2a$10$hashedpassword");
        usuarioVerificado.setRol(RolUsuario.aprendiz);
        usuarioVerificado.setEstado(EstadoUsuario.activo);
        usuarioVerificado.setEmailVerificado(true);

        usuarioNoVerificado = new Usuario();
        usuarioNoVerificado.setId(2L);
        usuarioNoVerificado.setNombres("Juan");
        usuarioNoVerificado.setApellidos("Gomez");
        usuarioNoVerificado.setCorreo("juan@email.com");
        usuarioNoVerificado.setPassword("$2a$10$hashedpassword");
        usuarioNoVerificado.setRol(RolUsuario.aprendiz);
        usuarioNoVerificado.setEstado(EstadoUsuario.activo);
        usuarioNoVerificado.setEmailVerificado(false);
    }

    @Test
    void login_shouldBlockUser_whenEmailNotVerified() {
        LoginRequestDTO dto = new LoginRequestDTO("juan@email.com", "Password123");
        when(usuarioRepository.findByCorreoIgnoreCase("juan@email.com")).thenReturn(Optional.of(usuarioNoVerificado));
        when(passwordEncoder.matches("Password123", "$2a$10$hashedpassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("verificar tu correo electrónico");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldAllowUser_whenEmailVerified() {
        LoginRequestDTO dto = new LoginRequestDTO("maria@email.com", "Password123");
        when(usuarioRepository.findByCorreoIgnoreCase("maria@email.com")).thenReturn(Optional.of(usuarioVerificado));
        when(passwordEncoder.matches("Password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtService.generateToken("maria@email.com")).thenReturn("mock-jwt-token");

        AuthResponseDTO response = authService.login(dto);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.usuario().correo()).isEqualTo("maria@email.com");
    }

    @Test
    void registrar_shouldCreateUnverifiedUser_andSendVerificationEmail() {
        RegistroRequestDTO dto = new RegistroRequestDTO("Carlos", "Rojas", "carlos@sena.edu.co", "Segura123");
        when(usuarioRepository.existsByCorreoIgnoreCase("carlos@sena.edu.co")).thenReturn(false);
        when(passwordEncoder.encode("Segura123")).thenReturn("$2a$10$encoded");
        when(tokenService.generarToken("carlos@sena.edu.co", TipoToken.verificacion_email)).thenReturn("token-12345");

        MensajeDTO result = authService.registrar(dto);

        assertThat(result.mensaje()).contains("Registro exitoso");
        verify(usuarioRepository, times(1)).save(argThat(u ->
            u.getCorreo().equals("carlos@sena.edu.co") &&
            Boolean.FALSE.equals(u.getEmailVerificado())
        ));
        verify(emailService, times(1)).enviarCorreoVerificacion("Carlos", "carlos@sena.edu.co", "token-12345");
    }

    @Test
    void verificarEmail_shouldSetEmailVerifiedTrue() {
        when(tokenService.consumirToken("valid-token", TipoToken.verificacion_email)).thenReturn("juan@email.com");
        when(usuarioRepository.findByCorreoIgnoreCase("juan@email.com")).thenReturn(Optional.of(usuarioNoVerificado));

        MensajeDTO result = authService.verificarEmail("valid-token");

        assertThat(result.mensaje()).contains("verificado exitosamente");
        assertThat(usuarioNoVerificado.getEmailVerificado()).isTrue();
        assertThat(usuarioNoVerificado.getFechaVerificacion()).isNotNull();
        verify(usuarioRepository, times(1)).save(usuarioNoVerificado);
    }

    @Test
    void reenviarVerificacion_shouldGenerateNewTokenAndSendEmail() {
        RecuperarPasswordRequestDTO dto = new RecuperarPasswordRequestDTO("juan@email.com");
        when(usuarioRepository.findByCorreoIgnoreCase("juan@email.com")).thenReturn(Optional.of(usuarioNoVerificado));
        when(tokenService.generarToken("juan@email.com", TipoToken.verificacion_email)).thenReturn("new-token-999");

        MensajeDTO result = authService.reenviarVerificacion(dto);

        assertThat(result.mensaje()).contains("enviado un nuevo enlace");
        verify(emailService, times(1)).enviarCorreoVerificacion("Juan", "juan@email.com", "new-token-999");
    }
}
