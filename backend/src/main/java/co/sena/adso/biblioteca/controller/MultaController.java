package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.dto.MultaCondonarDTO;
import co.sena.adso.biblioteca.dto.MultaResponseDTO;
import co.sena.adso.biblioteca.entity.EstadoMulta;
import co.sena.adso.biblioteca.entity.RolUsuario;
import co.sena.adso.biblioteca.entity.TipoRecurso;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.security.CurrentUser;
import co.sena.adso.biblioteca.service.EmailService;
import co.sena.adso.biblioteca.service.MultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/multas")
@Tag(name = "Multas / Suspensiones", description = "Sanciones por retraso en devoluciones")
public class MultaController {

    private final MultaService multaService;
    private final EmailService emailService;

    public MultaController(MultaService multaService, EmailService emailService) {
        this.multaService = multaService;
        this.emailService = emailService;
    }

    @GetMapping
    @Operation(summary = "Listar multas filtradas por rol y estado")
    public List<MultaResponseDTO> listar(@RequestParam(required = false) EstadoMulta estado,
                                         @AuthenticationPrincipal CurrentUser currentUser) {
        return multaService.listar(currentUser.rol(), currentUser.id(), estado);
    }

    @PostMapping("/{id}/condonar")
    @Operation(summary = "Condonar sanción (admin/bibliotecario/almacenista)")
    public MultaResponseDTO condonar(@PathVariable Long id,
                                     @Valid @RequestBody MultaCondonarDTO dto,
                                     @AuthenticationPrincipal CurrentUser currentUser) {
        if (currentUser.rol() == RolUsuario.bibliotecario || currentUser.rol() == RolUsuario.almacenista) {
            // La validación por tipo de recurso se hace recargando la multa.
            var multa = multaService.findById(id);
            if (currentUser.rol() == RolUsuario.bibliotecario && multa.tipoRecurso() != TipoRecurso.libro) {
                throw new BusinessException("Solo puedes condonar suspensiones relacionadas con libros.");
            }
            if (currentUser.rol() == RolUsuario.almacenista && multa.tipoRecurso() != TipoRecurso.equipo) {
                throw new BusinessException("Solo puedes condonar suspensiones relacionadas con equipos.");
            }
        }
        var condonada = multaService.condonar(id, dto.observacion(), currentUser.id());
        emailService.notificarMulta(condonada.getUsuario().getNombres(), condonada.getUsuario().getCorreo(),
            "condonada", "Un administrador revisó tu caso y condonó tu suspensión. Ya puedes solicitar préstamos.");
        return MultaResponseDTO.fromEntity(condonada);
    }
}
