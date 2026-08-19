package co.sena.adso.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;

public record MultaCondonarDTO(
    @NotBlank(message = "Debes ingresar una observación para condonar la sanción")
    String observacion
) {}
