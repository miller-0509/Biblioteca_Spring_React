package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.EstadoUsuario;
import co.sena.adso.biblioteca.entity.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    @Size(max = 150, message = "Máximo 150 caracteres")
    String correo,

    @Size(min = 6, max = 255, message = "Mínimo 6 caracteres si se cambia la contraseña")
    String password,

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Máximo 100 caracteres")
    String nombres,

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Máximo 100 caracteres")
    String apellidos,

    RolUsuario rol,

    EstadoUsuario estado
) {}
