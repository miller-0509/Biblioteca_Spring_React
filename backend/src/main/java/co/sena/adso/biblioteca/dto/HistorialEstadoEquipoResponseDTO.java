package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.HistorialEstadoEquipo;
import java.time.LocalDateTime;

public record HistorialEstadoEquipoResponseDTO(
    Long id,
    Long equipoId,
    String equipoNombre,
    String estadoAnterior,
    String estadoNuevo,
    String observacion,
    Long administradorId,
    String administradorNombre,
    LocalDateTime fecha
) {
    public static HistorialEstadoEquipoResponseDTO fromEntity(HistorialEstadoEquipo entity) {
        return new HistorialEstadoEquipoResponseDTO(
            entity.getId(),
            entity.getEquipo() != null ? entity.getEquipo().getId() : null,
            entity.getEquipo() != null ? entity.getEquipo().getNombre() : null,
            entity.getEstadoAnterior(),
            entity.getEstadoNuevo(),
            entity.getObservacion(),
            entity.getAdministrador() != null ? entity.getAdministrador().getId() : null,
            entity.getAdministrador() != null
                ? entity.getAdministrador().getNombres() + " " + entity.getAdministrador().getApellidos()
                : null,
            entity.getFecha()
        );
    }
}
