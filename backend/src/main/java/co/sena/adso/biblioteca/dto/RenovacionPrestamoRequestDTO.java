package co.sena.adso.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;

public record RenovacionPrestamoRequestDTO(
    @NotBlank(message = "Debes proporcionar un motivo para la renovación")
    String motivoRenovacion
) {}
