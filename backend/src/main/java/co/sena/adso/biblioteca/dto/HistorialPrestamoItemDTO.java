package co.sena.adso.biblioteca.dto;

import java.time.LocalDateTime;

public record HistorialPrestamoItemDTO(
    String tipo,
    String recurso,
    LocalDateTime fechaSolicitud,
    LocalDateTime fechaDevolucionEsperada,
    LocalDateTime fechaDevolucionReal,
    String estado,
    String observaciones,
    String razonRechazo,
    String observacionDevolucion,
    String estadoFisicoDevolucion,
    String estadoRenovacion
) {}
