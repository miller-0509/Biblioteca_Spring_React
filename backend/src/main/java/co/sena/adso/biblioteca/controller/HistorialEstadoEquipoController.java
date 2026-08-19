package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.HistorialEstadoEquipoRequestDTO;
import co.sena.adso.biblioteca.dto.HistorialEstadoEquipoResponseDTO;
import co.sena.adso.biblioteca.service.HistorialEstadoEquipoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/historial-estado-equipos")
@Tag(name = "Historial Estado Equipos", description = "Historial de cambios de estado de los equipos")
public class HistorialEstadoEquipoController {

    private final HistorialEstadoEquipoService historialService;

    public HistorialEstadoEquipoController(HistorialEstadoEquipoService historialService) {
        this.historialService = historialService;
    }

    @GetMapping
    @Operation(summary = "Listar historial de cambios de estado de equipos")
    public List<HistorialEstadoEquipoResponseDTO> listar() {
        return historialService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de historial por ID")
    public HistorialEstadoEquipoResponseDTO obtener(@PathVariable Long id) {
        return historialService.findById(id);
    }

    @GetMapping("/equipo/{idEquipo}")
    @Operation(summary = "Obtener historial de un equipo")
    public List<HistorialEstadoEquipoResponseDTO> listarPorEquipo(@PathVariable Long idEquipo) {
        return historialService.findByEquipoId(idEquipo);
    }

    @PostMapping
    @Operation(summary = "Crear registro de historial")
    public ResponseEntity<HistorialEstadoEquipoResponseDTO> crear(@Valid @RequestBody HistorialEstadoEquipoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historialService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar registro de historial")
    public HistorialEstadoEquipoResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody HistorialEstadoEquipoRequestDTO dto) {
        return historialService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar registro de historial")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        historialService.delete(id);
    }
}
