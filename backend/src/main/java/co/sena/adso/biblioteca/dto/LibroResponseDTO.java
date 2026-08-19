package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.EstadoLibro;
import co.sena.adso.biblioteca.entity.Libro;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LibroResponseDTO(
    Long id,
    String titulo,
    String autor,
    String genero,
    String codigoUnico,
    EstadoLibro estado,
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
    public static LibroResponseDTO fromEntity(Libro entity) {
        return new LibroResponseDTO(
            entity.getId(),
            entity.getTitulo(),
            entity.getAutor(),
            entity.getGenero(),
            entity.getCodigoUnico(),
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
