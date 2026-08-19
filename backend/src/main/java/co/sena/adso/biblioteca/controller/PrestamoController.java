package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.PrestamoDevolucionDTO;
import co.sena.adso.biblioteca.dto.PrestamoRequestDTO;
import co.sena.adso.biblioteca.dto.PrestamoResponseDTO;
import co.sena.adso.biblioteca.dto.ProcesarRenovacionDTO;
import co.sena.adso.biblioteca.dto.RenovacionPrestamoRequestDTO;
import co.sena.adso.biblioteca.security.CurrentUser;
import co.sena.adso.biblioteca.service.PrestamoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@Tag(name = "Préstamos de Equipos", description = "Gestión de préstamos de equipos (solicitud, aprobación, devolución, renovación)")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los préstamos de equipos (admin/almacenista ven todos; aprendiz/instructor ven los propios)")
    public List<PrestamoResponseDTO> listar(@AuthenticationPrincipal CurrentUser currentUser) {
        if (currentUser.rol() == co.sena.adso.biblioteca.entity.RolUsuario.administrador ||
            currentUser.rol() == co.sena.adso.biblioteca.entity.RolUsuario.almacenista) {
            return prestamoService.findAll();
        }
        return prestamoService.findByUsuarioId(currentUser.id());
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Listar préstamos de un usuario")
    public List<PrestamoResponseDTO> listarPorUsuario(@PathVariable Long idUsuario,
                                                      @AuthenticationPrincipal CurrentUser currentUser) {
        if (currentUser.rol() != co.sena.adso.biblioteca.entity.RolUsuario.administrador &&
            currentUser.rol() != co.sena.adso.biblioteca.entity.RolUsuario.almacenista &&
            !currentUser.id().equals(idUsuario)) {
            throw new co.sena.adso.biblioteca.exception.BusinessException("No tienes permiso para ver los préstamos de otros usuarios.");
        }
        return prestamoService.findByUsuarioId(idUsuario);
    }

    @GetMapping("/equipo/{idEquipo}")
    @Operation(summary = "Listar préstamos de un equipo (admin/almacenista)")
    public List<PrestamoResponseDTO> listarPorEquipo(@PathVariable Long idEquipo,
                                                     @AuthenticationPrincipal CurrentUser currentUser) {
        if (currentUser.rol() != co.sena.adso.biblioteca.entity.RolUsuario.administrador &&
            currentUser.rol() != co.sena.adso.biblioteca.entity.RolUsuario.almacenista) {
            throw new co.sena.adso.biblioteca.exception.BusinessException("No tienes permiso para ver el historial general de este equipo.");
        }
        return prestamoService.findByEquipoId(idEquipo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener préstamo por ID")
    public PrestamoResponseDTO obtener(@PathVariable Long id, @AuthenticationPrincipal CurrentUser currentUser) {
        return prestamoService.findById(id, currentUser);
    }

    @PostMapping
    @Operation(summary = "Solicitar préstamo de equipo (usuario) o crearlo aceptado (admin/almacenista)")
    public ResponseEntity<PrestamoResponseDTO> crear(@Valid @RequestBody PrestamoRequestDTO dto,
                                                     @AuthenticationPrincipal CurrentUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamoService.crear(dto, currentUser));
    }

    @PutMapping("/{id}/aceptar")
    @Operation(summary = "Aceptar solicitud de préstamo (admin/almacenista)")
    public PrestamoResponseDTO aceptar(@PathVariable Long id, @AuthenticationPrincipal CurrentUser currentUser) {
        return prestamoService.aceptar(id, currentUser);
    }

    @PutMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar solicitud de préstamo (admin/almacenista)")
    public PrestamoResponseDTO rechazar(@PathVariable Long id,
                                        @RequestBody(required = false) RechazoDTO dto,
                                        @AuthenticationPrincipal CurrentUser currentUser) {
        return prestamoService.rechazar(id, dto != null ? dto.razon() : null, currentUser);
    }

    @PutMapping("/{id}/devolver")
    @Operation(summary = "Registrar devolución (admin/almacenista)")
    public PrestamoResponseDTO devolver(@PathVariable Long id,
                                        @Valid @RequestBody PrestamoDevolucionDTO dto,
                                        @AuthenticationPrincipal CurrentUser currentUser) {
        return prestamoService.devolver(id, dto, currentUser);
    }

    @PutMapping("/{id}/renovar")
    @Operation(summary = "Solicitar renovación de préstamo")
    public PrestamoResponseDTO solicitarRenovacion(@PathVariable Long id,
                                                   @Valid @RequestBody RenovacionPrestamoRequestDTO dto,
                                                   @AuthenticationPrincipal CurrentUser currentUser) {
        return prestamoService.solicitarRenovacion(id, dto, currentUser);
    }

    @PutMapping("/{id}/procesar-renovacion")
    @Operation(summary = "Aprobar/rechazar renovación (admin/almacenista)")
    public PrestamoResponseDTO procesarRenovacion(@PathVariable Long id,
                                                  @Valid @RequestBody ProcesarRenovacionDTO dto,
                                                  @AuthenticationPrincipal CurrentUser currentUser) {
        return prestamoService.procesarRenovacion(id, dto, currentUser);
    }

    public record RechazoDTO(String razon) {}
}
