package co.sena.adso.biblioteca.controller;

import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.security.CurrentUser;
import co.sena.adso.biblioteca.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Estadísticas del dashboard y reportes con exportación Excel")
public class ReporteController {

    private final ReporteService reporteService;
    private final co.sena.adso.biblioteca.service.PdfExportService pdfExportService;

    public ReporteController(ReporteService reporteService,
                             co.sena.adso.biblioteca.service.PdfExportService pdfExportService) {
        this.reporteService = reporteService;
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Estadísticas del dashboard según el rol")
    public Map<String, Object> dashboard(@AuthenticationPrincipal CurrentUser currentUser) {
        return reporteService.dashboard(currentUser.rol(), currentUser.id());
    }

    @GetMapping("/inventario")
    @Operation(summary = "Inventario de equipos y libros (admin/bibliotecario/almacenista)")
    public Map<String, Object> inventario(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @AuthenticationPrincipal CurrentUser currentUser) {
        return reporteService.inventario(currentUser.rol(), estado, tipo, fechaInicio, fechaFin);
    }

    @GetMapping("/prestamos")
    @Operation(summary = "Reporte de préstamos de equipos y libros (admin/bibliotecario/almacenista)")
    public Map<String, Object> prestamos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipoRecurso,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @AuthenticationPrincipal CurrentUser currentUser) {
        return reporteService.prestamos(currentUser.rol(), estado, tipoRecurso, fechaInicio, fechaFin);
    }

    @GetMapping("/mis-prestamos")
    @Operation(summary = "Historial de préstamos del usuario autenticado")
    public Map<String, Object> misPrestamos(@RequestParam(required = false) String estado,
                                            @AuthenticationPrincipal CurrentUser currentUser) {
        return reporteService.misPrestamos(currentUser.id(), estado);
    }

    @GetMapping("/usuarios-activos")
    @Operation(summary = "Usuarios activos con conteo de préstamos (solo admin)")
    public Map<String, Object> usuariosActivos() {
        return reporteService.usuariosActivos();
    }

    @GetMapping(value = "/exportar/excel/{tipoReporte}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Exportar reporte a Excel",
        description = "tipos: inventario_equipos, inventario_libros, prestamos_equipos, prestamos_libros, mis_prestamos")
    public ResponseEntity<byte[]> exportarExcel(@PathVariable String tipoReporte,
                                                @AuthenticationPrincipal CurrentUser currentUser) {
        byte[] bytes = reporteService.exportarExcel(tipoReporte, currentUser.rol(), currentUser.id());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"reporte_" + tipoReporte + "_" + LocalDate.now() + ".xlsx\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(bytes);
    }

    @GetMapping(value = "/exportar/pdf/{tipoReporte}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Exportar reporte a PDF",
        description = "tipos: inventario_equipos, inventario_libros, prestamos_equipos, prestamos_libros, mis_prestamos")
    public ResponseEntity<byte[]> exportarPdf(@PathVariable String tipoReporte,
                                              @AuthenticationPrincipal CurrentUser currentUser) {
        byte[] bytes = pdfExportService.exportarPdf(tipoReporte, currentUser.rol(), currentUser.id());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"reporte_" + tipoReporte + "_" + LocalDate.now() + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(bytes);
    }

    @ExceptionHandler(BusinessException.class)
    public Map<String, Object> manejarBusiness(BusinessException e) {
        return Map.of("error", e.getMessage());
    }
}
