package co.sena.adso.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PrestamoDevolucionDTO(
    @NotBlank(message = "Debes seleccionar el estado físico de la devolución")
    String estadoFisico,

    @NotNull(message = "Debes seleccionar el estado final del recurso")
    String estadoFinal,

    @NotBlank(message = "Debes ingresar una observación para la devolución")
    String observacionDevolucion
) {}
