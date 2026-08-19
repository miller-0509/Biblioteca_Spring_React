package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.EstadoPrestamo;
import co.sena.adso.biblioteca.entity.Prestamo;
import java.time.LocalDateTime;

public record PrestamoResponseDTO(
    Long id,
    Long usuarioId,
    String usuarioNombre,
    Long equipoId,
    String equipoNombre,
    Long administradorId,
    String administradorNombre,
    LocalDateTime fechaSolicitud,
    LocalDateTime fechaAprobacion,
    LocalDateTime fechaDevolucionEsperada,
    LocalDateTime fechaDevolucionReal,
    EstadoPrestamo estado,
    String razonRechazo,
    String observaciones,
    Boolean notificacionVencimientoEnviada,
    Boolean notificacionVencidoEnviada,
    String observacionDevolucion,
    String estadoFisicoDevolucion,
    Integer renovacionesAplicadas,
    String estadoRenovacion,
    Integer diasRestantes
) {
    public static PrestamoResponseDTO fromEntity(Prestamo entity) {
        return new PrestamoResponseDTO(
            entity.getId(),
            entity.getUsuario() != null ? entity.getUsuario().getId() : null,
            entity.getUsuario() != null
                ? entity.getUsuario().getNombres() + " " + entity.getUsuario().getApellidos()
                : null,
            entity.getEquipo() != null ? entity.getEquipo().getId() : null,
            entity.getEquipo() != null ? entity.getEquipo().getNombre() : null,
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
            entity.getEstadoRenovacion(),
            calcularDiasRestantes(entity)
        );
    }

    private static Integer calcularDiasRestantes(Prestamo entity) {
        if (entity.getEstado() != EstadoPrestamo.aceptado || entity.getFechaDevolucionEsperada() == null) {
            return null;
        }
        long dias = java.time.temporal.ChronoUnit.DAYS.between(
            LocalDateTime.now(), entity.getFechaDevolucionEsperada());
        return (int) dias;
    }
}
