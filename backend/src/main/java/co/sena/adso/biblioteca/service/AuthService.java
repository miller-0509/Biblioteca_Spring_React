package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.AuthResponseDTO;
import co.sena.adso.biblioteca.dto.LoginRequestDTO;
import co.sena.adso.biblioteca.dto.MensajeDTO;
import co.sena.adso.biblioteca.dto.RecuperarPasswordRequestDTO;
import co.sena.adso.biblioteca.dto.RegistroRequestDTO;
import co.sena.adso.biblioteca.dto.RestablecerPasswordRequestDTO;
import co.sena.adso.biblioteca.dto.UsuarioResponseDTO;
import co.sena.adso.biblioteca.entity.EstadoUsuario;
import co.sena.adso.biblioteca.entity.RolUsuario;
import co.sena.adso.biblioteca.entity.TipoToken;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");
    private static final Pattern PASSWORD_UPPER = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile("[0-9]");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final EmailService emailService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, TokenService tokenService, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO dto) {
        String correo = dto.correo().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
            .orElseThrow(() -> new BusinessException("Credenciales inválidas o cuenta no autorizada."));

        if (!verificarPassword(dto.password(), usuario)) {
            throw new BusinessException("Credenciales inválidas o cuenta no autorizada.");
        }
        if (usuario.getEstado() != EstadoUsuario.activo) {
            throw new BusinessException("Cuenta inactiva o bloqueada. Contacta al administrador.");
        }
        if (!Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            throw new BusinessException(
                "Debes verificar tu correo electrónico antes de iniciar sesión. Revisa tu bandeja de entrada o solicita un nuevo enlace de verificación.");
        }

        String token = jwtService.generateToken(usuario.getCorreo());
        return new AuthResponseDTO(token, UsuarioResponseDTO.fromEntity(usuario));
    }

    @Transactional
    public MensajeDTO registrar(RegistroRequestDTO dto) {
        String nombres = dto.nombres().trim();
        String apellidos = dto.apellidos().trim();
        String correo = dto.correo().trim().toLowerCase();
        String password = dto.password();

        validarPassword(password);
        if (!EMAIL_PATTERN.matcher(correo).matches()) {
            throw new BusinessException("El formato del correo electrónico no es válido.");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new BusinessException("El correo ya está registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(RolUsuario.aprendiz);
        usuario.setEstado(EstadoUsuario.activo);
        usuario.setEmailVerificado(false);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = tokenService.generarToken(correo, TipoToken.verificacion_email);
        emailService.enviarCorreoVerificacion(nombres, correo, token);

        return new MensajeDTO("Registro exitoso. Hemos enviado un enlace de verificación a tu correo electrónico. Revisa tu bandeja de entrada y la carpeta de spam.");
    }

    @Transactional
    public MensajeDTO verificarEmail(String token) {
        String correo = tokenService.consumirToken(token, TipoToken.verificacion_email);
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
            .orElseThrow(() -> new BusinessException("No se encontró una cuenta asociada a este enlace."));
        if (Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            return new MensajeDTO("Tu correo electrónico ya fue verificado anteriormente. Puedes iniciar sesión.");
        }
        usuario.setEmailVerificado(true);
        usuario.setFechaVerificacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
        return new MensajeDTO("¡Tu correo electrónico ha sido verificado exitosamente! Ya puedes iniciar sesión.");
    }

    @Transactional
    public MensajeDTO reenviarVerificacion(RecuperarPasswordRequestDTO dto) {
        String correo = dto.correo().trim().toLowerCase();
        usuarioRepository.findByCorreoIgnoreCase(correo).ifPresent(usuario -> {
            if (Boolean.FALSE.equals(usuario.getEmailVerificado())) {
                String token = tokenService.generarToken(correo, TipoToken.verificacion_email);
                emailService.enviarCorreoVerificacion(usuario.getNombres(), correo, token);
            }
        });
        return new MensajeDTO("Si el correo está registrado y no ha sido verificado, hemos enviado un nuevo enlace de verificación. Revisa tu bandeja de entrada y la carpeta de spam.");
    }

    @Transactional
    public MensajeDTO recuperarPassword(RecuperarPasswordRequestDTO dto) {
        String correo = dto.correo().trim().toLowerCase();
        usuarioRepository.findByCorreoIgnoreCase(correo).ifPresent(usuario -> {
            if (usuario.getEstado() == EstadoUsuario.activo) {
                String token = tokenService.generarToken(correo, TipoToken.recuperar_password);
                emailService.enviarCorreoRecuperacion(usuario.getNombres(), correo, token);
            }
        });
        return new MensajeDTO("Si existe una cuenta asociada a ese correo, hemos enviado las instrucciones para restablecer tu contraseña. Revisa tu bandeja de entrada y la carpeta de spam.");
    }

    @Transactional
    public MensajeDTO restablecerPassword(String token, RestablecerPasswordRequestDTO dto) {
        String correo = tokenService.consumirToken(token, TipoToken.recuperar_password);
        if (!dto.password().equals(dto.passwordConfirm())) {
            throw new BusinessException("Las contraseñas no coinciden.");
        }
        validarPassword(dto.password());
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
            .orElseThrow(() -> new BusinessException("No se encontró una cuenta asociada a este enlace."));
        usuario.setPassword(passwordEncoder.encode(dto.password()));
        usuarioRepository.save(usuario);
        return new MensajeDTO("¡Tu contraseña ha sido restablecida exitosamente! Ya puedes iniciar sesión con tu nueva contraseña.");
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO me(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
        return UsuarioResponseDTO.fromEntity(usuario);
    }

    private boolean verificarPassword(String passwordPlano, Usuario usuario) {
        String almacenada = usuario.getPassword();
        if (almacenada != null && almacenada.startsWith("$2")) {
            return passwordEncoder.matches(passwordPlano, almacenada);
        }
        // Contraseña legacy sin hash (migración): comparación directa y upgrade automático.
        if (passwordPlano.equals(almacenada)) {
            usuario.setPassword(passwordEncoder.encode(passwordPlano));
            usuarioRepository.save(usuario);
            return true;
        }
        return false;
    }

    private void validarPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (!PASSWORD_UPPER.matcher(password).find()) {
            throw new BusinessException("La contraseña debe contener al menos una letra mayúscula.");
        }
        if (!PASSWORD_DIGIT.matcher(password).find()) {
            throw new BusinessException("La contraseña debe contener al menos un número.");
        }
    }

    private boolean esStaff(RolUsuario rol) {
        return rol == RolUsuario.administrador
            || rol == RolUsuario.bibliotecario
            || rol == RolUsuario.almacenista;
    }
}
