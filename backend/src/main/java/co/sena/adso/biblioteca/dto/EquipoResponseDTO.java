package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.EstadoEquipo;
import co.sena.adso.biblioteca.entity.Equipo;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EquipoResponseDTO(
    Long id,
    String nombre,
    String tipoEquipo,
    String marca,
    String modelo,
    String numeroSerie,
    EstadoEquipo estado,
    String ubicacion,
    LocalDateTime fechaRegistro,
    Boolean disponiblePrestamo,
    Integer tiempoMaxPrestamo,
    String descripcion,
    Boolean eliminado,
    LocalDate fechaCompra,
    String proveedor,
    String responsable
) {
    public static EquipoResponseDTO fromEntity(Equipo entity) {
        return new EquipoResponseDTO(
            entity.getId(),
            entity.getNombre(),
            entity.getTipoEquipo(),
            entity.getMarca(),
            entity.getModelo(),
            entity.getNumeroSerie(),
            entity.getEstado(),
            entity.getUbicacion(),
            entity.getFechaRegistro(),
            entity.getDisponiblePrestamo(),
            entity.getTiempoMaxPrestamo(),
            entity.getDescripcion(),
            entity.getEliminado(),
            entity.getFechaCompra(),
            entity.getProveedor(),
            entity.getResponsable()
        );
    }
}
