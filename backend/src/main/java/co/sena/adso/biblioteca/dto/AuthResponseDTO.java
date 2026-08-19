package co.sena.adso.biblioteca.dto;

public record AuthResponseDTO(
    String token,
    UsuarioResponseDTO usuario
) {}
