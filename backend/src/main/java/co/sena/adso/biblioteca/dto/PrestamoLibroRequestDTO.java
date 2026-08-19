package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record PrestamoLibroRequestDTO(
    @NotNull(message = "El usuario es obligatorio")
    Long usuarioId,

    @NotNull(message = "El libro es obligatorio")
    Long libroId,

    Long administradorId,

    LocalDateTime fechaSolicitud,

    LocalDateTime fechaAprobacion,

    LocalDateTime fechaDevolucionEsperada,

    LocalDateTime fechaDevolucionReal,

    EstadoPrestamoLibro estado,

    @Size(max = 255, message = "Máximo 255 caracteres")
    String razonRechazo,

    String observaciones,

    String observacionDevolucion,

    @Size(max = 20, message = "Máximo 20 caracteres")
    String estadoFisicoDevolucion,

    Integer renovacionesAplicadas,

    @Size(max = 20, message = "Máximo 20 caracteres")
    String estadoRenovacion
) {}
