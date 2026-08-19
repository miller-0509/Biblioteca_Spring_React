package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.EstadoMulta;
import co.sena.adso.biblioteca.entity.Multa;
import co.sena.adso.biblioteca.entity.TipoRecurso;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record MultaResponseDTO(
    Long id,
    TipoRecurso tipoRecurso,
    Long idPrestamoEquipo,
    Long idPrestamoLibro,
    Long idUsuario,
    String usuarioNombre,
    String recursoNombre,
    Integer diasRetraso,
    Integer diasSuspension,
    LocalDateTime fechaGeneracion,
    LocalDateTime fechaInicioSuspension,
    LocalDateTime fechaFinSuspension,
    EstadoMulta estado,
    String observacion,
    String adminResolucionNombre,
    Long diasRestantesSuspension
) {
    public static MultaResponseDTO fromEntity(Multa entity) {
        return new MultaResponseDTO(
            entity.getId(),
            entity.getTipoRecurso(),
            entity.getPrestamoEquipo() != null ? entity.getPrestamoEquipo().getId() : null,
            entity.getPrestamoLibro() != null ? entity.getPrestamoLibro().getId() : null,
            entity.getUsuario() != null ? entity.getUsuario().getId() : null,
            entity.getUsuario() != null
                ? entity.getUsuario().getNombres() + " " + entity.getUsuario().getApellidos() : null,
            nombreRecurso(entity),
            entity.getDiasRetraso(),
            entity.getDiasSuspension(),
            entity.getFechaGeneracion(),
            entity.getFechaInicioSuspension(),
            entity.getFechaFinSuspension(),
            entity.getEstado(),
            entity.getObservacion(),
            entity.getAdministradorResolucion() != null
                ? entity.getAdministradorResolucion().getNombres() + " " + entity.getAdministradorResolucion().getApellidos()
                : null,
            entity.getFechaFinSuspension() != null
                ? Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now(), entity.getFechaFinSuspension()))
                : null
        );
    }

    private static String nombreRecurso(Multa entity) {
        if (entity.getPrestamoLibro() != null && entity.getPrestamoLibro().getLibro() != null) {
            return entity.getPrestamoLibro().getLibro().getTitulo();
        }
        if (entity.getPrestamoEquipo() != null && entity.getPrestamoEquipo().getEquipo() != null) {
            return entity.getPrestamoEquipo().getEquipo().getNombre();
        }
        return null;
    }
}
