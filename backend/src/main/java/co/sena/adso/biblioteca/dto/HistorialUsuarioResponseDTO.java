package co.sena.adso.biblioteca.dto;

import java.util.List;
import java.util.Map;

public record HistorialUsuarioResponseDTO(
    List<HistorialPrestamoItemDTO> historial,
    Map<String, Long> stats
) {}
