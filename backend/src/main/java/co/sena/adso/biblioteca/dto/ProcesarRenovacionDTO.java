package co.sena.adso.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;

public record ProcesarRenovacionDTO(
    @NotBlank(message = "Debes indicar si apruebas o rechazas la renovación")
    String accion,

    String motivoRechazo
) {}
