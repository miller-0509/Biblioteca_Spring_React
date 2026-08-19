package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.RenovacionLibroRequestDTO;
import co.sena.adso.biblioteca.dto.RenovacionLibroResponseDTO;
import co.sena.adso.biblioteca.service.RenovacionLibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/renovaciones-libros")
@Tag(name = "Renovaciones de Libros", description = "Gestión de renovaciones de préstamos de libros")
public class RenovacionLibroController {

    private final RenovacionLibroService renovacionService;

    public RenovacionLibroController(RenovacionLibroService renovacionService) {
        this.renovacionService = renovacionService;
    }

    @GetMapping
    @Operation(summary = "Listar renovaciones de libros")
    public List<RenovacionLibroResponseDTO> listar() {
        return renovacionService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener renovación por ID")
    public RenovacionLibroResponseDTO obtener(@PathVariable Long id) {
        return renovacionService.findById(id);
    }

    @GetMapping("/prestamo/{idPrestamoLibro}")
    @Operation(summary = "Listar renovaciones de un préstamo")
    public List<RenovacionLibroResponseDTO> listarPorPrestamo(@PathVariable Long idPrestamoLibro) {
        return renovacionService.findByPrestamoLibroId(idPrestamoLibro);
    }

    @PostMapping
    @Operation(summary = "Crear renovación de libro")
    public ResponseEntity<RenovacionLibroResponseDTO> crear(@Valid @RequestBody RenovacionLibroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(renovacionService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar renovación de libro")
    public RenovacionLibroResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody RenovacionLibroRequestDTO dto) {
        return renovacionService.update(id, dto);
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de la renovación")
    public RenovacionLibroResponseDTO cambiarEstado(@PathVariable Long id, @RequestBody String estado) {
        return renovacionService.cambiarEstado(id, estado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar renovación de libro")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        renovacionService.delete(id);
    }
}
