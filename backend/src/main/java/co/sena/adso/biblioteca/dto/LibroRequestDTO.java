package co.sena.adso.biblioteca.dto;

import co.sena.adso.biblioteca.entity.EstadoLibro;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record LibroRequestDTO(
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "Máximo 255 caracteres")
    String titulo,

    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    String autor,

    @NotBlank(message = "El género es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    String genero,

    @NotBlank(message = "El código único es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    String codigoUnico,

    EstadoLibro estado,

    @Size(max = 150, message = "Máximo 150 caracteres")
    String ubicacion,

    @Positive(message = "El tiempo máximo de préstamo debe ser positivo")
    Integer tiempoMaxPrestamo,

    String descripcion,

    LocalDate fechaCompra,

    @Size(max = 150, message = "Máximo 150 caracteres")
    String proveedor,

    @Size(max = 150, message = "Máximo 150 caracteres")
    String responsable,

    Boolean disponiblePrestamo
) {}
