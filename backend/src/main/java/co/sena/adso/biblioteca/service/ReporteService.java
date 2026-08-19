package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.dto.PrestamoLibroResponseDTO;
import co.sena.adso.biblioteca.dto.PrestamoResponseDTO;
import co.sena.adso.biblioteca.entity.*;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes y estadísticas por rol (equivalente a reportes.py + dashboard de la referencia).
 */
@Service
public class ReporteService {

    private final EquipoRepository equipoRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final PrestamoLibroRepository prestamoLibroRepository;
    private final UsuarioRepository usuarioRepository;
    private final MultaRepository multaRepository;
    private final PrestamoRules rules;

    public ReporteService(EquipoRepository equipoRepository, LibroRepository libroRepository,
                          PrestamoRepository prestamoRepository, PrestamoLibroRepository prestamoLibroRepository,
                          UsuarioRepository usuarioRepository, MultaRepository multaRepository,
                          PrestamoRules rules) {
        this.equipoRepository = equipoRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.prestamoLibroRepository = prestamoLibroRepository;
        this.usuarioRepository = usuarioRepository;
        this.multaRepository = multaRepository;
        this.rules = rules;
    }

    private boolean verEquipos(RolUsuario rol) {
        return rol == RolUsuario.administrador || rol == RolUsuario.almacenista;
    }

    private boolean verLibros(RolUsuario rol) {
        return rol == RolUsuario.administrador || rol == RolUsuario.bibliotecario;
    }

    private boolean esBasico(RolUsuario rol) {
        return rol == RolUsuario.aprendiz || rol == RolUsuario.instructor;
    }

    // ------------------------------------------------------------------
    // DASHBOARD
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard(RolUsuario rol, Long idUsuario) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("rol", rol.name());

        if (esBasico(rol)) {
            Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
            long prestamosUsuario = rules.prestamosActivosCount(usuario);
            long multasUsuario = usuario != null ? rules.multasPendientesCount(usuario) : 0;
            data.put("prestamosActivosUsuario", prestamosUsuario);
            data.put("multasPendientesUsuario", multasUsuario);
            data.put("tieneMultasPendientes", multasUsuario > 0);
            data.put("limitePrestamos", usuario != null ? rules.limitePrestamos(usuario.getRol()) : 0);
            return data;
        }

        long prestamosEquiposActivos = countEquiposActivos();
        long prestamosLibrosActivos = countLibrosActivos();
        long multasActivas = countMultasPendientes();

        data.put("totalEquipos", verEquipos(rol) ? equipoRepository.countByEliminadoFalse() : 0);
        data.put("totalLibros", verLibros(rol) ? libroRepository.countByEliminadoFalse() : 0);
        data.put("equiposPrestados", verEquipos(rol)
            ? equipoRepository.countByEstado(EstadoEquipo.prestado) : 0);
        data.put("librosPrestados", verLibros(rol)
            ? libroRepository.countByEstado(EstadoLibro.prestado) : 0);
        data.put("prestamosEquiposActivos", verEquipos(rol) ? prestamosEquiposActivos : 0);
        data.put("prestamosLibrosActivos", verLibros(rol) ? prestamosLibrosActivos : 0);
        data.put("multasActivas", verEquipos(rol) || verLibros(rol) ? multasActivas : 0);

        if (rol == RolUsuario.administrador) {
            data.put("usuariosActivos", usuarioRepository.countByEstado(EstadoUsuario.activo));
            data.put("sancionesVigentes", multasActivas);
        } else if (rol == RolUsuario.bibliotecario) {
            data.put("multasLibros", multaRepository
                .findByTipoRecursoOrderByFechaGeneracionDesc(TipoRecurso.libro).stream()
                .filter(m -> m.getEstado() == EstadoMulta.acumulando || m.getEstado() == EstadoMulta.activa)
                .count());
        } else if (rol == RolUsuario.almacenista) {
            data.put("multasEquipos", multaRepository
                .findByTipoRecursoOrderByFechaGeneracionDesc(TipoRecurso.equipo).stream()
                .filter(m -> m.getEstado() == EstadoMulta.acumulando || m.getEstado() == EstadoMulta.activa)
                .count());
        }
        return data;
    }

    // ------------------------------------------------------------------
    // REPORTES JSON
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Map<String, Object> inventario(RolUsuario rol, String estado, String tipo,
                                          LocalDate desde, LocalDate hasta) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (esBasico(rol)) {
            throw new BusinessException("No tienes permisos para acceder a reportes de inventario.");
        }
        if (verEquipos(rol)) {
            data.put("equipos", equipoRepository.findByEliminadoFalse().stream()
                .filter(e -> estado == null || estado.isBlank() || e.getEstado().name().equalsIgnoreCase(estado))
                .filter(e -> tipo == null || tipo.isBlank()
                    || e.getTipoEquipo() == null || e.getTipoEquipo().equalsIgnoreCase(tipo))
                .filter(e -> dentroDeRango(desde, hasta, e.getFechaRegistro()))
                .sorted(Comparator.comparing(Equipo::getFechaRegistro,
                    Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ReporteService::equipoMap)
                .toList());
        }
        if (verLibros(rol)) {
            data.put("libros", libroRepository.findByEliminadoFalse().stream()
                .filter(l -> estado == null || estado.isBlank() || l.getEstado().name().equalsIgnoreCase(estado))
                .filter(l -> dentroDeRango(desde, hasta, l.getFechaRegistro()))
                .sorted(Comparator.comparing(Libro::getFechaRegistro,
                    Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ReporteService::libroMap)
                .toList());
        }
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> prestamos(RolUsuario rol, String estado, String tipoRecurso,
                                         LocalDate desde, LocalDate hasta) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (esBasico(rol)) {
            throw new BusinessException("No tienes permisos para acceder a reportes de préstamos.");
        }
        boolean incluirEquipos = verEquipos(rol) && !"libros".equalsIgnoreCase(tipoRecurso);
        boolean incluirLibros = verLibros(rol) && !"equipos".equalsIgnoreCase(tipoRecurso);
        if (incluirEquipos) {
            data.put("prestamosEquipos", prestamoRepository.findAll().stream()
                .filter(p -> filtrarEstado(p.getEstado().name(), estado))
                .filter(p -> dentroDeRango(desde, hasta, p.getFechaSolicitud()))
                .sorted(Comparator.comparing(Prestamo::getFechaSolicitud,
                    Comparator.nullsLast(Comparator.reverseOrder())))
                .map(PrestamoResponseDTO::fromEntity)
                .toList());
        }
        if (incluirLibros) {
            data.put("prestamosLibros", prestamoLibroRepository.findAll().stream()
                .filter(p -> filtrarEstado(p.getEstado().name(), estado))
                .filter(p -> dentroDeRango(desde, hasta, p.getFechaSolicitud()))
                .sorted(Comparator.comparing(PrestamoLibro::getFechaSolicitud,
                    Comparator.nullsLast(Comparator.reverseOrder())))
                .map(PrestamoLibroResponseDTO::fromEntity)
                .toList());
        }
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> misPrestamos(Long idUsuario, String estado) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("prestamosEquipos", prestamoRepository.findByUsuarioId(idUsuario).stream()
            .filter(p -> filtrarEstado(p.getEstado().name(), estado))
            .sorted(Comparator.comparing(Prestamo::getFechaSolicitud,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .map(PrestamoResponseDTO::fromEntity)
            .toList());
        data.put("prestamosLibros", prestamoLibroRepository.findByUsuarioId(idUsuario).stream()
            .filter(p -> filtrarEstado(p.getEstado().name(), estado))
            .sorted(Comparator.comparing(PrestamoLibro::getFechaSolicitud,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .map(PrestamoLibroResponseDTO::fromEntity)
            .toList());
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> usuariosActivos() {
        List<Map<String, Object>> usuarios = usuarioRepository.findByEstado(EstadoUsuario.activo).stream()
            .map(u -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", u.getId());
                m.put("nombre", u.getNombres() + " " + u.getApellidos());
                m.put("correo", u.getCorreo());
                m.put("rol", u.getRol().name());
                m.put("prestamosActivos", rules.prestamosActivosCount(u));
                return m;
            })
            .sorted(Comparator.comparingLong(
                m -> ((Number) ((Map<?, ?>) m).get("prestamosActivos")).longValue()).reversed())
            .toList();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("usuarios", usuarios);
        return data;
    }

    // ------------------------------------------------------------------
    // EXPORTACIÓN EXCEL
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String tipoReporte, RolUsuario rol, Long idUsuario) {
        if (esBasico(rol) && !"mis_prestamos".equals(tipoReporte)) {
            throw new BusinessException("No tienes permisos para exportar este reporte.");
        }
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet ws = wb.createSheet();
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            switch (tipoReporte) {
                case "inventario_equipos" -> {
                    if (!verEquipos(rol)) throw new BusinessException("Sin permisos para este reporte.");
                    wb.setSheetName(0, "Inventario Equipos");
                    List<String> headers = List.of("ID", "Nombre", "Tipo", "Marca", "Modelo", "N° Serie", "Estado", "Ubicación", "Registro");
                    escribirFilaHeader(ws, headers, headerStyle);
                    int fila = 2;
                    for (Equipo e : equipoRepository.findByEliminadoFalse()) {
                        Row row = ws.createRow(fila++);
                        row.createCell(0).setCellValue(e.getId());
                        row.createCell(1).setCellValue(orVacio(e.getNombre()));
                        row.createCell(2).setCellValue(orVacio(e.getTipoEquipo()));
                        row.createCell(3).setCellValue(orVacio(e.getMarca()));
                        row.createCell(4).setCellValue(orVacio(e.getModelo()));
                        row.createCell(5).setCellValue(orVacio(e.getNumeroSerie()));
                        row.createCell(6).setCellValue(e.getEstado() != null ? e.getEstado().name() : "");
                        row.createCell(7).setCellValue(orVacio(e.getUbicacion()));
                        row.createCell(8).setCellValue(fechaStr(e.getFechaRegistro()));
                    }
                }
                case "inventario_libros" -> {
                    if (!verLibros(rol)) throw new BusinessException("Sin permisos para este reporte.");
                    wb.setSheetName(0, "Inventario Libros");
                    List<String> headers = List.of("ID", "Título", "Autor", "Género", "Código", "Estado", "Ubicación", "Registro");
                    escribirFilaHeader(ws, headers, headerStyle);
                    int fila = 2;
                    for (Libro l : libroRepository.findByEliminadoFalse()) {
                        Row row = ws.createRow(fila++);
                        row.createCell(0).setCellValue(l.getId());
                        row.createCell(1).setCellValue(orVacio(l.getTitulo()));
                        row.createCell(2).setCellValue(orVacio(l.getAutor()));
                        row.createCell(3).setCellValue(orVacio(l.getGenero()));
                        row.createCell(4).setCellValue(orVacio(l.getCodigoUnico()));
                        row.createCell(5).setCellValue(l.getEstado() != null ? l.getEstado().name() : "");
                        row.createCell(6).setCellValue(orVacio(l.getUbicacion()));
                        row.createCell(7).setCellValue(fechaStr(l.getFechaRegistro()));
                    }
                }
                case "prestamos_equipos" -> {
                    if (!verEquipos(rol)) throw new BusinessException("Sin permisos para este reporte.");
                    wb.setSheetName(0, "Préstamos Equipos");
                    List<String> headers = List.of("Cod", "Usuario", "Equipo", "Estado", "Solicitud", "Devolución");
                    escribirFilaHeader(ws, headers, headerStyle);
                    int fila = 2;
                    for (Prestamo p : prestamoRepository.findAll()) {
                        Row row = ws.createRow(fila++);
                        row.createCell(0).setCellValue(p.getId());
                        row.createCell(1).setCellValue(nombreCompleto(p.getUsuario()));
                        row.createCell(2).setCellValue(p.getEquipo() != null ? orVacio(p.getEquipo().getNombre()) : "");
                        row.createCell(3).setCellValue(p.getEstado() != null ? p.getEstado().name() : "");
                        row.createCell(4).setCellValue(fechaStr(p.getFechaSolicitud()));
                        row.createCell(5).setCellValue(fechaOpendiente(p.getFechaDevolucionReal()));
                    }
                }
                case "prestamos_libros" -> {
                    if (!verLibros(rol)) throw new BusinessException("Sin permisos para este reporte.");
                    wb.setSheetName(0, "Préstamos Libros");
                    List<String> headers = List.of("Cod", "Usuario", "Libro", "Estado", "Solicitud", "Devolución");
                    escribirFilaHeader(ws, headers, headerStyle);
                    int fila = 2;
                    for (PrestamoLibro p : prestamoLibroRepository.findAll()) {
                        Row row = ws.createRow(fila++);
                        row.createCell(0).setCellValue(p.getId());
                        row.createCell(1).setCellValue(nombreCompleto(p.getUsuario()));
                        row.createCell(2).setCellValue(p.getLibro() != null ? orVacio(p.getLibro().getTitulo()) : "");
                        row.createCell(3).setCellValue(p.getEstado() != null ? p.getEstado().name() : "");
                        row.createCell(4).setCellValue(fechaStr(p.getFechaSolicitud()));
                        row.createCell(5).setCellValue(fechaOpendiente(p.getFechaDevolucionReal()));
                    }
                }
                case "mis_prestamos" -> {
                    wb.setSheetName(0, "Mis Préstamos");
                    List<String> headers = List.of("Tipo", "Recurso", "Estado", "Solicitud", "Devolución");
                    escribirFilaHeader(ws, headers, headerStyle);
                    int fila = 2;
                    for (Prestamo p : prestamoRepository.findByUsuarioId(idUsuario)) {
                        Row row = ws.createRow(fila++);
                        row.createCell(0).setCellValue("Equipo");
                        row.createCell(1).setCellValue(p.getEquipo() != null ? orVacio(p.getEquipo().getNombre()) : "");
                        row.createCell(2).setCellValue(p.getEstado() != null ? p.getEstado().name() : "");
                        row.createCell(3).setCellValue(fechaStr(p.getFechaSolicitud()));
                        row.createCell(4).setCellValue(fechaOpendiente(p.getFechaDevolucionReal()));
                    }
                    for (PrestamoLibro p : prestamoLibroRepository.findByUsuarioId(idUsuario)) {
                        Row row = ws.createRow(fila++);
                        row.createCell(0).setCellValue("Libro");
                        row.createCell(1).setCellValue(p.getLibro() != null ? orVacio(p.getLibro().getTitulo()) : "");
                        row.createCell(2).setCellValue(p.getEstado() != null ? p.getEstado().name() : "");
                        row.createCell(3).setCellValue(fechaStr(p.getFechaSolicitud()));
                        row.createCell(4).setCellValue(fechaOpendiente(p.getFechaDevolucionReal()));
                    }
                }
                default -> throw new BusinessException("Reporte no válido.");
            }

            for (int i = 0; i < ws.getRow(0).getLastCellNum(); i++) {
                ws.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error generando el archivo Excel", e);
        }
    }

    // ------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------

    private void escribirFilaHeader(Sheet ws, List<String> headers, CellStyle style) {
        Row row = ws.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(style);
        }
    }

    private boolean filtrarEstado(String estadoActual, String filtro) {
        return filtro == null || filtro.isBlank() || estadoActual.equalsIgnoreCase(filtro);
    }

    private boolean dentroDeRango(LocalDate desde, LocalDate hasta, LocalDateTime fecha) {
        if (fecha == null) return true;
        LocalDate dia = fecha.toLocalDate();
        if (desde != null && dia.isBefore(desde)) return false;
        if (hasta != null && dia.isAfter(hasta)) return false;
        return true;
    }

    private static Map<String, Object> equipoMap(Equipo e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("nombre", e.getNombre());
        m.put("tipoEquipo", e.getTipoEquipo());
        m.put("marca", e.getMarca());
        m.put("modelo", e.getModelo());
        m.put("numeroSerie", e.getNumeroSerie());
        m.put("estado", e.getEstado() != null ? e.getEstado().name() : null);
        m.put("ubicacion", e.getUbicacion());
        m.put("disponiblePrestamo", e.getDisponiblePrestamo());
        m.put("fechaRegistro", e.getFechaRegistro());
        return m;
    }

    private static Map<String, Object> libroMap(Libro l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("titulo", l.getTitulo());
        m.put("autor", l.getAutor());
        m.put("genero", l.getGenero());
        m.put("codigoUnico", l.getCodigoUnico());
        m.put("estado", l.getEstado() != null ? l.getEstado().name() : null);
        m.put("ubicacion", l.getUbicacion());
        m.put("disponiblePrestamo", l.getDisponiblePrestamo());
        m.put("fechaRegistro", l.getFechaRegistro());
        return m;
    }

    private long countEquiposActivos() {
        return prestamoRepository.findAll().stream()
            .filter(p -> p.getEstado() == EstadoPrestamo.pendiente || p.getEstado() == EstadoPrestamo.aceptado)
            .count();
    }

    private long countLibrosActivos() {
        return prestamoLibroRepository.findAll().stream()
            .filter(p -> p.getEstado() == EstadoPrestamoLibro.pendiente || p.getEstado() == EstadoPrestamoLibro.aceptado)
            .count();
    }

    private long countMultasPendientes() {
        return multaRepository.findAll().stream()
            .filter(m -> m.getEstado() == EstadoMulta.acumulando
                || (m.getEstado() == EstadoMulta.activa && m.getFechaFinSuspension() != null
                    && m.getFechaFinSuspension().isAfter(LocalDateTime.now())))
            .count();
    }

    private String nombreCompleto(Usuario u) {
        if (u == null) return "";
        return (u.getNombres() == null ? "" : u.getNombres()) + " " + (u.getApellidos() == null ? "" : u.getApellidos());
    }

    private String fechaStr(LocalDateTime fecha) {
        return fecha != null ? fecha.toLocalDate().toString() : "";
    }

    private String fechaOpendiente(LocalDateTime fecha) {
        return fecha != null ? fecha.toLocalDate().toString() : "Pendiente";
    }

    private String orVacio(String s) {
        return s == null ? "" : s;
    }
}
