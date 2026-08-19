package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.RenovacionEquipoResponseDTO;
import co.sena.adso.biblioteca.service.RenovacionEquipoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/renovaciones-equipos")
@Tag(name = "Renovaciones de Equipos", description = "Historial de solicitudes de renovación de préstamos de equipos")
public class RenovacionEquipoController {

    private final RenovacionEquipoService renovacionService;

    public RenovacionEquipoController(RenovacionEquipoService renovacionService) {
        this.renovacionService = renovacionService;
    }

    @GetMapping
    @Operation(summary = "Listar renovaciones de equipos")
    public List<RenovacionEquipoResponseDTO> listar() {
        return renovacionService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener renovación por ID")
    public RenovacionEquipoResponseDTO obtener(@PathVariable Long id) {
        return renovacionService.findById(id);
    }

    @GetMapping("/prestamo/{idPrestamo}")
    @Operation(summary = "Listar renovaciones de un préstamo")
    public List<RenovacionEquipoResponseDTO> listarPorPrestamo(@PathVariable Long idPrestamo) {
        return renovacionService.findByPrestamoId(idPrestamo);
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Listar renovaciones de un usuario")
    public List<RenovacionEquipoResponseDTO> listarPorUsuario(@PathVariable Long idUsuario) {
        return renovacionService.findByUsuarioId(idUsuario);
    }
}
