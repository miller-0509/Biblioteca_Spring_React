package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.EstadoEquipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoEquipoRequestDTO(
    @NotNull(message = "El estado nuevo es obligatorio")
    EstadoEquipo estado,

    @NotBlank(message = "Debe proporcionar una observación para cambiar el estado")
    String observacion,

    @NotNull(message = "El administrador es obligatorio")
    Long administradorId
) {}
