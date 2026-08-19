package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.PrestamoDevolucionDTO;
import co.sena.adso.biblioteca.dto.PrestamoLibroRequestDTO;
import co.sena.adso.biblioteca.dto.PrestamoLibroResponseDTO;
import co.sena.adso.biblioteca.dto.ProcesarRenovacionDTO;
import co.sena.adso.biblioteca.dto.RenovacionPrestamoRequestDTO;
import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.security.CurrentUser;
import co.sena.adso.biblioteca.service.PrestamoLibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prestamos-libros")
@Tag(name = "Préstamos de Libros", description = "Gestión de préstamos de libros (solicitud, aprobación, devolución, renovación)")
public class PrestamoLibroController {

    private final PrestamoLibroService prestamoService;

    public PrestamoLibroController(PrestamoLibroService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    @Operation(summary = "Listar préstamos de libros (admin/bibliotecario ven todos; aprendiz/instructor ven los propios)")
    public List<PrestamoLibroResponseDTO> listar(@AuthenticationPrincipal CurrentUser currentUser) {
        if (currentUser.rol() == co.sena.adso.biblioteca.entity.RolUsuario.administrador ||
            currentUser.rol() == co.sena.adso.biblioteca.entity.RolUsuario.bibliotecario) {
            return prestamoService.findAll();
        }
        return prestamoService.findByUsuarioId(currentUser.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener préstamo por ID")
    public PrestamoLibroResponseDTO obtener(@PathVariable Long id,
                                            @AuthenticationPrincipal CurrentUser currentUser) {
        var p = prestamoService.findById(id);
        if (currentUser.rol() != co.sena.adso.biblioteca.entity.RolUsuario.administrador &&
            currentUser.rol() != co.sena.adso.biblioteca.entity.RolUsuario.bibliotecario &&
            (p.usuarioId() == null || !p.usuarioId().equals(currentUser.id()))) {
            throw new co.sena.adso.biblioteca.exception.BusinessException("No tienes permiso para consultar este préstamo.");
        }
        return p;
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Listar préstamos de un usuario")
    public List<PrestamoLibroResponseDTO> listarPorUsuario(@PathVariable Long idUsuario,
                                                           @AuthenticationPrincipal CurrentUser currentUser) {
        if (currentUser.rol() != co.sena.adso.biblioteca.entity.RolUsuario.administrador &&
            currentUser.rol() != co.sena.adso.biblioteca.entity.RolUsuario.bibliotecario &&
            !currentUser.id().equals(idUsuario)) {
            throw new co.sena.adso.biblioteca.exception.BusinessException("No tienes permiso para ver los préstamos de otros usuarios.");
        }
        return prestamoService.findByUsuarioId(idUsuario);
    }

    @GetMapping("/libro/{idLibro}")
    @Operation(summary = "Listar préstamos de un libro (admin/bibliotecario)")
    public List<PrestamoLibroResponseDTO> listarPorLibro(@PathVariable Long idLibro,
                                                         @AuthenticationPrincipal CurrentUser currentUser) {
        if (currentUser.rol() != co.sena.adso.biblioteca.entity.RolUsuario.administrador &&
            currentUser.rol() != co.sena.adso.biblioteca.entity.RolUsuario.bibliotecario) {
            throw new co.sena.adso.biblioteca.exception.BusinessException("No tienes permiso para ver el historial general de este libro.");
        }
        return prestamoService.findByLibroId(idLibro);
    }

    @PostMapping
    @Operation(summary = "Crear préstamo de libro")
    public ResponseEntity<PrestamoLibroResponseDTO> crear(@Valid @RequestBody PrestamoLibroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamoService.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar préstamo de libro")
    public PrestamoLibroResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody PrestamoLibroRequestDTO dto) {
        return prestamoService.update(id, dto);
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado del préstamo")
    public PrestamoLibroResponseDTO cambiarEstado(@PathVariable Long id, @RequestBody EstadoPrestamoLibro estado) {
        return prestamoService.cambiarEstado(id, estado);
    }

    @PutMapping("/{id}/aceptar")
    @Operation(summary = "Aceptar solicitud de préstamo (admin/bibliotecario)")
    public PrestamoLibroResponseDTO aceptar(@PathVariable Long id, @AuthenticationPrincipal CurrentUser currentUser) {
        return prestamoService.aceptar(id, currentUser);
    }

    @PutMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar solicitud de préstamo (admin/bibliotecario)")
    public PrestamoLibroResponseDTO rechazar(@PathVariable Long id,
                                             @RequestBody(required = false) RechazoDTO dto,
                                             @AuthenticationPrincipal CurrentUser currentUser) {
        return prestamoService.rechazar(id, dto != null ? dto.razon() : null, currentUser);
    }

    @PutMapping("/{id}/devolver")
    @Operation(summary = "Registrar devolución (admin/bibliotecario)")
    public PrestamoLibroResponseDTO devolver(@PathVariable Long id,
                                             @Valid @RequestBody PrestamoDevolucionDTO dto,
                                             @AuthenticationPrincipal CurrentUser currentUser) {
        return prestamoService.devolver(id, dto, currentUser);
    }

    @PutMapping("/{id}/renovar")
    @Operation(summary = "Solicitar renovación de préstamo")
    public PrestamoLibroResponseDTO solicitarRenovacion(@PathVariable Long id,
                                                        @Valid @RequestBody RenovacionPrestamoRequestDTO dto,
                                                        @AuthenticationPrincipal CurrentUser currentUser) {
        return prestamoService.solicitarRenovacion(id, dto, currentUser);
    }

    @PutMapping("/{id}/procesar-renovacion")
    @Operation(summary = "Aprobar/rechazar renovación (admin/bibliotecario)")
    public PrestamoLibroResponseDTO procesarRenovacion(@PathVariable Long id,
                                                       @Valid @RequestBody ProcesarRenovacionDTO dto,
                                                       @AuthenticationPrincipal CurrentUser currentUser) {
        return prestamoService.procesarRenovacion(id, dto, currentUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar préstamo de libro")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        prestamoService.delete(id);
    }

    public record RechazoDTO(String razon) {}
}
