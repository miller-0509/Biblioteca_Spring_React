package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.HistorialEstadoLibro;
import java.time.LocalDateTime;

public record HistorialEstadoLibroResponseDTO(
    Long id,
    Long libroId,
    String libroTitulo,
    String estadoAnterior,
    String estadoNuevo,
    String observacion,
    Long administradorId,
    String administradorNombre,
    LocalDateTime fecha
) {
    public static HistorialEstadoLibroResponseDTO fromEntity(HistorialEstadoLibro entity) {
        return new HistorialEstadoLibroResponseDTO(
            entity.getId(),
            entity.getLibro() != null ? entity.getLibro().getId() : null,
            entity.getLibro() != null ? entity.getLibro().getTitulo() : null,
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
