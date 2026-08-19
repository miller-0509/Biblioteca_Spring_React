package co.sena.adso.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record RenovacionLibroRequestDTO(
    @NotNull(message = "El préstamo de libro es obligatorio")
    Long prestamoLibroId,

    @NotNull(message = "El usuario es obligatorio")
    Long usuarioId,

    Long administradorId,

    LocalDateTime fechaSolicitud,

    LocalDateTime fechaRespuesta,

    @NotNull(message = "La fecha esperada original es obligatoria")
    LocalDateTime fechaEsperadaOriginal,

    LocalDateTime fechaEsperadaNueva,

    @Size(max = 20, message = "Máximo 20 caracteres")
    String estado,

    @NotBlank(message = "El motivo de la solicitud es obligatorio")
    String motivoSolicitud,

    @Size(max = 255, message = "Máximo 255 caracteres")
    String motivoRechazo
) {}
