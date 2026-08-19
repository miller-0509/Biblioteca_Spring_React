package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.entity.PrestamoLibro;
import java.time.LocalDateTime;

public record PrestamoLibroResponseDTO(
    Long id,
    Long usuarioId,
    String usuarioNombre,
    Long libroId,
    String libroTitulo,
    Long administradorId,
    String administradorNombre,
    LocalDateTime fechaSolicitud,
    LocalDateTime fechaAprobacion,
    LocalDateTime fechaDevolucionEsperada,
    LocalDateTime fechaDevolucionReal,
    EstadoPrestamoLibro estado,
    String razonRechazo,
    String observaciones,
    Boolean notificacionVencimientoEnviada,
    Boolean notificacionVencidoEnviada,
    String observacionDevolucion,
    String estadoFisicoDevolucion,
    Integer renovacionesAplicadas,
    String estadoRenovacion
) {
    public static PrestamoLibroResponseDTO fromEntity(PrestamoLibro entity) {
        return new PrestamoLibroResponseDTO(
            entity.getId(),
            entity.getUsuario() != null ? entity.getUsuario().getId() : null,
            entity.getUsuario() != null
                ? entity.getUsuario().getNombres() + " " + entity.getUsuario().getApellidos()
                : null,
            entity.getLibro() != null ? entity.getLibro().getId() : null,
            entity.getLibro() != null ? entity.getLibro().getTitulo() : null,
            entity.getAdministrador() != null ? entity.getAdministrador().getId() : null,
            entity.getAdministrador() != null
                ? entity.getAdministrador().getNombres() + " " + entity.getAdministrador().getApellidos()
                : null,
            entity.getFechaSolicitud(),
            entity.getFechaAprobacion(),
            entity.getFechaDevolucionEsperada(),
            entity.getFechaDevolucionReal(),
            entity.getEstado(),
            entity.getRazonRechazo(),
            entity.getObservaciones(),
            entity.getNotificacionVencimientoEnviada(),
            entity.getNotificacionVencidoEnviada(),
            entity.getObservacionDevolucion(),
            entity.getEstadoFisicoDevolucion(),
            entity.getRenovacionesAplicadas(),
            entity.getEstadoRenovacion()
        );
    }
}
