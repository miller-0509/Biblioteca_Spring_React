package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.CambioEstadoEquipoRequestDTO;
import co.sena.adso.biblioteca.dto.EquipoRequestDTO;
import co.sena.adso.biblioteca.dto.EquipoResponseDTO;
import co.sena.adso.biblioteca.entity.EstadoEquipo;
import co.sena.adso.biblioteca.service.EquipoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/equipos")
@Tag(name = "Equipos", description = "Gestión de equipos de la biblioteca")
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping
    @Operation(summary = "Listar equipos (no eliminados) con búsqueda y filtros")
    public List<EquipoResponseDTO> listar(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) EstadoEquipo estado,
            @RequestParam(required = false) String tipo) {
        return equipoService.findByBusqueda(busqueda, estado, tipo);
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Listar equipos disponibles para préstamo")
    public List<EquipoResponseDTO> listarDisponibles() {
        return equipoService.findByDisponibles();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener equipo por ID")
    public EquipoResponseDTO obtener(@PathVariable Long id) {
        return equipoService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear equipo")
    public ResponseEntity<EquipoResponseDTO> crear(@Valid @RequestBody EquipoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipoService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar equipo")
    public EquipoResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody EquipoRequestDTO dto) {
        return equipoService.update(id, dto);
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del equipo (registra historial)")
    public EquipoResponseDTO cambiarEstado(@PathVariable Long id,
                                           @Valid @RequestBody CambioEstadoEquipoRequestDTO dto) {
        return equipoService.cambiarEstado(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar equipo (borrado lógico)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        equipoService.delete(id);
    }
}
