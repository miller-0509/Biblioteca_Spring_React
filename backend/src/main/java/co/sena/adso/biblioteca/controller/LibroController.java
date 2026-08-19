package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.LibroRequestDTO;
import co.sena.adso.biblioteca.dto.LibroResponseDTO;
import co.sena.adso.biblioteca.entity.EstadoLibro;
import co.sena.adso.biblioteca.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/libros")
@Tag(name = "Libros", description = "Gestión de libros de la biblioteca")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    @Operation(summary = "Listar libros (no eliminados)")
    public org.springframework.data.domain.Page<LibroResponseDTO> listar(
            @org.springdoc.core.annotations.ParameterObject org.springframework.data.domain.Pageable pageable) {
        return libroService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener libro por ID")
    public LibroResponseDTO obtener(@PathVariable Long id) {
        return libroService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear libro")
    public ResponseEntity<LibroResponseDTO> crear(@Valid @RequestBody LibroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libroService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar libro")
    public LibroResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody LibroRequestDTO dto) {
        return libroService.update(id, dto);
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del libro")
    public LibroResponseDTO cambiarEstado(@PathVariable Long id, @RequestBody EstadoLibro estado) {
        return libroService.cambiarEstado(id, estado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar libro (borrado lógico)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        libroService.delete(id);
    }
}
