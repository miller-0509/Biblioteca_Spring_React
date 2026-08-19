package co.sena.adso.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record HistorialEstadoLibroRequestDTO(
    @NotNull(message = "El libro es obligatorio")
    Long libroId,

    @NotBlank(message = "El estado anterior es obligatorio")
    @Size(max = 50, message = "Máximo 50 caracteres")
    String estadoAnterior,

    @NotBlank(message = "El estado nuevo es obligatorio")
    @Size(max = 50, message = "Máximo 50 caracteres")
    String estadoNuevo,

    @NotBlank(message = "La observación es obligatoria")
    String observacion,

    @NotNull(message = "El administrador es obligatorio")
    Long administradorId,

    LocalDateTime fecha
) {}
