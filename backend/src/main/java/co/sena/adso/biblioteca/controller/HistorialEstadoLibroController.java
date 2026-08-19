package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.HistorialEstadoLibroRequestDTO;
import co.sena.adso.biblioteca.dto.HistorialEstadoLibroResponseDTO;
import co.sena.adso.biblioteca.service.HistorialEstadoLibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/historial-estado-libros")
@Tag(name = "Historial Estado Libros", description = "Historial de cambios de estado de los libros")
public class HistorialEstadoLibroController {

    private final HistorialEstadoLibroService historialService;

    public HistorialEstadoLibroController(HistorialEstadoLibroService historialService) {
        this.historialService = historialService;
    }

    @GetMapping
    @Operation(summary = "Listar historial de cambios de estado")
    public List<HistorialEstadoLibroResponseDTO> listar() {
        return historialService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de historial por ID")
    public HistorialEstadoLibroResponseDTO obtener(@PathVariable Long id) {
        return historialService.findById(id);
    }

    @GetMapping("/libro/{idLibro}")
    @Operation(summary = "Obtener historial de un libro")
    public List<HistorialEstadoLibroResponseDTO> listarPorLibro(@PathVariable Long idLibro) {
        return historialService.findByLibroId(idLibro);
    }

    @PostMapping
    @Operation(summary = "Crear registro de historial")
    public ResponseEntity<HistorialEstadoLibroResponseDTO> crear(@Valid @RequestBody HistorialEstadoLibroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historialService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar registro de historial")
    public HistorialEstadoLibroResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody HistorialEstadoLibroRequestDTO dto) {
        return historialService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar registro de historial")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        historialService.delete(id);
    }
}
