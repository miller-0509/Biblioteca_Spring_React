package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.RenovacionLibro;
import java.time.LocalDateTime;

public record RenovacionLibroResponseDTO(
    Long id,
    Long prestamoLibroId,
    Long usuarioId,
    String usuarioNombre,
    Long administradorId,
    String administradorNombre,
    LocalDateTime fechaSolicitud,
    LocalDateTime fechaRespuesta,
    LocalDateTime fechaEsperadaOriginal,
    LocalDateTime fechaEsperadaNueva,
    String estado,
    String motivoSolicitud,
    String motivoRechazo
) {
    public static RenovacionLibroResponseDTO fromEntity(RenovacionLibro entity) {
        return new RenovacionLibroResponseDTO(
            entity.getId(),
            entity.getPrestamoLibro() != null ? entity.getPrestamoLibro().getId() : null,
            entity.getUsuario() != null ? entity.getUsuario().getId() : null,
            entity.getUsuario() != null
                ? entity.getUsuario().getNombres() + " " + entity.getUsuario().getApellidos()
                : null,
            entity.getAdministrador() != null ? entity.getAdministrador().getId() : null,
            entity.getAdministrador() != null
                ? entity.getAdministrador().getNombres() + " " + entity.getAdministrador().getApellidos()
                : null,
            entity.getFechaSolicitud(),
            entity.getFechaRespuesta(),
            entity.getFechaEsperadaOriginal(),
            entity.getFechaEsperadaNueva(),
            entity.getEstado(),
            entity.getMotivoSolicitud(),
            entity.getMotivoRechazo()
        );
    }
}
