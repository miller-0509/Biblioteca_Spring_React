package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.EstadoEquipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EquipoRequestDTO(
    @NotBlank(message = "El nombre del equipo es obligatorio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    String nombre,

    @NotBlank(message = "El tipo de equipo es obligatorio")
    @Size(max = 50, message = "Máximo 50 caracteres")
    String tipoEquipo,

    @Size(max = 100, message = "Máximo 100 caracteres")
    String marca,

    @Size(max = 100, message = "Máximo 100 caracteres")
    String modelo,

    @NotBlank(message = "El número de serie es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    String numeroSerie,

    EstadoEquipo estado,

    @Size(max = 150, message = "Máximo 150 caracteres")
    String ubicacion,

    LocalDate fechaCompra,

    @Size(max = 150, message = "Máximo 150 caracteres")
    String proveedor,

    @Size(max = 150, message = "Máximo 150 caracteres")
    String responsable,

    Boolean disponiblePrestamo,

    @Positive(message = "El tiempo máximo de préstamo debe ser positivo")
    Integer tiempoMaxPrestamo,

    String descripcion
) {}
