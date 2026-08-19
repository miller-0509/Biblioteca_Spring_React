package co.sena.adso.biblioteca.dto;

import jakarta.validation.constraints.NotNull;

public record PrestamoRequestDTO(
    @NotNull(message = "El usuario es obligatorio")
    Long usuarioId,

    @NotNull(message = "El equipo es obligatorio")
    Long equipoId,

    Integer diasPrestamo,

    String observaciones
) {}
