package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.EstadoUsuario;
import co.sena.adso.biblioteca.entity.RolUsuario;
import co.sena.adso.biblioteca.entity.Usuario;
import java.time.LocalDateTime;

public record UsuarioResponseDTO(
    Long id,
    String nombres,
    String apellidos,
    String correo,
    RolUsuario rol,
    EstadoUsuario estado,
    LocalDateTime fechaRegistro,
    Boolean emailVerificado,
    LocalDateTime fechaVerificacion
) {
    public static UsuarioResponseDTO fromEntity(Usuario entity) {
        return new UsuarioResponseDTO(
            entity.getId(),
            entity.getNombres(),
            entity.getApellidos(),
            entity.getCorreo(),
            entity.getRol(),
            entity.getEstado(),
            entity.getFechaRegistro(),
            entity.getEmailVerificado(),
            entity.getFechaVerificacion()
        );
    }
}
